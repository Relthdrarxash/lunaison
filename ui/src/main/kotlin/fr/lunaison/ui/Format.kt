package fr.lunaison.ui

import fr.lunaison.core.cycle.Confidence
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.Phase
import fr.lunaison.core.model.Language
import fr.lunaison.core.model.tr
import kotlinx.datetime.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import fr.lunaison.core.model.minusDays

/** Mise en forme des dates et des phrases de prévision, dans la langue de l'appli. */
object Format {
    private val french = Formatters(Locale.FRENCH, "d MMMM", "EEEE d MMMM", "MMMM yyyy")
    private val english = Formatters(Locale.ENGLISH, "MMMM d", "EEEE, MMMM d", "MMMM yyyy")
    private val polish = Formatters(Locale.forLanguageTag("pl"), "d MMMM", "EEEE, d MMMM", "LLLL yyyy")
    private val ukrainian = Formatters(Locale.forLanguageTag("uk"), "d MMMM", "EEEE, d MMMM", "LLLL yyyy")
    private val spanish = Formatters(Locale.forLanguageTag("es"), "d 'de' MMMM", "EEEE, d 'de' MMMM", "MMMM yyyy")
    private val german = Formatters(Locale.GERMAN, "d. MMMM", "EEEE, d. MMMM", "MMMM yyyy")

    private class Formatters(
        locale: Locale,
        dayMonth: String,
        weekday: String,
        monthYear: String,
        dayOfWeek: String = "EEEE",
    ) {
        val dayMonth: DateTimeFormatter = DateTimeFormatter.ofPattern(dayMonth, locale)
        val weekday: DateTimeFormatter = DateTimeFormatter.ofPattern(weekday, locale)
        val monthYear: DateTimeFormatter = DateTimeFormatter.ofPattern(monthYear, locale)
        val dayOfWeek: DateTimeFormatter = DateTimeFormatter.ofPattern(dayOfWeek, locale)
        /** Initiale autonome du jour (L, M, M… / M, T, W…), données CLDR de la langue. */
        val narrowDay: DateTimeFormatter = DateTimeFormatter.ofPattern("ccccc", locale)
    }

    private val current: Formatters
        get() = when (Language.current()) {
            Language.FR -> french
            Language.EN -> english
            Language.PL -> polish
            Language.UK -> ukrainian
            Language.ES -> spanish
            Language.DE -> german
        }

    fun narrowWeekday(date: LocalDate): String = date.toJava().format(current.narrowDay).uppercase()

    fun dayMonth(date: LocalDate): String = date.toJava().format(current.dayMonth)

    fun dayOfWeek(date: LocalDate): String = date.toJava().format(current.dayOfWeek).replaceFirstChar { it.uppercase() }

    fun weekday(date: LocalDate): String = date.toJava().format(current.weekday).replaceFirstChar { it.uppercase() }

    fun monthYear(date: LocalDate): String = date.toJava().format(current.monthYear).replaceFirstChar { it.uppercase() }

    fun days(count: Int): String = if (count <= 1) {
        tr(
            fr = "$count jour",
            en = "$count day",
            pl = "$count dzień",
            uk = "$count день",
            es = "$count día",
            de = "$count Tag",
        )
    } else {
        tr(
            fr = "$count jours",
            en = "$count days",
            pl = "$count dni",
            uk = "$count дн.",
            es = "$count días",
            de = "$count Tage",
        )
    }

    fun relativeDay(date: LocalDate, today: LocalDate): String = when (date) {
        today -> tr("Aujourd'hui", "Today", "Dzisiaj", "Сьогодні", "Hoy", "Heute")
        today.minusDays(1) -> tr("Hier", "Yesterday", "Wczoraj", "Вчора", "Ayer", "Gestern")
        else -> weekday(date)
    }

    fun phaseName(phase: Phase): String = when (phase) {
        Phase.MENSTRUAL -> tr("Règles", "Period", "Miesiączka", "Менструація", "Menstruación", "Periode")
        Phase.FOLLICULAR -> tr("Phase folliculaire", "Follicular phase", "Faza folikularna", "Фолікулярна фаза", "Fase folicular", "Follikelphase")
        Phase.OVULATION -> tr("Autour de l'ovulation", "Around ovulation", "Wokół owulacji", "Навколо овуляції", "En torno a la ovulación", "Rund um den Eisprung")
        Phase.LUTEAL -> tr("Phase lutéale", "Luteal phase", "Faza lutealna", "Лютеїнова фаза", "Fase lútea", "Lutealphase")
        Phase.LATE -> tr("Règles en retard", "Period late", "Spóźniona miesiączka", "Затримка менструації", "Retraso menstrual", "Periode verspätet")
    }

    /** Phrase principale de prévision, sans fausse précision. */
    fun nextPeriod(state: CycleState.Known): String {
        val next = state.next
        return when {
            state.inPeriod -> tr(
                fr = "Règles en cours, jour ${state.dayOfCycle}.",
                en = "Period in progress, day ${state.dayOfCycle}.",
                pl = "Trwa miesiączka, dzień ${state.dayOfCycle}.",
                uk = "Триває менструація, день ${state.dayOfCycle}.",
                es = "Menstruación en curso, día ${state.dayOfCycle}.",
                de = "Periode läuft, Tag ${state.dayOfCycle}.",
            )
            state.daysLate > 0 -> tr(
                fr = "Vos règles avaient été estimées vers le ${dayMonth(next.start)}. ${days(state.daysLate)} de retard.",
                en = "Your period was expected around ${dayMonth(next.start)}. ${days(state.daysLate)} late.",
                pl = "Miesiączka była przewidywana około ${dayMonth(next.start)}. Opóźnienie: ${days(state.daysLate)}.",
                uk = "Менструація очікувалася близько ${dayMonth(next.start)}. Затримка: ${days(state.daysLate)}.",
                es = "Se estimaba hacia el ${dayMonth(next.start)}. ${days(state.daysLate)} de retraso.",
                de = "Periode wurde um den ${dayMonth(next.start)} erwartet. ${days(state.daysLate)} überfällig.",
            )
            state.daysUntilNext <= 0 -> tr(
                fr = "Vos règles peuvent arriver d'un jour à l'autre.",
                en = "Your period may arrive any day now.",
                pl = "Miesiączka może pojawić się lada dzień.",
                uk = "Менструація може початися з дня на день.",
                es = "Tu menstruación puede llegar en cualquier momento.",
                de = "Deine Periode kann jeden Tag einsetzen.",
            )
            state.daysUntilNext == 1 -> tr(
                fr = "Prochaines règles probablement demain.",
                en = "Next period probably tomorrow.",
                pl = "Następna miesiączka prawdopodobnie jutro.",
                uk = "Наступна менструація, ймовірно, завтра.",
                es = "Próxima menstruación probablemente mañana.",
                de = "Nächste Periode wahrscheinlich morgen.",
            )
            else -> tr(
                fr = "Prochaines règles dans ${days(state.daysUntilNext)} environ, vers le ${dayMonth(next.start)}.",
                en = "Next period in about ${days(state.daysUntilNext)}, around ${dayMonth(next.start)}.",
                pl = "Następna miesiączka za około ${days(state.daysUntilNext)}, około ${dayMonth(next.start)}.",
                uk = "Наступна менструація приблизно через ${days(state.daysUntilNext)}, близько ${dayMonth(next.start)}.",
                es = "Próxima menstruación en unos ${days(state.daysUntilNext)}, hacia el ${dayMonth(next.start)}.",
                de = "Nächste Periode in etwa ${days(state.daysUntilNext)}, um den ${dayMonth(next.start)}.",
            )
        }
    }

    fun range(state: CycleState.Known): String = tr(
        fr = "Plage probable : du ${dayMonth(state.next.earliest)} au ${dayMonth(state.next.latest)}.",
        en = "Likely range: ${dayMonth(state.next.earliest)} to ${dayMonth(state.next.latest)}.",
        pl = "Prawdopodobny przedział: od ${dayMonth(state.next.earliest)} do ${dayMonth(state.next.latest)}.",
        uk = "Ймовірний проміжок: з ${dayMonth(state.next.earliest)} до ${dayMonth(state.next.latest)}.",
        es = "Rango probable: del ${dayMonth(state.next.earliest)} al ${dayMonth(state.next.latest)}.",
        de = "Wahrscheinlicher Zeitraum: ${dayMonth(state.next.earliest)} bis ${dayMonth(state.next.latest)}.",
    )

    fun fertileWindow(state: CycleState.Known): String = tr(
        fr = "Fenêtre fertile estimée : du ${dayMonth(state.fertileWindow.start)} au ${dayMonth(state.fertileWindow.endInclusive)} (ovulation estimée vers le ${dayMonth(state.ovulationDate)}).",
        en = "Estimated fertile window: ${dayMonth(state.fertileWindow.start)} to ${dayMonth(state.fertileWindow.endInclusive)} (estimated ovulation around ${dayMonth(state.ovulationDate)}).",
        pl = "Szacowane dni płodne: od ${dayMonth(state.fertileWindow.start)} do ${dayMonth(state.fertileWindow.endInclusive)} (szacowana owulacja około ${dayMonth(state.ovulationDate)}).",
        uk = "Орієнтовне фертильне вікно: з ${dayMonth(state.fertileWindow.start)} до ${dayMonth(state.fertileWindow.endInclusive)} (овуляція близько ${dayMonth(state.ovulationDate)}).",
        es = "Ventana fértil estimada: del ${dayMonth(state.fertileWindow.start)} al ${dayMonth(state.fertileWindow.endInclusive)} (ovulación estimada hacia el ${dayMonth(state.ovulationDate)}).",
        de = "Geschätztes fruchtbares Fenster: ${dayMonth(state.fertileWindow.start)} bis ${dayMonth(state.fertileWindow.endInclusive)} (geschätzter Eisprung um den ${dayMonth(state.ovulationDate)}).",
    )

    fun confidence(confidence: Confidence): String = when (confidence) {
        Confidence.PROVISIONAL -> tr(
            fr = "Estimation provisoire : elle s'affinera dès vos prochains cycles.",
            en = "Provisional estimate: it will get more precise with your next cycles.",
            pl = "Szacunek wstępny: stanie się dokładniejszy z kolejnymi cyklami.",
            uk = "Попередня оцінка: вона стане точнішою з наступними циклами.",
            es = "Estimación provisional: se afinará con tus próximos ciclos.",
            de = "Vorläufige Schätzung: wird mit den nächsten Zyklen genauer.",
        )
        Confidence.LOW -> tr(
            fr = "Estimation encore approximative : elle s'appuie sur peu de cycles.",
            en = "Still a rough estimate: it is based on only a few cycles.",
            pl = "Szacunek orientacyjny: opiera się na niewielu cyklach.",
            uk = "Ще приблизна оцінка: базується лише на кількох циклах.",
            es = "Estimación aún aproximada: se basa en pocos ciclos.",
            de = "Noch ungefähre Schätzung: basiert auf wenigen Zyklen.",
        )
        Confidence.GOOD -> tr(
            fr = "Estimation fondée sur vos derniers cycles.",
            en = "Estimate based on your recent cycles.",
            pl = "Szacunek oparty na Twoich ostatnich cyklach.",
            uk = "Оцінка на основі ваших останніх циклів.",
            es = "Estimación basada en tus últimos ciclos.",
            de = "Schätzung auf Basis deiner letzten Zyklen.",
        )
    }
}
