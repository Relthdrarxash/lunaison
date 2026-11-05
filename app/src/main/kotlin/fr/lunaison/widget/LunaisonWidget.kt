package fr.lunaison.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import fr.lunaison.MainActivity
import fr.lunaison.R
import fr.lunaison.core.cycle.CycleAnalyzer
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.data.LunaisonRepository
import fr.lunaison.core.data.LunaisonRepositoryProvider
import fr.lunaison.core.model.tr
import fr.lunaison.ui.Format
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Widget d'accueil sobre et discret : affiche le jour du cycle et une estimation courte,
 * ou reste neutre si le verrouillage par code PIN ou la pause des prévisions est activé.
 */
class LunaisonWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        val repository = (context.applicationContext as? LunaisonRepositoryProvider)?.repository
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if (repository != null) {
                    renderWidgets(context, repository, appWidgetManager, appWidgetIds)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        suspend fun updateAll(context: Context, repository: LunaisonRepository) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, LunaisonWidget::class.java))
            if (ids.isNotEmpty()) {
                renderWidgets(context, repository, manager, ids)
            }
        }

        private suspend fun renderWidgets(
            context: Context,
            repository: LunaisonRepository,
            manager: AppWidgetManager,
            ids: IntArray,
        ) {
            val settings = repository.settingsOnce()
            val logs = repository.logsOnce()
            val today = LocalDate.now()
            val state = CycleAnalyzer.analyze(logs, settings.baseline, today)

            val (title, subtitle) = when {
                settings.pinHash != null -> "Lunaison" to tr("Toucher pour ouvrir", "Tap to open")
                settings.pausePredictions -> tr("Lunaison : en pause", "Lunaison: paused") to tr("Noter ma journée", "Log my day")
                state is CycleState.Known -> {
                    val head = tr("Jour ${state.dayOfCycle} · ${Format.phaseName(state.phase)}", "Day ${state.dayOfCycle} · ${Format.phaseName(state.phase)}")
                    val sub = when {
                        state.inPeriod -> tr("Règles en cours", "Period in progress")
                        state.daysLate > 0 -> tr("${Format.days(state.daysLate)} de retard", "${Format.days(state.daysLate)} late")
                        state.daysUntilNext <= 1 -> tr("Règles très bientôt", "Period very soon")
                        else -> tr("Prochaines règles vers le ${Format.dayMonth(state.next.start)}", "Next period around ${Format.dayMonth(state.next.start)}")
                    }
                    head to sub
                }
                else -> "Lunaison" to tr("Noter mes dernières règles", "Log my last period")
            }

            val launchIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.widget_lunaison).apply {
                    setTextViewText(R.id.widget_title, title)
                    setTextViewText(R.id.widget_subtitle, subtitle)
                    setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                }
                manager.updateAppWidget(id, views)
            }
        }
    }
}
