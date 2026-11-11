package fr.lunaison.core.cycle

import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Mood
import fr.lunaison.core.model.Symptom
import fr.lunaison.core.model.isBefore
import fr.lunaison.core.model.plusDays

/**
 * Ce qui revient souvent pendant une phase, d'après les cycles déjà terminés.
 * [cycles] compte les cycles où la phase a été renseignée au moins un jour : c'est la base des
 * « 3 cycles sur 4 » affichés.
 */
data class PhasePattern(
    val phase: Phase,
    val cycles: Int,
    val symptoms: List<Pair<Symptom, Int>>,
    val moods: List<Pair<Mood, Int>>,
) {
    val isEmpty: Boolean get() = symptoms.isEmpty() && moods.isEmpty()

    /** Libellés dans l'ordre d'affichage, symptômes puis humeurs. */
    val labels: List<String> get() = symptoms.map { it.first.label } + moods.map { it.first.label }
}

/** Un jour où la personne a noté son état : un symptôme, une humeur ou une douleur. Le flux seul ne dit rien des symptômes. */
internal val DayLog.isTracked: Boolean get() = symptoms.isNotEmpty() || moods.isNotEmpty() || pain != null

object PhasePatterns {

    /** Un élément est retenu s'il a été noté pendant cette phase dans au moins deux cycles différents. */
    const val MIN_CYCLES = CycleConfig.MIN_CYCLES_FOR_STATS

    /**
     * Trois garde-fous, pour ne pas inventer de tendance :
     * - seuls les jours renseignés comptent, un jour vide n'est pas un jour sans symptôme ;
     * - l'élément doit revenir dans au moins deux cycles, et dans au moins la moitié des cycles renseignés ;
     * - s'il y a assez de jours renseignés ailleurs dans le cycle, il doit être au moins deux fois plus
     *   fréquent dans la phase qu'en dehors. Une fatigue présente tout le mois n'est pas liée à une phase.
     */
    fun compute(logs: List<DayLog>, state: CycleState.Known, phase: Phase, limit: Int = 3): PhasePattern {
        val tracked = logs.filter { it.isTracked }.associateBy { it.date }
        val completed = state.cycles.filter { it.isPlausible }.takeLast(CycleConfig.CYCLES_FOR_PREDICTION)

        val cyclesWith = mutableMapOf<Any, MutableSet<Int>>()
        val daysIn = mutableMapOf<Any, Int>()
        val daysOut = mutableMapOf<Any, Int>()
        var trackedIn = 0
        var trackedOut = 0
        var cyclesTracked = 0

        completed.forEachIndexed { index, cycle ->
            val period = state.periods.firstOrNull { it.start == cycle.start }
            var phaseTracked = false
            var date = cycle.start
            while (date.isBefore(cycle.nextStart)) {
                val log = tracked[date]
                val dayPhase = log?.let { CycleAnalyzer.phaseInCycle(date, cycle, period) }
                if (log != null && dayPhase != null) {
                    val items: Set<Any> = log.symptoms + log.moods
                    if (dayPhase == phase) {
                        phaseTracked = true
                        trackedIn++
                        items.forEach {
                            cyclesWith.getOrPut(it) { mutableSetOf() } += index
                            daysIn[it] = (daysIn[it] ?: 0) + 1
                        }
                    } else {
                        trackedOut++
                        items.forEach { daysOut[it] = (daysOut[it] ?: 0) + 1 }
                    }
                }
                date = date.plusDays(1)
            }
            if (phaseTracked) cyclesTracked++
        }

        fun keep(item: Any): Boolean {
            val cycles = cyclesWith[item]?.size ?: 0
            if (cycles < MIN_CYCLES || cycles * 2 < cyclesTracked) return false
            if (trackedOut < CycleConfig.PATTERN_MIN_DAYS_OUTSIDE) return true
            val rateIn = (daysIn[item] ?: 0).toDouble() / trackedIn
            val rateOut = (daysOut[item] ?: 0).toDouble() / trackedOut
            return rateIn >= CycleConfig.PATTERN_ENRICHMENT * rateOut
        }

        val kept = cyclesWith.keys.filter(::keep)
            .map { it to cyclesWith.getValue(it).size }
            .sortedByDescending { it.second }

        return PhasePattern(
            phase = phase,
            cycles = cyclesTracked,
            symptoms = kept.mapNotNull { (item, n) -> (item as? Symptom)?.let { it to n } }.take(limit),
            moods = kept.mapNotNull { (item, n) -> (item as? Mood)?.let { it to n } }.take(limit),
        )
    }
}
