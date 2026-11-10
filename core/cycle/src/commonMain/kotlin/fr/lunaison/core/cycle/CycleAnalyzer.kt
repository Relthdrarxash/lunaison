package fr.lunaison.core.cycle

import fr.lunaison.core.model.Baseline
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.tr
import kotlinx.datetime.LocalDate
import kotlin.math.ceil
import fr.lunaison.core.model.isAfter
import fr.lunaison.core.model.isBefore
import fr.lunaison.core.model.minusDays
import fr.lunaison.core.model.plusDays
import kotlin.math.roundToInt

/** Phase du cycle, toujours estimée : seules les règles sont observées directement. */
enum class Phase {
    MENSTRUAL,
    FOLLICULAR,
    OVULATION,
    LUTEAL,

    /** Les règles auraient dû commencer : la phase n'est plus estimable. */
    LATE,
}

/** Probabilité estimée de grossesse / fertilité au jour le jour, selon les repères cliniques (Wilcox, Trussell). */
enum class PregnancyOdds(
    private val fr: String,
    private val en: String,
    private val pl: String,
    private val uk: String,
    private val es: String,
    private val de: String,
    private val frDescription: String,
    private val enDescription: String,
    private val plDescription: String,
    private val ukDescription: String,
    private val esDescription: String,
    private val deDescription: String,
) {
    VERY_LOW(
        "Très faibles", "Very low", "Bardzo niskie", "Дуже низькі", "Muy bajas", "Sehr gering",
        "Période peu propice à la conception : ovule absent ou non viable (survie max de 24 h).",
        "Unfavorable window for conception: egg not present or no longer viable (max 24h survival).",
        "Okres mało sprzyjający poczęciu: brak komórki jajowej lub brak jej żywotności.",
        "Період з низькою ймовірністю зачаття: яйцеклітини немає або вона не життєздатна.",
        "Periodo poco propicio para la concepción: óvulo ausente o no viable.",
        "Wenig fruchtbare Phase: Eizelle nicht vorhanden oder nicht mehr lebensfähig.",
    ),
    LOW(
        "Faibles", "Low", "Niskie", "Низькі", "Bajas", "Gering",
        "Encore à distance de l'ovulation, mais les spermatozoïdes peuvent parfois survivre jusqu'à 5 jours.",
        "Still distant from ovulation, though sperm can sometimes survive up to 5 days.",
        "Wciąż daleko od owulacji, ale plemniki mogą przeżyć do 5 dni.",
        "До овуляції ще є час, але сперматозоїди можуть жити до 5 днів.",
        "Aún lejos de la ovulación, pero los espermatozoides pueden sobrevivir hasta 5 días.",
        "Noch vor dem fruchtbaren Fenster, Spermien können jedoch bis zu 5 Tage überleben.",
    ),
    MEDIUM(
        "Modérées", "Moderate", "Umiarkowane", "Помірні", "Medias", "Mäßig",
        "Entrée dans la fenêtre fertile : rapport susceptible de conduire à une fécondation.",
        "Entering the fertile window: intercourse may lead to fertilization.",
        "Początek okna płodnego: stosunek może prowadzić do zapłodnienia.",
        "Початок плідного вікна: статевий акт може призвести до запліднення.",
        "Entrada en la ventana fértil: las relaciones pueden llevar a la fecundación.",
        "Eintritt ins fruchtbare Fenster: Befruchtung ist möglich.",
    ),
    HIGH(
        "Élevées", "High", "Wysokie", "Високі", "Altas", "Hoch",
        "Période très favorable : l'ovulation est attendue dans les 24 à 48 heures.",
        "Highly favorable window: ovulation expected within 24 to 48 hours.",
        "Bardzo korzystny czas: owulacja spodziewana w ciągu 24 do 48 godzin.",
        "Сприятливий час: овуляція очікується протягом 24-48 годин.",
        "Periodo muy favorable: la ovulación se espera en 24 a 48 horas.",
        "Sehr günstige Phase: Eisprung wird in 24 bis 48 Stunden erwartet.",
    ),
    PEAK(
        "Maximales", "Peak", "Szczytowe", "Максимальні", "Máximas", "Spitzenwert",
        "Jour estimé de l'ovulation : probabilité de conception au plus haut du cycle.",
        "Estimated ovulation day: highest conception probability of the cycle.",
        "Szacowany dzień owulacji: najwyższe prawdopodobieństwo poczęcia w cyklu.",
        "Очікуваний день овуляції: найвища ймовірність зачаття в циклі.",
        "Día estimado de la ovulación: máxima probabilidad de concepción del ciclo.",
        "Geschätzter Eisprungtag: höchste Befruchtungswahrscheinlichkeit im Zyklus.",
    ),
    ;

    val label: String get() = tr(fr, en, pl, uk, es, de)
    val description: String get() = tr(frDescription, enDescription, plDescription, ukDescription, esDescription, deDescription)
}

/** Prédiction détaillée pour un jour à venir. */
data class UpcomingDayForecast(
    val date: LocalDate,
    val dayOfCycle: Int,
    val phase: Phase,
    val odds: PregnancyOdds,
    val inLikelyPeriod: Boolean,
)

/** Fiabilité de la prévision, selon l'historique disponible. */
enum class Confidence {
    /** Aucun cycle complet noté : on s'appuie sur les réponses du démarrage. */
    PROVISIONAL,

    /** Un ou deux cycles complets. */
    LOW,

    /** Trois cycles complets ou plus. */
    GOOD,
}

/** Prochaines règles : une date estimée et une plage plausible autour. */
data class Prediction(
    val start: LocalDate,
    val earliest: LocalDate,
    val latest: LocalDate,
    val periodLength: Int,
) {
    operator fun contains(date: LocalDate): Boolean = !date.isBefore(earliest) && !date.isAfter(latest)

    /** Jours de règles probables si elles commencent à la date estimée. */
    fun likelyDays(): List<LocalDate> = (0 until periodLength).map { start.plusDays(it.toLong()) }
}

data class CycleStats(
    val cycleLength: Int,
    val periodLength: Int,
    val spread: Int,
    val completedCycles: Int,
    val confidence: Confidence,
)

/** Tout ce que l'accueil, le calendrier et les rappels ont besoin de savoir. */
sealed interface CycleState {

    /** Rien n'est encore connu : ni règles notées, ni date déclarée. */
    data object Unknown : CycleState

    data class Known(
        val today: LocalDate,
        val lastStart: LocalDate,
        val dayOfCycle: Int,
        val phase: Phase,
        val inPeriod: Boolean,
        val next: Prediction,
        /** Jours de retard par rapport à la date estimée, si la plage est dépassée. */
        val daysLate: Int,
        val stats: CycleStats,
        val periods: List<Period>,
        val cycles: List<Cycle>,
        val spotting: List<LocalDate>,
    ) : CycleState {
        val daysUntilNext: Int get() = daysBetween(today, next.start)

        /** Date estimée de l'ovulation (13 jours avant les prochaines règles prévues). */
        val ovulationDate: LocalDate get() = next.start.minusDays(CycleConfig.LUTEAL_DAYS.toLong())

        /** Fenêtre fertile estimée (5 jours avant l'ovulation jusqu'au lendemain). */
        val fertileWindow: ClosedRange<LocalDate> get() = ovulationDate.minusDays(5)..ovulationDate.plusDays(1)

        /** Vrai si aujourd'hui tombe dans la fenêtre fertile estimée. */
        val inFertileWindow: Boolean get() = !inPeriod && daysLate == 0 && today in fertileWindow

        /** Retard significatif au-delà de la plage habituelle (au moins 5 jours après la date estimée). */
        val possiblePregnancyHint: Boolean get() = daysLate >= 5

        /** Probabilité estimée de grossesse pour une date donnée. */
        fun pregnancyOdds(date: LocalDate): PregnancyOdds {
            if (inPeriod && !date.isAfter(lastStart.plusDays(stats.periodLength.toLong()))) {
                return PregnancyOdds.VERY_LOW
            }
            if (daysLate > 0) {
                return PregnancyOdds.LOW
            }
            val daysToOvulation = daysBetween(date, ovulationDate)
            return when {
                daysToOvulation in -1..0 -> PregnancyOdds.PEAK
                daysToOvulation in 1..2 -> PregnancyOdds.HIGH
                daysToOvulation in 3..5 -> PregnancyOdds.MEDIUM
                daysToOvulation < -1 -> PregnancyOdds.VERY_LOW
                else -> PregnancyOdds.LOW
            }
        }

        /** Probabilité estimée de grossesse pour aujourd'hui. */
        val todayPregnancyOdds: PregnancyOdds get() = pregnancyOdds(today)

        /** Prévisions détaillées jour par jour pour les prochains jours. */
        fun upcomingDayForecasts(count: Int = 7): List<UpcomingDayForecast> = (1..count).map { offset ->
            val targetDate = today.plusDays(offset.toLong())
            val targetDayOfCycle = dayOfCycle + offset
            val inLikelyPeriod = targetDate in next.likelyDays()
            val targetPhase = when {
                inLikelyPeriod -> Phase.MENSTRUAL
                daysLate > 0 -> Phase.LATE
                !targetDate.isBefore(next.start) -> Phase.MENSTRUAL
                else -> CycleAnalyzer.phaseBefore(targetDate, next.start)
            }
            val odds = if (inLikelyPeriod) PregnancyOdds.VERY_LOW else pregnancyOdds(targetDate)
            UpcomingDayForecast(
                date = targetDate,
                dayOfCycle = targetDayOfCycle,
                phase = targetPhase,
                odds = odds,
                inLikelyPeriod = inLikelyPeriod,
            )
        }

        /** Prévisions suivantes, pour le calendrier. La marge s'élargit à chaque cycle. */
        fun upcoming(count: Int): List<Prediction> = (0 until count).map { index ->
            val start = next.start.plusDays((index * stats.cycleLength).toLong())
            val spread = (stats.spread + index * 2).coerceAtMost(CycleConfig.MAX_SPREAD + 4).toLong()
            Prediction(start, start.minusDays(spread), start.plusDays(spread), stats.periodLength)
        }
    }
}

object CycleAnalyzer {

    fun analyze(logs: List<DayLog>, baseline: Baseline?, today: LocalDate): CycleState {
        val detection = PeriodDetector.detect(logs.filter { !it.date.isAfter(today) })
        val periods = detection.periods

        // La date déclarée au démarrage ne sert que si aucune règle notée n'est proche d'elle.
        val declared = baseline?.lastPeriodStart?.takeIf { date ->
            !date.isAfter(today) && periods.none { kotlin.math.abs(daysBetween(it.start, date)) <= 10 }
        }
        val starts = (periods.map { it.start } + listOfNotNull(declared)).sorted()
        val lastStart = starts.lastOrNull() ?: return CycleState.Unknown

        val cycles = starts.zipWithNext { a, b -> Cycle(a, b) }
        val recent = cycles.filter { it.isPlausible }.takeLast(CycleConfig.CYCLES_FOR_PREDICTION).map { it.length }
        val usual = baseline?.usualCycleLength?.takeIf { it in CycleConfig.MIN_PLAUSIBLE_CYCLE..CycleConfig.MAX_PLAUSIBLE_CYCLE }

        val cycleLength = when {
            recent.size >= 2 -> median(recent)
            recent.size == 1 -> if (usual != null) ((recent[0] + usual) / 2.0).roundToInt() else recent[0]
            else -> usual ?: CycleConfig.DEFAULT_CYCLE_LENGTH
        }
        val spread = when {
            recent.size >= 3 -> ceil(standardDeviation(recent)).toInt().coerceIn(CycleConfig.MIN_SPREAD, CycleConfig.MAX_SPREAD)
            recent.size == 2 -> 3
            else -> CycleConfig.PROVISIONAL_SPREAD
        }
        val confidence = when {
            recent.size >= 3 -> Confidence.GOOD
            recent.isNotEmpty() -> Confidence.LOW
            else -> Confidence.PROVISIONAL
        }

        val lastPeriod = periods.lastOrNull()?.takeIf { it.start == lastStart }
        // Les règles en cours ne comptent pas dans la durée habituelle : elles ne sont pas finies.
        val finished = periods.filter { it !== lastPeriod || daysBetween(it.end, today) > 1 }
        val periodLength = finished.takeLast(CycleConfig.CYCLES_FOR_PREDICTION).map { it.length }.takeIf { it.isNotEmpty() }?.let(::median)
            ?: baseline?.usualPeriodLength?.coerceIn(1, 10)
            ?: CycleConfig.DEFAULT_PERIOD_LENGTH

        val dayOfCycle = (daysBetween(lastStart, today).toLong() + 1L).coerceIn(1L, 3650L).toInt()
        val inPeriod = when {
            lastPeriod != null -> daysBetween(lastPeriod.end, today) <= 1 && dayOfCycle <= maxOf(periodLength, lastPeriod.length) + 1
            else -> dayOfCycle <= periodLength
        }

        val nextStart = lastStart.plusDays(cycleLength.toLong())
        val next = Prediction(nextStart, nextStart.minusDays(spread.toLong()), nextStart.plusDays(spread.toLong()), periodLength)
        val daysLate = if (today.isAfter(next.latest)) daysBetween(nextStart, today) else 0

        val phase = when {
            inPeriod -> Phase.MENSTRUAL
            daysLate > 0 -> Phase.LATE
            else -> phaseBefore(today, nextStart)
        }

        return CycleState.Known(
            today = today,
            lastStart = lastStart,
            dayOfCycle = dayOfCycle,
            phase = phase,
            inPeriod = inPeriod,
            next = next,
            daysLate = daysLate,
            stats = CycleStats(cycleLength, periodLength, spread, recent.size, confidence),
            periods = periods,
            cycles = cycles,
            spotting = detection.spotting,
        )
    }

    /** Phase d'un jour hors règles, d'après le début des règles suivantes (réelles ou prévues). */
    internal fun phaseBefore(date: LocalDate, nextStart: LocalDate): Phase {
        val ovulation = nextStart.minusDays(CycleConfig.LUTEAL_DAYS.toLong())
        val offset = daysBetween(ovulation, date)
        return when {
            offset < -CycleConfig.OVULATION_WINDOW -> Phase.FOLLICULAR
            offset <= CycleConfig.OVULATION_WINDOW -> Phase.OVULATION
            else -> Phase.LUTEAL
        }
    }

    /** Phase d'un jour passé, dans un cycle terminé dont on connaît les règles. */
    fun phaseInCycle(date: LocalDate, cycle: Cycle, period: Period?): Phase? = when {
        date.isBefore(cycle.start) || !date.isBefore(cycle.nextStart) -> null
        period != null && date in period -> Phase.MENSTRUAL
        period == null && daysBetween(cycle.start, date) < CycleConfig.DEFAULT_PERIOD_LENGTH -> Phase.MENSTRUAL
        else -> phaseBefore(date, cycle.nextStart)
    }
}
