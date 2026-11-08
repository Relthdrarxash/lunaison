package fr.lunaison.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import fr.lunaison.core.data.LunaisonRepositoryProvider
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Actions en un geste depuis une notification : « Mes règles ont commencé » et « C'est pris ».
 * Le reste de la journée reste modifiable dans l'appli. Les rappels et le widget se mettent à jour
 * seuls, puisque l'application observe les journées enregistrées.
 */
class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val repository = (context.applicationContext as? LunaisonRepositoryProvider)?.repository ?: return
        val action = intent.action ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val today = LocalDate.now()
                val log = repository.logsOnce().firstOrNull { it.date == today } ?: DayLog(today)
                when (action) {
                    ACTION_PERIOD_STARTED -> {
                        // Même règle que le bouton de l'accueil : flux moyen par défaut, modifiable ensuite.
                        repository.save(log.copy(flow = log.flow?.takeIf { it.isBleeding } ?: Flow.MEDIUM))
                        NotificationManagerCompat.from(context).cancel(ReminderWorker.notificationId(ReminderKind.PERIOD))
                    }
                    ACTION_MEDICATION_TAKEN -> {
                        repository.save(log.copy(medicationTaken = true))
                        NotificationManagerCompat.from(context).cancel(ReminderWorker.notificationId(ReminderKind.MEDICATION))
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_PERIOD_STARTED = "fr.lunaison.action.PERIOD_STARTED"
        const val ACTION_MEDICATION_TAKEN = "fr.lunaison.action.MEDICATION_TAKEN"
    }
}
