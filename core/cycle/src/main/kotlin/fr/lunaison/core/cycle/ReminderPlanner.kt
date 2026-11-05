package fr.lunaison.core.cycle

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class LogReminderMode {
    OFF,

    /** Seulement pendant les règles et les jours où elles sont attendues. */
    AROUND_PERIOD,
    DAILY,
}

data class ReminderSettings(
    val periodReminder: Boolean = true,
    val periodReminderDaysBefore: Int = 2,
    val logReminder: LogReminderMode = LogReminderMode.AROUND_PERIOD,
    val logReminderTime: LocalTime = LocalTime.of(20, 0),
    val medicationReminder: Boolean = false,
    val medicationReminderTime: LocalTime = LocalTime.of(9, 0),
    val medicationLabel: String = "",
)

object ReminderPlanner {

    /** Heure de l'annonce des règles : le matin, quelques jours avant la date estimée. */
    val PERIOD_REMINDER_TIME: LocalTime = LocalTime.of(9, 0)

    /** Prochain rappel « règles bientôt », ou rien s'il n'y a pas lieu. */
    fun nextPeriodReminder(state: CycleState, settings: ReminderSettings, now: LocalDateTime): LocalDateTime? {
        if (!settings.periodReminder || state !is CycleState.Known || state.inPeriod || state.daysLate > 0) return null
        val day = state.next.start.minusDays(settings.periodReminderDaysBefore.coerceIn(0, 7).toLong())
        val at = LocalDateTime.of(day, PERIOD_REMINDER_TIME)
        return at.takeIf { it.isAfter(now) }
    }

    /** Prochaine heure de rappel de saisie. Le rappel lui-même vérifie au dernier moment s'il est utile. */
    fun nextLogReminder(settings: ReminderSettings, now: LocalDateTime): LocalDateTime? {
        if (settings.logReminder == LogReminderMode.OFF) return null
        val today = LocalDateTime.of(now.toLocalDate(), settings.logReminderTime)
        return if (today.isAfter(now)) today else today.plusDays(1)
    }

    /** Prochaine heure de rappel de contraception ou traitement quotidien. */
    fun nextMedicationReminder(settings: ReminderSettings, now: LocalDateTime): LocalDateTime? {
        if (!settings.medicationReminder) return null
        val today = LocalDateTime.of(now.toLocalDate(), settings.medicationReminderTime)
        return if (today.isAfter(now)) today else today.plusDays(1)
    }

    /** Le rappel de saisie est-il utile ce jour-là ? Jamais si la journée est déjà remplie. */
    fun shouldRemindToLog(state: CycleState, settings: ReminderSettings, date: LocalDate, alreadyLogged: Boolean): Boolean {
        if (alreadyLogged) return false
        return when (settings.logReminder) {
            LogReminderMode.OFF -> false
            LogReminderMode.DAILY -> true
            LogReminderMode.AROUND_PERIOD -> state is CycleState.Known && (state.inPeriod || date in state.next || state.daysLate > 0)
        }
    }

    /** Le rappel de contraception ou traitement est-il utile ce jour-là ? Jamais si la prise est déjà notée. */
    fun shouldRemindMedication(settings: ReminderSettings, alreadyTaken: Boolean): Boolean =
        settings.medicationReminder && !alreadyTaken
}
