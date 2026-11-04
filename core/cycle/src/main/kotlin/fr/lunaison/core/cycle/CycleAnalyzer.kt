package fr.lunaison.core.cycle

import fr.lunaison.core.model.Baseline
import fr.lunaison.core.model.DayLog
import java.time.LocalDate
import kotlin.math.ceil

/** Phase du cycle, toujours estimée : seules les règles sont observées directement. */
enum class Phase {
    MENSTRUAL,
    FOLLICULAR,
    OVULATION,
    LUTEAL,

    /** Les règles auraient dû commencer : la phase n'est plus estimable. */
    LATE,
}

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
            recent.size == 1 -> if (usual != null) Math.round((recent[0] + usual) / 2.0).toInt() else recent[0]
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

        val dayOfCycle = daysBetween(lastStart, today) + 1
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
