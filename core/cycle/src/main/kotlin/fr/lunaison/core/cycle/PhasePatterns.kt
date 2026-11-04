package fr.lunaison.core.cycle

import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Mood
import fr.lunaison.core.model.Symptom

/** Ce qui revient souvent pendant une phase, d'après les cycles déjà terminés. */
data class PhasePattern(
    val phase: Phase,
    val cycles: Int,
    val symptoms: List<Pair<Symptom, Int>>,
    val moods: List<Pair<Mood, Int>>,
) {
    val isEmpty: Boolean get() = symptoms.isEmpty() && moods.isEmpty()
}

object PhasePatterns {

    /** Un élément est retenu s'il a été noté pendant cette phase dans au moins deux cycles différents. */
    const val MIN_CYCLES = 2

    fun compute(logs: List<DayLog>, state: CycleState.Known, phase: Phase, limit: Int = 3): PhasePattern {
        val symptomCycles = mutableMapOf<Symptom, MutableSet<Int>>()
        val moodCycles = mutableMapOf<Mood, MutableSet<Int>>()
        val completed = state.cycles.filter { it.isPlausible }

        completed.forEachIndexed { index, cycle ->
            val period = state.periods.firstOrNull { it.start == cycle.start }
            logs.filter { CycleAnalyzer.phaseInCycle(it.date, cycle, period) == phase }.forEach { log ->
                log.symptoms.forEach { symptomCycles.getOrPut(it) { mutableSetOf() } += index }
                log.moods.forEach { moodCycles.getOrPut(it) { mutableSetOf() } += index }
            }
        }

        fun <T> top(map: Map<T, Set<Int>>): List<Pair<T, Int>> = map
            .map { (key, cycles) -> key to cycles.size }
            .filter { it.second >= MIN_CYCLES }
            .sortedByDescending { it.second }
            .take(limit)

        return PhasePattern(phase, completed.size, top(symptomCycles), top(moodCycles))
    }
}
