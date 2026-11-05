package fr.lunaison.core.data

import android.content.Context
import fr.lunaison.core.cycle.AlertKind
import fr.lunaison.core.cycle.LogReminderMode
import fr.lunaison.core.cycle.ReminderSettings
import fr.lunaison.core.model.AgeGroup
import fr.lunaison.core.model.Baseline
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.Mood
import fr.lunaison.core.model.Pain
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
        appendLine(tr("date;flux;douleur;symptomes;humeurs;traitement;note", "date;flow;pain;symptoms;moods;medication;note"))
        logsOnce().forEach { log ->
            appendLine(
                listOf(
                    log.date.toString(),
                    log.flow?.label.orEmpty(),
                    log.pain?.label.orEmpty(),
                    log.symptoms.joinToString(", ") { it.label },
                    log.moods.joinToString(", ") { it.label },
                    if (log.medicationTaken) tr("oui", "yes") else "",
                    log.note.replace(";", ",").replace("\n", " "),
                ).joinToString(";"),
            )
        }
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
)

internal fun DayLog.toEntity() = DayLogEntity(
    date = date.toString(),
    flow = flow?.name,
    pain = pain?.name,
    symptoms = symptoms.joinToString(",") { it.name },
    moods = moods.joinToString(",") { it.name },
    note = note,
    medicationTaken = medicationTaken,
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
)
