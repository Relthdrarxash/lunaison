package fr.lunaison.ui

import fr.lunaison.core.cycle.Confidence
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.Phase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Mise en forme des dates et des phrases de prévision, en français courant. */
object Format {
    private val dayMonth = DateTimeFormatter.ofPattern("d MMMM", Locale.FRENCH)
    private val weekdayDayMonth = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)
    private val monthYear = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)

    fun dayMonth(date: LocalDate): String = date.format(dayMonth)

    fun weekday(date: LocalDate): String = date.format(weekdayDayMonth).replaceFirstChar { it.uppercase() }

    fun monthYear(date: LocalDate): String = date.format(monthYear).replaceFirstChar { it.uppercase() }

    fun days(count: Int): String = if (count <= 1) "$count jour" else "$count jours"

    fun relativeDay(date: LocalDate, today: LocalDate): String = when (date) {
        today -> "Aujourd'hui"
        today.minusDays(1) -> "Hier"
        else -> weekday(date)
    }

    fun phaseName(phase: Phase): String = when (phase) {
        Phase.MENSTRUAL -> "Règles"
        Phase.FOLLICULAR -> "Phase folliculaire"
        Phase.OVULATION -> "Autour de l'ovulation"
        Phase.LUTEAL -> "Phase lutéale"
        Phase.LATE -> "Règles en retard"
    }

    /** Phrase principale de prévision, sans fausse précision. */
    fun nextPeriod(state: CycleState.Known): String {
        val next = state.next
        return when {
            state.inPeriod -> "Règles en cours, jour ${state.dayOfCycle}."
            state.daysLate > 0 -> "Vos règles avaient été estimées vers le ${dayMonth(next.start)}. ${days(state.daysLate)} de retard."
            state.daysUntilNext <= 0 -> "Vos règles peuvent arriver d'un jour à l'autre."
            state.daysUntilNext == 1 -> "Prochaines règles probablement demain."
            else -> "Prochaines règles dans ${days(state.daysUntilNext)} environ, vers le ${dayMonth(next.start)}."
        }
    }

    fun range(state: CycleState.Known): String =
        "Plage probable : du ${dayMonth(state.next.earliest)} au ${dayMonth(state.next.latest)}."

    fun confidence(confidence: Confidence): String = when (confidence) {
        Confidence.PROVISIONAL -> "Estimation provisoire : elle s'affinera dès vos prochains cycles."
        Confidence.LOW -> "Estimation encore approximative : elle s'appuie sur peu de cycles."
        Confidence.GOOD -> "Estimation fondée sur vos derniers cycles."
    }
}
