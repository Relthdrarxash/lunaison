package fr.lunaison.core.cycle

import fr.lunaison.core.model.AgeGroup
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Pain
import java.time.LocalDate
import kotlin.math.abs

/** Un cycle terminé, tel que la personne l'a noté. */
data class CycleRecord(
    val start: LocalDate,
    val length: Int,
    /** Durée des règles qui ouvrent ce cycle, si elles ont été notées. */
    val periodLength: Int?,
    /** Jours de ces règles avec une douleur « Gênante » ou « Forte ». */
    val painDays: Int,
    /** Durée que Lunaison aurait prévue avec les cycles précédents, s'il y en avait au moins deux. */
    val expectedLength: Int?,
) {
    /** Positif : règles arrivées après la date estimée. Négatif : avant. */
    val predictionError: Int? get() = expectedLength?.let { length - it }
}

/** Vue d'ensemble pour l'écran « Mes cycles ». */
data class CycleOverview(
    /** Les derniers cycles terminés, du plus ancien au plus récent. */
    val records: List<CycleRecord>,
    val usualLength: Int,
    val shortest: Int,
    val longest: Int,
    val usualPeriodLength: Int?,
    /** Durées et écart entre cycles dans les repères FIGO (ou ACOG avant 18 ans). */
    val withinReferences: Boolean,
    /** Écart habituel, en jours, entre la date estimée et l'arrivée des règles. */
    val typicalPredictionError: Int?,
    val predictionsChecked: Int,
)

/** Bilan du cycle qui vient de se terminer, affiché au début du suivant. */
data class CycleRecap(
    val record: CycleRecord,
    /** Durée habituelle des cycles précédents, s'il y en a au moins deux. */
    val usualLength: Int?,
    val withinReferences: Boolean,
    /** Jours douloureux habituels lors des règles précédentes, s'il y en a au moins deux. */
    val usualPainDays: Int?,
) {
    val difference: Int? get() = usualLength?.let { record.length - it }
}

/** La phase qui arrive bientôt, et ce qui y revient d'habitude. */
data class Anticipation(val phase: Phase, val inDays: Int, val pattern: PhasePattern)

/** Moment d'apparition d'un symptôme ou d'une humeur avant les règles. */
data class Onset(val label: String, val daysBefore: Int, val cycles: Int)

/** Synthèse complète des cycles pour une consultation médicale. */
data class HealthReport(
    val generatedOn: LocalDate,
    val lastPeriodStart: LocalDate,
    val dayOfCycle: Int,
    val overview: CycleOverview?,
    val recentPeriods: List<Period>,
    val phasePatterns: List<PhasePattern>,
    val alerts: List<AlertKind>,
    val spottingCountLast90Days: Int,
    val medicationDaysLast30Days: Int,
)

/**
 * Statistiques personnelles, calculées sur le téléphone à partir de ce qui a été noté.
 * Elles décrivent, sans interpréter : pas de diagnostic, des comptes plutôt que des pourcentages,
 * et rien avant deux cycles complets.
 */
object CycleInsights {

    /** Les cycles plausibles terminés, du plus ancien au plus récent. */
    fun records(logs: List<DayLog>, state: CycleState.Known): List<CycleRecord> {
        val byDate = logs.associateBy { it.date }
        val plausible = state.cycles.filter { it.isPlausible }
        return plausible.mapIndexed { index, cycle ->
            val period = state.periods.firstOrNull { it.start == cycle.start }
            val previous = plausible.subList(0, index).takeLast(CycleConfig.CYCLES_FOR_PREDICTION).map { it.length }
            val painDays = period?.let { p ->
                generateSequence(p.start) { it.plusDays(1) }.takeWhile { !it.isAfter(p.end) }
                    .count { byDate[it]?.pain == Pain.MODERATE || byDate[it]?.pain == Pain.SEVERE }
            } ?: 0
            CycleRecord(
                start = cycle.start,
                length = cycle.length,
                periodLength = period?.length,
                painDays = painDays,
                expectedLength = previous.takeIf { it.size >= CycleConfig.MIN_CYCLES_FOR_STATS }?.let(::median),
            )
        }
    }

    fun overview(logs: List<DayLog>, state: CycleState.Known, ageGroup: AgeGroup): CycleOverview? {
        val records = records(logs, state).takeLast(CycleConfig.CYCLES_FOR_PREDICTION)
        if (records.size < CycleConfig.MIN_CYCLES_FOR_STATS) return null
        val lengths = records.map { it.length }
        val errors = records.mapNotNull { it.predictionError }
        return CycleOverview(
            records = records,
            usualLength = median(lengths),
            shortest = lengths.min(),
            longest = lengths.max(),
            usualPeriodLength = records.mapNotNull { it.periodLength }.takeIf { it.isNotEmpty() }?.let(::median),
            withinReferences = withinReferences(lengths, ageGroup),
            typicalPredictionError = errors.takeIf { it.size >= CycleConfig.MIN_CHECKED_PREDICTIONS }?.map { abs(it) }?.let(::median),
            predictionsChecked = errors.size,
        )
    }

    /** Le bilan n'existe que pendant les premiers jours d'un cycle, pour le cycle qui vient de finir. */
    fun recap(logs: List<DayLog>, state: CycleState.Known, ageGroup: AgeGroup): CycleRecap? {
        if (state.dayOfCycle > CycleConfig.RECAP_DAYS) return null
        val records = records(logs, state)
        val last = records.lastOrNull()?.takeIf { daysBetween(it.start, state.lastStart) == it.length } ?: return null
        val previous = records.dropLast(1).takeLast(CycleConfig.CYCLES_FOR_PREDICTION)
        val enough = previous.size >= CycleConfig.MIN_CYCLES_FOR_STATS
        return CycleRecap(
            record = last,
            usualLength = if (enough) median(previous.map { it.length }) else null,
            withinReferences = last.length in CycleConfig.normalCycleRange(ageGroup) &&
                (last.periodLength ?: 0) <= CycleConfig.MAX_NORMAL_PERIOD,
            usualPainDays = previous.filter { it.periodLength != null }
                .takeIf { it.size >= CycleConfig.MIN_CYCLES_FOR_STATS }
                ?.let { list -> median(list.map { it.painDays }) },
        )
    }

    /**
     * La phase suivante, si elle commence dans les prochains jours et qu'un motif y revient.
     * Rien tant que la prévision est provisoire ou que les règles sont en retard.
     */
    fun anticipation(logs: List<DayLog>, state: CycleState.Known): Anticipation? {
        if (state.stats.confidence == Confidence.PROVISIONAL || state.phase == Phase.LATE) return null
        for (offset in 1..CycleConfig.ANTICIPATION_DAYS) {
            val date = state.today.plusDays(offset.toLong())
            val phase = if (!date.isBefore(state.next.start)) Phase.MENSTRUAL else CycleAnalyzer.phaseBefore(date, state.next.start)
            if (phase != state.phase) {
                if (state.inPeriod && phase != Phase.MENSTRUAL) return null
                val pattern = PhasePatterns.compute(logs, state, phase)
                return if (pattern.isEmpty) null else Anticipation(phase, offset, pattern)
            }
        }
        return null
    }

    /**
     * Pour ce qui revient en fin de cycle : combien de jours avant les règles cela commence d'habitude.
     * On compte à rebours depuis les règles suivantes, parce que la fin du cycle est la partie la plus stable.
     */
    fun onsets(logs: List<DayLog>, state: CycleState.Known): List<Onset> {
        val pattern = PhasePatterns.compute(logs, state, Phase.LUTEAL)
        if (pattern.isEmpty) return emptyList()
        val tracked = logs.filter { it.isTracked }
        val cycles = state.cycles.filter { it.isPlausible }.takeLast(CycleConfig.CYCLES_FOR_PREDICTION)

        fun onset(label: String, matches: (DayLog) -> Boolean): Onset? {
            val values = cycles.mapNotNull { cycle ->
                val period = state.periods.firstOrNull { it.start == cycle.start }
                tracked.filter { CycleAnalyzer.phaseInCycle(it.date, cycle, period) == Phase.LUTEAL && matches(it) }
                    .minByOrNull { it.date }
                    ?.let { daysBetween(it.date, cycle.nextStart) }
            }
            return if (values.size >= CycleConfig.MIN_CYCLES_FOR_STATS) Onset(label, median(values), values.size) else null
        }

        return pattern.symptoms.mapNotNull { (symptom, _) -> onset(symptom.label) { symptom in it.symptoms } } +
            pattern.moods.mapNotNull { (mood, _) -> onset(mood.label) { mood in it.moods } }
    }

    /** Schémas corporels et d'humeur sur les quatre phases du cycle. */
    fun allPhasePatterns(logs: List<DayLog>, state: CycleState.Known): List<PhasePattern> =
        listOf(Phase.MENSTRUAL, Phase.FOLLICULAR, Phase.OVULATION, Phase.LUTEAL)
            .map { PhasePatterns.compute(logs, state, it) }
            .filter { !it.isEmpty }

    /** Construit le bilan de santé synthétique à montrer ou partager en consultation. */
    fun healthReport(logs: List<DayLog>, state: CycleState.Known, ageGroup: AgeGroup): HealthReport {
        val overview = overview(logs, state, ageGroup)
        val alerts = CycleAlerts.evaluate(state, ageGroup)
        val cutoff90 = state.today.minusDays(90)
        val cutoff30 = state.today.minusDays(30)
        return HealthReport(
            generatedOn = state.today,
            lastPeriodStart = state.lastStart,
            dayOfCycle = state.dayOfCycle,
            overview = overview,
            recentPeriods = state.periods.takeLast(CycleConfig.CYCLES_FOR_PREDICTION),
            phasePatterns = allPhasePatterns(logs, state),
            alerts = alerts,
            spottingCountLast90Days = state.spotting.count { !it.isBefore(cutoff90) },
            medicationDaysLast30Days = logs.count { !it.date.isBefore(cutoff30) && !it.date.isAfter(state.today) && it.medicationTaken },
        )
    }

    private fun withinReferences(lengths: List<Int>, ageGroup: AgeGroup): Boolean {
        val range = CycleConfig.normalCycleRange(ageGroup)
        val inRange = lengths.all { it in range }
        // À l'adolescence, la variabilité d'un cycle à l'autre est habituelle (ACOG, 2015).
        val regular = ageGroup == AgeGroup.UNDER_18 || lengths.max() - lengths.min() <= CycleConfig.MAX_NORMAL_VARIATION
        return inRange && regular
    }
}
