package fr.lunaison.ui

import fr.lunaison.core.cycle.AlertKind
import fr.lunaison.core.cycle.Anticipation
import fr.lunaison.core.cycle.CycleAlerts
import fr.lunaison.core.cycle.CycleAnalyzer
import fr.lunaison.core.cycle.CycleInsights
import fr.lunaison.core.cycle.CycleRecap
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
    /** Bilan du cycle qui vient de finir, tant qu'il n'a pas été masqué. */
    val recap: CycleRecap? = null,
    /** Ce qui revient d'habitude dans la phase qui commence bientôt. */
    val anticipation: Anticipation? = null,
    /** État estimé du cycle d'un ou d'une partenaire suivi(e) par QR Code ou SMS. */
    val followedPartnerCycle: CycleState.Known? = null,
) {
    fun logFor(date: LocalDate): DayLog = logs.firstOrNull { it.date == date } ?: DayLog(date)

    companion object {
        fun compute(logs: List<DayLog>, settings: AppSettings, today: LocalDate): AppState {
            val cycle = CycleAnalyzer.analyze(logs, settings.baseline, today)
            val known = cycle as? CycleState.Known
            val alerts = if (settings.pausePredictions) {
                emptyList()
            } else {
                known
                    ?.let { CycleAlerts.evaluate(it, settings.baseline.ageGroup) }
                    .orEmpty()
                    .filterNot { settings.irregularCyclesMode && (it == AlertKind.IRREGULAR || it == AlertKind.CYCLE_LENGTH) }
                    .filterNot { settings.isDismissed(it, today) }
            }
            val recap = if (settings.pausePredictions) {
                null
            } else {
                known
                    ?.let { CycleInsights.recap(logs, it, settings.baseline.ageGroup) }
                    ?.takeIf { it.record.start != settings.recapSeen }
            }
            val anticipation = if (settings.pausePredictions) null else known?.let { CycleInsights.anticipation(logs, it) }
            // Un snapshot persisté illisible ne doit jamais provoquer une boucle de plantage au démarrage.
            val followedPartnerCycle = settings.followedPartner?.let { snap ->
                runCatching {
                    if (!snap.isPlausible(today)) return@runCatching null
                    (CycleAnalyzer.analyze(snap.toDayLogs(), snap.toBaseline(), today) as? CycleState.Known)
                        ?.takeIf { known ->
                            known.dayOfCycle in 1..365 && !known.lastStart.isBefore(today.minusYears(3)) && !known.lastStart.isAfter(today)
                        }
                }.getOrNull()
            }
            return AppState(today, logs, settings, cycle, alerts, recap, anticipation, followedPartnerCycle)
        }
    }
}
