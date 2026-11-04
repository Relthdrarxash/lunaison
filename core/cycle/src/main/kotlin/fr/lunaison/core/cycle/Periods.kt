package fr.lunaison.core.cycle

import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.Pain
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Des règles : du premier jour de vrai saignement au dernier jour de flux noté. */
data class Period(
    val start: LocalDate,
    val end: LocalDate,
    val days: List<DayLog>,
) {
    val length: Int get() = daysBetween(start, end) + 1
    val heavyDays: Int get() = days.count { it.flow == Flow.HEAVY }
    val hasSeverePain: Boolean get() = days.any { it.pain == Pain.SEVERE }

    operator fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)
}

/** Un cycle complet : d'un premier jour de règles au suivant. */
data class Cycle(val start: LocalDate, val nextStart: LocalDate) {
    val length: Int get() = daysBetween(start, nextStart)
    val isPlausible: Boolean get() = length in CycleConfig.MIN_PLAUSIBLE_CYCLE..CycleConfig.MAX_PLAUSIBLE_CYCLE
}

/** Résultat du repérage : les règles et les épisodes de traces isolées. */
data class Detection(val periods: List<Period>, val spotting: List<LocalDate>)

object PeriodDetector {

    /**
     * Regroupe les jours avec flux. Des jours proches (au plus deux jours sans saisie entre eux) forment un épisode.
     * Un épisode qui contient au moins un vrai saignement donne des règles, qui commencent au premier saignement :
     * les traces qui précèdent ne comptent pas. Un épisode de traces seules est un saignement intermédiaire.
     */
    fun detect(logs: List<DayLog>): Detection {
        val flowDays = logs.filter { it.flow != null }.sortedBy { it.date }
        val groups = mutableListOf<MutableList<DayLog>>()
        for (day in flowDays) {
            val current = groups.lastOrNull()
            if (current != null && daysBetween(current.last().date, day.date) <= CycleConfig.MAX_GAP_IN_PERIOD) {
                current += day
            } else {
                groups += mutableListOf(day)
            }
        }
        val periods = mutableListOf<Period>()
        val spotting = mutableListOf<LocalDate>()
        for (group in groups) {
            val firstBleeding = group.indexOfFirst { it.flow?.isBleeding == true }
            if (firstBleeding < 0) {
                spotting += group.first().date
            } else {
                val days = group.drop(firstBleeding)
                periods += Period(start = days.first().date, end = days.last().date, days = days)
            }
        }
        return Detection(periods, spotting)
    }
}

internal fun daysBetween(from: LocalDate, to: LocalDate): Int = ChronoUnit.DAYS.between(from, to).toInt()

internal fun median(values: List<Int>): Int {
    require(values.isNotEmpty())
    val sorted = values.sorted()
    val middle = sorted.size / 2
    return if (sorted.size % 2 == 1) sorted[middle] else Math.round((sorted[middle - 1] + sorted[middle]) / 2.0).toInt()
}

internal fun standardDeviation(values: List<Int>): Double {
    if (values.size < 2) return 0.0
    val mean = values.average()
    return Math.sqrt(values.sumOf { (it - mean) * (it - mean) } / (values.size - 1))
}
