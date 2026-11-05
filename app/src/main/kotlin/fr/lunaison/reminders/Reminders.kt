package fr.lunaison.reminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import fr.lunaison.R
import fr.lunaison.core.cycle.CycleAnalyzer
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.ReminderPlanner
import fr.lunaison.core.data.LunaisonRepository
import fr.lunaison.core.data.LunaisonRepositoryProvider
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import fr.lunaison.core.model.tr

enum class ReminderKind { PERIOD, LOG }

/**
 * Rappels locaux, planifiés avec WorkManager : chaque rappel est une tâche unique,
 * replanifiée après chaque envoi et à chaque changement des données ou des réglages.
 */
object ReminderScheduler {

    private fun workName(kind: ReminderKind) = "rappel-${kind.name.lowercase()}"

    suspend fun reschedule(context: Context, repository: LunaisonRepository) {
        val settings = repository.settingsOnce()
        val now = LocalDateTime.now()
        if (!settings.onboarded) {
            ReminderKind.entries.forEach { WorkManager.getInstance(context).cancelUniqueWork(workName(it)) }
            return
        }
        val state = CycleAnalyzer.analyze(repository.logsOnce(), settings.baseline, now.toLocalDate())
        plan(context, ReminderKind.PERIOD, ReminderPlanner.nextPeriodReminder(state, settings.reminders, now), now)
        plan(context, ReminderKind.LOG, ReminderPlanner.nextLogReminder(settings.reminders, now), now)
    }

    private fun plan(context: Context, kind: ReminderKind, at: LocalDateTime?, now: LocalDateTime) {
        val manager = WorkManager.getInstance(context)
        if (at == null) {
            manager.cancelUniqueWork(workName(kind))
            return
        }
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(Duration.between(now, at))
            .setInputData(workDataOf(ReminderWorker.KIND to kind.name))
            .build()
        manager.enqueueUniqueWork(workName(kind), ExistingWorkPolicy.REPLACE, request)
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as? LunaisonRepositoryProvider)?.repository ?: return Result.failure()
        val kind = inputData.getString(KIND)?.let { name -> ReminderKind.entries.firstOrNull { it.name == name } } ?: return Result.failure()
        val settings = repository.settingsOnce()
        val logs = repository.logsOnce()
        val today = LocalDate.now()
        val state = CycleAnalyzer.analyze(logs, settings.baseline, today)

        when (kind) {
            ReminderKind.PERIOD -> if (state is CycleState.Known && !state.inPeriod) {
                val days = state.daysUntilNext
                val text = when {
                    days <= 0 -> tr("Vos règles peuvent arriver d'un jour à l'autre.", "Your period may arrive any day now.")
                    days == 1 -> tr("Vos règles devraient arriver demain.", "Your period should arrive tomorrow.")
                    else -> tr("Vos règles devraient arriver dans $days jours environ.", "Your period should arrive in about $days days.")
                }
                notify(kind, tr("Bientôt vos règles", "Period coming soon"), text)
            }
            ReminderKind.LOG -> {
                val logged = logs.any { it.date == today }
                if (ReminderPlanner.shouldRemindToLog(state, settings.reminders, today, logged)) {
                    notify(kind, tr("Un petit point sur votre journée ?", "A quick check-in on your day?"), tr("Quelques secondes pour noter comment vous allez.", "A few seconds to note how you are doing."))
                }
            }
        }
        ReminderScheduler.reschedule(applicationContext, repository)
        return Result.success()
    }

    // L'autorisation est vérifiée au début de la fonction.
    @SuppressLint("MissingPermission")
    private fun notify(kind: ReminderKind, title: String, text: String) {
        val context = applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        ensureChannel(context)
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pending = launch?.let {
            PendingIntent.getActivity(context, kind.ordinal, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        // Sur l'écran verrouillé, seule une version neutre s'affiche.
        val public = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .build()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(public)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(kind.ordinal + 1, notification)
    }

    companion object {
        const val KIND = "kind"
        private const val CHANNEL_ID = "rappels"

        fun ensureChannel(context: Context) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val channel = NotificationChannel(CHANNEL_ID, context.getString(R.string.reminders_channel), NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = context.getString(R.string.reminders_channel_description)
            channel.lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
            manager.createNotificationChannel(channel)
        }
    }
}
