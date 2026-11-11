package fr.lunaison.core.model

import kotlinx.datetime.LocalDate

/** Une période de règles résumée pour le partage décentralisé avec un ou une partenaire. */
data class PartnerPeriod(
    val start: LocalDate,
    val length: Int,
)

/**
 * Empreinte compacte et hors ligne du cycle (sans aucune note personnelle),
 * encodable en QR Code ou dans un SMS sous la forme `LUNA1:...`.
 */
data class PartnerSnapshot(
    val partnerName: String,
    val sharedAt: LocalDate,
    val periods: List<PartnerPeriod>,
    val usualCycleLength: Int = 28,
    val showFertility: Boolean = false,
) {
    /**
     * Reconstruit une liste minimale de journées de règles pour alimenter [CycleAnalyzer].
     */
    fun toDayLogs(): List<DayLog> = periods.flatMap { period ->
        val safeLength = period.length.coerceIn(1, 15)
        (0 until safeLength).map { offset ->
            DayLog(date = period.start.plusDays(offset.toLong()), flow = Flow.MEDIUM)
        }
    }

    /** Reconstruit la base de départ au cas où moins de deux cycles sont présents. */
    fun toBaseline(): Baseline = Baseline(
        ageGroup = AgeGroup.ADULT,
        lastPeriodStart = periods.maxByOrNull { it.start }?.start,
        usualCycleLength = usualCycleLength.coerceIn(21, 45),
    )

    /**
     * Fusionne cet historique avec un QR Code ou SMS de mise à jour plus récent :
     * les règles déjà connues dont le début est très proche (à 2 jours près) sont remplacées
     * par la version mise à jour, et les nouvelles règles sont ajoutées (jusqu'aux 6 dernières).
     */
    fun mergeWith(update: PartnerSnapshot): PartnerSnapshot {
        val merged = periods.toMutableList()
        for (incoming in update.periods) {
            val existingIdx = merged.indexOfFirst { kotlin.math.abs(it.start.toEpochDay() - incoming.start.toEpochDay()) <= 2 }
            if (existingIdx >= 0) {
                merged[existingIdx] = incoming
            } else {
                merged.add(incoming)
            }
        }
        val sorted = merged.sortedBy { it.start }.takeLast(6)
        return update.copy(
            partnerName = update.partnerName.ifBlank { partnerName },
            periods = sorted,
        )
    }

    /** Vérifie que le snapshot ne contient pas de dates aberrantes avant toute exploitation. */
    fun isPlausible(today: LocalDate = LocalDate.now()): Boolean = runCatching {
        if (sharedAt.isBefore(EARLIEST_SHARE) || sharedAt.isAfter(today.plusDays(SHARE_CLOCK_SKEW_DAYS))) return@runCatching false
        if (periods.size > MAX_PERIODS) return@runCatching false
        val earliest = sharedAt.minusYears(PERIOD_HISTORY_YEARS)
        val latest = sharedAt.plusDays(PERIOD_AFTER_SHARE_DAYS)
        periods.all { !it.start.isBefore(earliest) && !it.start.isAfter(latest) && it.length in 1..15 }
    }.getOrDefault(false)

    /**
     * Encode l'empreinte sous la forme d'une chaîne courte `LUNA1:...` compatible QR Code et SMS.
     * La charge utile est en version `v2` : un contrôle d'intégrité (CRC32 tronqué sur 4 caractères hexadécimaux)
     * permet de refuser un code tronqué ou altéré.
     */
    fun encode(): String {
        val periodsPart = periods
            .takeLast(MAX_PERIODS)
            .joinToString(",") { "${it.start.toEpochDay()}:${it.length.coerceIn(1, 15)}" }
        val body = listOf(
            "v2",
            sanitizeName(partnerName),
            sharedAt.toEpochDay().toString(),
            usualCycleLength.coerceIn(21, 45).toString(),
            if (showFertility) "1" else "0",
            periodsPart,
        ).joinToString("|")
        val payload = "$body|${checksum(body)}"
        val b64 = Base64Url.encode(payload.encodeToByteArray())
        return "$PREFIX$b64"
    }

    companion object {
        const val PREFIX = "LUNA1:"
        private val TOKEN_REGEX = Regex("""LUNA1:[A-Za-z0-9_-]+""")

        /** Nombre maximal de périodes transportées. */
        const val MAX_PERIODS = 6

        /** Longueur maximale du texte examiné (un SMS autour du code), pour ne pas lire un texte démesuré. */
        const val MAX_RAW_LENGTH = 2000

        private const val MAX_NAME_LENGTH = 30
        private val EARLIEST_SHARE: LocalDate = LocalDate.of(2020, 1, 1)

        /** Tolérance sur la date de partage : l'horloge de l'émetteur peut avoir un peu d'avance. */
        private const val SHARE_CLOCK_SKEW_DAYS = 2L
        private const val PERIOD_HISTORY_YEARS = 5L
        /** Une période déclarée peut démarrer légèrement après le partage (décalage de fuseau). */
        private const val PERIOD_AFTER_SHARE_DAYS = 1L

        private fun checksum(body: String): String {
            val low16 = Crc32.of(body.encodeToByteArray()) and 0xFFFF
            return low16.toString(16).padStart(4, '0')
        }

        /** Nettoie le nom d'un partage non fiable (le séparateur `|` casserait le format) et borne sa longueur. */
        private fun sanitizeName(name: String): String {
            val trimmed = name.withoutInvisibleCharacters().trim()
            return trimmed.takeCodePoints(MAX_NAME_LENGTH).trim()
        }

        /** Décode de l'UTF-8 en refusant toute séquence invalide (une entrée altérée ne doit jamais passer). */
        private fun decodeUtf8Strict(bytes: ByteArray): String = bytes.decodeToString(throwOnInvalidSequence = true)

        /**
         * Décode une chaîne `LUNA1:...` ou extrait automatiquement le code s'il est entouré du texte d'un SMS.
         * Renvoie `null` si le code est invalide, tronqué, trop volumineux ou hors des bornes plausibles :
         * le partage d'une autre personne est une entrée non fiable.
         *
         * @param fallbackName nom repris si le code n'en contient pas (par ex. celui du partenaire déjà suivi).
         * @param today horloge, injectable pour les tests.
         */
        fun decode(
            raw: String,
            fallbackName: String? = null,
            today: LocalDate = LocalDate.now(),
        ): PartnerSnapshot? = runCatching {
            if (raw.length > MAX_RAW_LENGTH) return null
            val match = TOKEN_REGEX.find(raw.trim())?.value ?: return null
            val b64 = match.removePrefix(PREFIX)
            val decoded = decodeUtf8Strict(Base64Url.decode(b64))
            val parts = decoded.split("|")
            when (parts.firstOrNull()) {
                // Un code v1 n'a pas de contrôle d'intégrité : on exige au moins une période, ce qui écarte
                // un code coupé juste avant le champ des périodes (le partage n'en émet jamais sans période).
                "v1" -> if (parts.size != 6 || parts[5].isEmpty()) return null
                "v2" -> {
                    if (parts.size != 7) return null
                    val body = decoded.substring(0, decoded.lastIndexOf('|'))
                    if (parts[6] != checksum(body)) return null
                }
                else -> return null
            }
            val name = sanitizeName(parts[1]).ifEmpty { fallbackName?.let(::sanitizeName).orEmpty() }
                .ifEmpty { tr(fr = "Partenaire", en = "Partner", pl = "Partner", uk = "Партнер", es = "Pareja", de = "Partner") }
            val sharedDay = parts[2].toLongOrNull() ?: return null
            if (sharedDay < EARLIEST_SHARE.toEpochDay() || sharedDay > today.plusDays(SHARE_CLOCK_SKEW_DAYS).toEpochDay()) return null
            val sharedAt = LocalDate.ofEpochDay(sharedDay)
            val usualLength = (parts[3].toIntOrNull() ?: return null).coerceIn(21, 45)
            val showFertility = when (parts[4]) {
                "1" -> true
                "0" -> false
                else -> return null
            }
            val earliestStart = sharedAt.minusYears(PERIOD_HISTORY_YEARS).toEpochDay()
            val latestStart = sharedAt.plusDays(PERIOD_AFTER_SHARE_DAYS).toEpochDay()
            val periods = if (parts[5].isEmpty()) {
                emptyList()
            } else {
                val items = parts[5].split(",")
                if (items.size > MAX_PERIODS) return null
                items.map { item ->
                    val sub = item.split(":")
                    if (sub.size != 2) return null
                    val startDay = sub[0].toLongOrNull() ?: return null
                    if (startDay < earliestStart || startDay > latestStart) return null
                    PartnerPeriod(
                        start = LocalDate.ofEpochDay(startDay),
                        length = (sub[1].toIntOrNull() ?: return null).coerceIn(1, 15),
                    )
                }.sortedBy { it.start }.distinctBy { it.start }
            }
            PartnerSnapshot(
                partnerName = name,
                sharedAt = sharedAt,
                periods = periods,
                usualCycleLength = usualLength,
                showFertility = showFertility,
            )
        }.getOrNull()
    }
}
