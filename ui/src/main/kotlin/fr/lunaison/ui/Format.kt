package fr.lunaison.ui

import fr.lunaison.core.cycle.Confidence
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.Phase
import fr.lunaison.core.model.Language
import fr.lunaison.core.model.tr
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Mise en forme des dates et des phrases de prévision, dans la langue de l'appli. */
object Format {
    private val french = Formatters(Locale.FRENCH, "d MMMM", "EEEE d MMMM", "MMMM yyyy")
    private val english = Formatters(Locale.ENGLISH, "MMMM d", "EEEE, MMMM d", "MMMM yyyy")

    private class Formatters(locale: Locale, dayMonth: String, weekday: String, monthYear: String) {
        val dayMonth: DateTimeFormatter = DateTimeFormatter.ofPattern(dayMonth, locale)
        val weekday: DateTimeFormatter = DateTimeFormatter.ofPattern(weekday, locale)
        val monthYear: DateTimeFormatter = DateTimeFormatter.ofPattern(monthYear, locale)
    }

    private val current: Formatters get() = if (Language.current() == Language.FR) french else english

    fun dayMonth(date: LocalDate): String = date.format(current.dayMonth)

    fun weekday(date: LocalDate): String = date.format(current.weekday).replaceFirstChar { it.uppercase() }

    fun monthYear(date: LocalDate): String = date.format(current.monthYear).replaceFirstChar { it.uppercase() }

    fun days(count: Int): String = if (count <= 1) tr("$count jour", "$count day") else tr("$count jours", "$count days")

    fun relativeDay(date: LocalDate, today: LocalDate): String = when (date) {
        today -> tr("Aujourd'hui", "Today")
        today.minusDays(1) -> tr("Hier", "Yesterday")
        else -> weekday(date)
    }

    fun phaseName(phase: Phase): String = when (phase) {
        Phase.MENSTRUAL -> tr("Règles", "Period")
        Phase.FOLLICULAR -> tr("Phase folliculaire", "Follicular phase")
        Phase.OVULATION -> tr("Autour de l'ovulation", "Around ovulation")
        Phase.LUTEAL -> tr("Phase lutéale", "Luteal phase")
        Phase.LATE -> tr("Règles en retard", "Period late")
    }

    /** Phrase principale de prévision, sans fausse précision. */
    fun nextPeriod(state: CycleState.Known): String {
        val next = state.next
        return when {
            state.inPeriod -> tr("Règles en cours, jour ${state.dayOfCycle}.", "Period in progress, day ${state.dayOfCycle}.")
            state.daysLate > 0 -> tr(
                "Vos règles avaient été estimées vers le ${dayMonth(next.start)}. ${days(state.daysLate)} de retard.",
                "Your period was expected around ${dayMonth(next.start)}. ${days(state.daysLate)} late.",
            )
            state.daysUntilNext <= 0 -> tr("Vos règles peuvent arriver d'un jour à l'autre.", "Your period may arrive any day now.")
            state.daysUntilNext == 1 -> tr("Prochaines règles probablement demain.", "Next period probably tomorrow.")
            else -> tr(
                "Prochaines règles dans ${days(state.daysUntilNext)} environ, vers le ${dayMonth(next.start)}.",
                "Next period in about ${days(state.daysUntilNext)}, around ${dayMonth(next.start)}.",
            )
        }
    }

    fun range(state: CycleState.Known): String = tr(
        "Plage probable : du ${dayMonth(state.next.earliest)} au ${dayMonth(state.next.latest)}.",
        "Likely range: ${dayMonth(state.next.earliest)} to ${dayMonth(state.next.latest)}.",
    )

    fun fertileWindow(state: CycleState.Known): String = tr(
        "Fenêtre fertile estimée : du ${dayMonth(state.fertileWindow.start)} au ${dayMonth(state.fertileWindow.endInclusive)} (ovulation estimée vers le ${dayMonth(state.ovulationDate)}).",
        "Estimated fertile window: ${dayMonth(state.fertileWindow.start)} to ${dayMonth(state.fertileWindow.endInclusive)} (estimated ovulation around ${dayMonth(state.ovulationDate)}).",
    )

    fun confidence(confidence: Confidence): String = when (confidence) {
        Confidence.PROVISIONAL -> tr(
            "Estimation provisoire : elle s'affinera dès vos prochains cycles.",
            "Provisional estimate: it will get more precise with your next cycles.",
        )
        Confidence.LOW -> tr(
            "Estimation encore approximative : elle s'appuie sur peu de cycles.",
            "Still a rough estimate: it is based on only a few cycles.",
        )
        Confidence.GOOD -> tr("Estimation fondée sur vos derniers cycles.", "Estimate based on your recent cycles.")
    }
}
