package fr.lunaison.core.data

import android.content.Context
import fr.lunaison.core.cycle.AlertKind
import fr.lunaison.core.cycle.LogReminderMode
import fr.lunaison.core.cycle.ReminderSettings
import fr.lunaison.core.model.AgeGroup
import fr.lunaison.core.model.Baseline
import fr.lunaison.core.model.CervicalMucus
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.IntimacyKind
import fr.lunaison.core.model.Libido
import fr.lunaison.core.model.MedicationKind
import fr.lunaison.core.model.Mood
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.PartnerSnapshot
import fr.lunaison.core.model.PillStatus
import fr.lunaison.core.model.Symptom
import fr.lunaison.core.model.ThemePalette
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.time.LocalDate
import java.time.LocalTime
import fr.lunaison.core.model.tr

/** Messagerie utilisée pour le partage. L'appli ne fait que préparer le message. */
enum class PartnerChannel(private val fr: String, private val en: String) {
    SMS("SMS", "SMS"),
    WHATSAPP("WhatsApp", "WhatsApp"),
    SIGNAL("Signal", "Signal"),
    OTHER("Autre appli", "Another app"),
    ;

    val label: String get() = tr(fr, en)
}

data class Partner(val name: String, val phone: String?, val channel: PartnerChannel)

data class AppSettings(
    val onboarded: Boolean = false,
    val baseline: Baseline = Baseline(AgeGroup.ADULT),
    val reminders: ReminderSettings = ReminderSettings(),
    val partner: Partner? = null,
    /** Signaux masqués par la personne, avec la date du masquage : ils reviennent au bout de 60 jours. */
    val dismissedAlerts: Map<AlertKind, LocalDate> = emptyMap(),
    /** Début du cycle dont le bilan a été lu ou masqué. */
    val recapSeen: LocalDate? = null,
    /** Met en pause les prévisions et les signaux (grossesse, allaitement, contraception continue). */
    val pausePredictions: Boolean = false,
    /** Affiche la fenêtre fertile estimée et les chances de concevoir. */
    val showFertility: Boolean = false,
    /** Palette de couleurs choisie. */
    val themePalette: ThemePalette = ThemePalette.PLUM,
    /** Empreinte SHA-256 du code PIN à 4 chiffres, ou null si aucun verrouillage n'est actif. */
    val pinHash: String? = null,
    /** Empreinte du cycle d'un ou d'une partenaire importée par QR Code ou SMS. */
    val followedPartner: PartnerSnapshot? = null,
    /** Prénom affiché dans le QR Code ou le SMS généré pour son ou sa partenaire. */
    val myShareName: String = "",
    /** Active le suivi facultatif des signes d'ovulation (glaire cervicale et température basale). */
    val trackOvulationSigns: Boolean = false,
    /** Mode adapté aux cycles naturellement irréguliers (périménopause, SOPK, post-partum). */
    val irregularCyclesMode: Boolean = false,
    /** Mémorise si la demande d'avis discrète a été traitée (avis laissé ou masqué). */
    val reviewPromptDismissed: Boolean = false,
) {
    val isAdult: Boolean get() = baseline.ageGroup == AgeGroup.ADULT

    fun isDismissed(alert: AlertKind, today: LocalDate): Boolean =
        dismissedAlerts[alert]?.let { !today.isAfter(it.plusDays(ALERT_SNOOZE_DAYS)) } == true

    companion object {
        const val ALERT_SNOOZE_DAYS = 60L

        fun hashPin(pin: String): String =
            MessageDigest.getInstance("SHA-256")
                .digest(pin.toByteArray())
                .joinToString("") { "%02x".format(it) }
    }
}

/** Seule source de données de l'appli. Tout reste sur le téléphone. */
class LunaisonRepository(private val dao: LunaisonDao) {

    val logs: kotlinx.coroutines.flow.Flow<List<DayLog>> = dao.logs().map { list -> list.map { it.toModel() } }
    val settings: kotlinx.coroutines.flow.Flow<AppSettings> = dao.settings().map { it?.toModel() ?: AppSettings() }

    suspend fun logsOnce(): List<DayLog> = dao.logsOnce().map { it.toModel() }
    suspend fun settingsOnce(): AppSettings = dao.settingsOnce()?.toModel() ?: AppSettings()

    /** Une journée vide est supprimée plutôt qu'enregistrée. */
    suspend fun save(log: DayLog) {
        if (log.isEmpty) dao.deleteLog(log.date.toString()) else dao.upsertLog(log.toEntity())
    }

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        dao.upsertSettings(transform(settingsOnce()).toEntity())
    }

    /** Efface tout, sans retour possible. */
    suspend fun eraseAll() {
        dao.deleteLogs()
        dao.deleteSettings()
    }

    /** Export lisible par un tableur. Rien ne quitte le téléphone sans que la personne choisisse où l'enregistrer. */
    suspend fun exportCsv(): String = buildString {
        appendLine(tr(
            "date;flux;douleur;symptomes;humeurs;traitement;glaire;temperature;pilule;medicaments;poids;intimite;libido;note",
            "date;flow;pain;symptoms;moods;medication;mucus;temperature;pill;medications;weight;intimacy;libido;note",
        ))
        logsOnce().forEach { log ->
            appendLine(
                listOf(
                    log.date.toString(),
                    log.flow?.label.orEmpty(),
                    log.pain?.label.orEmpty(),
                    log.symptoms.joinToString(", ") { it.label },
                    log.moods.joinToString(", ") { it.label },
                    if (log.medicationTaken || log.pillStatus == PillStatus.TAKEN) tr("oui", "yes") else "",
                    log.mucus?.label.orEmpty(),
                    log.basalTemperature?.let { "%.1f".format(java.util.Locale.ROOT, it) }.orEmpty(),
                    log.pillStatus?.label.orEmpty(),
                    log.medications.joinToString(", ") { it.label },
                    log.weight?.let { "%.1f".format(java.util.Locale.ROOT, it) }.orEmpty(),
                    log.intimacy.joinToString(", ") { it.label },
                    log.libido?.label.orEmpty(),
                    log.note.replace(";", ",").replace("\n", " "),
                ).joinToString(";"),
            )
        }
    }

    /**
     * Importe un fichier CSV (format Lunaison `;` ou export externe `,` contenant dates et flux)
     * et renvoie le nombre de journées importées.
     */
    suspend fun importCsv(content: String): Int {
        var imported = 0
        val lines = content.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        for (line in lines) {
            val delimiter = if (';' in line) ';' else ','
            val cols = line.split(delimiter).map { it.trim().removeSurrounding("\"") }
            val date = cols.firstOrNull()?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: continue
            val rawFlow = cols.getOrNull(1).orEmpty()
            val flow = parseFlow(rawFlow)
            val pain = cols.getOrNull(2)?.let(::parsePain)
            val symptoms = cols.getOrNull(3)?.let(::parseSymptoms).orEmpty()
            val moods = cols.getOrNull(4)?.let(::parseMoods).orEmpty()
            val medCol = cols.getOrNull(5).orEmpty().lowercase()
            val medTaken = medCol in setOf("oui", "yes", "true", "1")
            val isV2 = cols.size >= 14
            val isV1Extended = !isV2 && cols.size >= 9
            val mucus = if (isV2 || isV1Extended) parseMucus(cols.getOrNull(6).orEmpty()) else null
            val temp = if (isV2 || isV1Extended) cols.getOrNull(7)?.replace(',', '.')?.toFloatOrNull() else null
            val pillStatus = if (isV2) parsePillStatus(cols.getOrNull(8).orEmpty()) else null
            val medications = if (isV2) parseMedications(cols.getOrNull(9).orEmpty()) else emptySet()
            val weight = if (isV2) cols.getOrNull(10)?.replace(',', '.')?.toFloatOrNull() else null
            val intimacy = if (isV2) parseIntimacy(cols.getOrNull(11).orEmpty()) else emptySet()
            val libido = if (isV2) parseLibido(cols.getOrNull(12).orEmpty()) else null
            val noteColIdx = when {
                isV2 -> 13
                isV1Extended -> 8
                else -> 6
            }
            val note = cols.getOrNull(noteColIdx).orEmpty().take(500)
            val log = DayLog(
                date = date,
                flow = flow,
                pain = pain,
                symptoms = symptoms,
                moods = moods,
                note = note,
                medicationTaken = medTaken || pillStatus == PillStatus.TAKEN,
                pillStatus = pillStatus,
                medications = medications,
                weight = weight,
                intimacy = intimacy,
                libido = libido,
                mucus = mucus,
                basalTemperature = temp,
            )
            if (!log.isEmpty) {
                save(log)
                imported += 1
            }
        }
        return imported
    }

    companion object {
        fun create(context: Context) = LunaisonRepository(LunaisonDatabase.create(context).dao())
    }
}

interface LunaisonRepositoryProvider {
    val repository: LunaisonRepository
}

private inline fun <reified T : Enum<T>> String?.toEnumOrNull(): T? =
    this?.let { name -> enumValues<T>().firstOrNull { it.name == name } }

private inline fun <reified T : Enum<T>> String.toEnumSet(): Set<T> =
    split(",").mapNotNull { it.trim().toEnumOrNull<T>() }.toSet()

internal fun DayLogEntity.toModel() = DayLog(
    date = LocalDate.parse(date),
    flow = flow.toEnumOrNull<Flow>(),
    pain = pain.toEnumOrNull<Pain>(),
    symptoms = symptoms.toEnumSet<Symptom>(),
    moods = moods.toEnumSet<Mood>(),
    note = note,
    medicationTaken = medicationTaken,
    pillStatus = pillStatus.toEnumOrNull<PillStatus>(),
    medications = medications.toEnumSet<MedicationKind>(),
    weight = weight,
    intimacy = intimacy.toEnumSet<IntimacyKind>(),
    libido = libido.toEnumOrNull<Libido>(),
    mucus = mucus.toEnumOrNull<CervicalMucus>(),
    basalTemperature = basalTemperature,
)

internal fun DayLog.toEntity() = DayLogEntity(
    date = date.toString(),
    flow = flow?.name,
    pain = pain?.name,
    symptoms = symptoms.joinToString(",") { it.name },
    moods = moods.joinToString(",") { it.name },
    note = note,
    medicationTaken = medicationTaken || pillStatus == PillStatus.TAKEN,
    pillStatus = pillStatus?.name,
    medications = medications.joinToString(",") { it.name },
    weight = weight,
    intimacy = intimacy.joinToString(",") { it.name },
    libido = libido?.name,
    mucus = mucus?.name,
    basalTemperature = basalTemperature,
)

internal fun SettingsEntity.toModel() = AppSettings(
    onboarded = onboarded,
    baseline = Baseline(
        ageGroup = ageGroup.toEnumOrNull<AgeGroup>() ?: AgeGroup.ADULT,
        lastPeriodStart = lastPeriodStart?.let(LocalDate::parse),
        usualCycleLength = usualCycleLength,
        usualPeriodLength = usualPeriodLength,
    ),
    reminders = ReminderSettings(
        periodReminder = periodReminder,
        periodReminderDaysBefore = periodReminderDaysBefore,
        logReminder = logReminder.toEnumOrNull<LogReminderMode>() ?: LogReminderMode.AROUND_PERIOD,
        logReminderTime = runCatching { LocalTime.parse(logReminderTime) }.getOrDefault(LocalTime.of(20, 0)),
        medicationReminder = medicationReminder,
        medicationReminderTime = runCatching { LocalTime.parse(medicationReminderTime) }.getOrDefault(LocalTime.of(9, 0)),
        medicationLabel = medicationLabel,
    ),
    partner = if (partnerName != null && partnerChannel != null) {
        Partner(partnerName, partnerPhone, partnerChannel.toEnumOrNull<PartnerChannel>() ?: PartnerChannel.OTHER)
    } else {
        null
    },
    dismissedAlerts = dismissedAlerts.split(",").mapNotNull { pair ->
        val parts = pair.split("@")
        val kind = parts.getOrNull(0).toEnumOrNull<AlertKind>()
        val date = parts.getOrNull(1)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (kind != null && date != null) kind to date else null
    }.toMap(),
    recapSeen = recapSeen?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
    pausePredictions = pausePredictions,
    showFertility = showFertility,
    themePalette = themePalette.toEnumOrNull<ThemePalette>() ?: ThemePalette.PLUM,
    pinHash = pinHash,
    followedPartner = followedPartnerSnapshot?.let(PartnerSnapshot::decode),
    myShareName = myShareName,
    trackOvulationSigns = trackOvulationSigns,
    irregularCyclesMode = irregularCyclesMode,
    reviewPromptDismissed = reviewPromptDismissed,
)

internal fun AppSettings.toEntity() = SettingsEntity(
    onboarded = onboarded,
    ageGroup = baseline.ageGroup.name,
    lastPeriodStart = baseline.lastPeriodStart?.toString(),
    usualCycleLength = baseline.usualCycleLength,
    usualPeriodLength = baseline.usualPeriodLength,
    periodReminder = reminders.periodReminder,
    periodReminderDaysBefore = reminders.periodReminderDaysBefore,
    logReminder = reminders.logReminder.name,
    logReminderTime = reminders.logReminderTime.toString(),
    partnerName = partner?.name,
    partnerPhone = partner?.phone,
    partnerChannel = partner?.channel?.name,
    dismissedAlerts = dismissedAlerts.entries.joinToString(",") { "${it.key.name}@${it.value}" },
    recapSeen = recapSeen?.toString(),
    medicationReminder = reminders.medicationReminder,
    medicationReminderTime = reminders.medicationReminderTime.toString(),
    medicationLabel = reminders.medicationLabel,
    pausePredictions = pausePredictions,
    showFertility = showFertility,
    themePalette = themePalette.name,
    pinHash = pinHash,
    followedPartnerSnapshot = followedPartner?.encode(),
    myShareName = myShareName,
    trackOvulationSigns = trackOvulationSigns,
    irregularCyclesMode = irregularCyclesMode,
    reviewPromptDismissed = reviewPromptDismissed,
)

private fun parseFlow(raw: String): Flow? {
    val v = raw.trim().lowercase()
    if (v.isEmpty()) return null
    return Flow.entries.firstOrNull { it.name.lowercase() == v || it.label.lowercase() == v }
        ?: when {
            "spot" in v || "trace" in v -> Flow.SPOTTING
            "light" in v || "léger" in v || "leger" in v -> Flow.LIGHT
            "heavy" in v || "abond" in v -> Flow.HEAVY
            "med" in v || "moyen" in v || "period" in v || "règle" in v -> Flow.MEDIUM
            else -> null
        }
}

private fun parsePain(raw: String): Pain? {
    val v = raw.trim().lowercase()
    if (v.isEmpty()) return null
    return Pain.entries.firstOrNull { it.name.lowercase() == v || it.label.lowercase() == v }
}

private fun parseSymptoms(raw: String): Set<Symptom> =
    raw.split(",").mapNotNull { item ->
        val v = item.trim().lowercase()
        Symptom.entries.firstOrNull { it.name.lowercase() == v || it.label.lowercase() == v }
    }.toSet()

private fun parseMoods(raw: String): Set<Mood> =
    raw.split(",").mapNotNull { item ->
        val v = item.trim().lowercase()
        Mood.entries.firstOrNull { it.name.lowercase() == v || it.label.lowercase() == v }
    }.toSet()

private fun parseMucus(raw: String): CervicalMucus? {
    val v = raw.trim().lowercase()
    if (v.isEmpty()) return null
    return CervicalMucus.entries.firstOrNull { it.name.lowercase() == v || it.label.lowercase() == v }
}

private fun parsePillStatus(raw: String): PillStatus? {
    val v = raw.trim().lowercase()
    if (v.isEmpty()) return null
    return PillStatus.entries.firstOrNull { it.name.lowercase() == v || it.label.lowercase() == v }
}

private fun parseMedications(raw: String): Set<MedicationKind> =
    raw.split(",").mapNotNull { item ->
        val v = item.trim().lowercase()
        MedicationKind.entries.firstOrNull { it.name.lowercase() == v || it.label.lowercase() == v }
    }.toSet()

private fun parseIntimacy(raw: String): Set<IntimacyKind> =
    raw.split(",").mapNotNull { item ->
        val v = item.trim().lowercase()
        IntimacyKind.entries.firstOrNull { it.name.lowercase() == v || it.label.lowercase() == v }
    }.toSet()

private fun parseLibido(raw: String): Libido? {
    val v = raw.trim().lowercase()
    if (v.isEmpty()) return null
    return Libido.entries.firstOrNull { it.name.lowercase() == v || it.label.lowercase() == v }
}
