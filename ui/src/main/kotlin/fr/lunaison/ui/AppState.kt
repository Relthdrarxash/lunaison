package fr.lunaison.ui

import fr.lunaison.core.cycle.AlertKind
import fr.lunaison.core.cycle.CycleAlerts
import fr.lunaison.core.cycle.CycleAnalyzer
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.data.AppSettings
import fr.lunaison.core.model.DayLog
import java.time.LocalDate

/** Ce que les écrans affichent, calculé à partir des données enregistrées. */
data class AppState(
    val today: LocalDate,
    val logs: List<DayLog>,
    val settings: AppSettings,
    val cycle: CycleState,
    val alerts: List<AlertKind>,
) {
    fun logFor(date: LocalDate): DayLog = logs.firstOrNull { it.date == date } ?: DayLog(date)

    companion object {
        fun compute(logs: List<DayLog>, settings: AppSettings, today: LocalDate): AppState {
            val cycle = CycleAnalyzer.analyze(logs, settings.baseline, today)
            val alerts = (cycle as? CycleState.Known)
                ?.let { CycleAlerts.evaluate(it, settings.baseline.ageGroup) }
                .orEmpty()
                .filterNot { settings.isDismissed(it, today) }
            return AppState(today, logs, settings, cycle, alerts)
        }
    }
}
