package fr.lunaison.core.model

import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.Base64

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
    /** Reconstruit une liste minimale de journées de règles pour alimenter [CycleAnalyzer]. */
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

    /** Encode l'empreinte sous la forme d'une chaîne courte `LUNA1:...` compatible QR Code et SMS. */
    fun encode(): String {
        val cleanName = partnerName.replace("|", " ").trim().take(30)
        val periodsPart = periods
            .takeLast(6)
            .joinToString(",") { "${it.start.toEpochDay()}:${it.length.coerceIn(1, 15)}" }
        val payload = listOf(
            "v1",
            cleanName,
            sharedAt.toEpochDay().toString(),
            usualCycleLength.coerceIn(21, 45).toString(),
            if (showFertility) "1" else "0",
            periodsPart,
        ).joinToString("|")
        val b64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray(StandardCharsets.UTF_8))
        return "$PREFIX$b64"
    }

    companion object {
        const val PREFIX = "LUNA1:"
        private val TOKEN_REGEX = Regex("""LUNA1:[A-Za-z0-9_-]+""")

        /**
         * Décode une chaîne `LUNA1:...` ou extrait automatiquement le code s'il est entouré du texte d'un SMS.
         * Renvoie `null` si le code est invalide.
         */
        fun decode(raw: String): PartnerSnapshot? = runCatching {
            val match = TOKEN_REGEX.find(raw.trim())?.value ?: return null
            val b64 = match.removePrefix(PREFIX)
            val decoded = String(Base64.getUrlDecoder().decode(b64), StandardCharsets.UTF_8)
            val parts = decoded.split("|")
            if (parts.size < 6 || parts[0] != "v1") return null
            val name = parts[1].trim().ifEmpty { tr("Partenaire", "Partner") }
            val sharedAt = LocalDate.ofEpochDay(parts[2].toLong())
            val usualLength = parts[3].toInt().coerceIn(21, 45)
            val showFertility = parts[4] == "1"
            val periods = if (parts[5].isBlank()) {
                emptyList()
            } else {
                parts[5].split(",").mapNotNull { item ->
                    val sub = item.split(":")
                    if (sub.size != 2) null
                    else PartnerPeriod(
                        start = LocalDate.ofEpochDay(sub[0].toLong()),
                        length = sub[1].toInt().coerceIn(1, 15),
                    )
                }
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
