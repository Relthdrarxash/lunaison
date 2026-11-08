package fr.lunaison.reminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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

enum class ReminderKind { PERIOD, LOG, MEDICATION }

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
        val periodAt = if (settings.pausePredictions) null else ReminderPlanner.nextPeriodReminder(state, settings.reminders, now)
        plan(context, ReminderKind.PERIOD, periodAt, now)
        plan(context, ReminderKind.LOG, ReminderPlanner.nextLogReminder(settings.reminders, now), now)
        plan(context, ReminderKind.MEDICATION, ReminderPlanner.nextMedicationReminder(settings.reminders, now), now)
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
                    days <= 0 -> tr("Vos règles peuvent arriver d'un jour à l'autre.", "Your period may arrive any day now.", pl = "Miesiączka może pojawić się lada dzień.", uk = "Місячні можуть початися будь-якого дня.", es = "Tu regla puede llegar en cualquier momento.", de = "Deine Periode kann jeden Tag kommen.")
                    days == 1 -> tr("Vos règles devraient arriver demain.", "Your period should arrive tomorrow.", pl = "Miesiączka powinna pojawić się jutro.", uk = "Місячні мають початися завтра.", es = "Tu regla debería llegar mañana.", de = "Deine Periode sollte morgen kommen.")
                    else -> tr("Vos règles devraient arriver dans $days jours environ.", "Your period should arrive in about $days days.", pl = "Miesiączka powinna pojawić się za około $days dni.", uk = "Місячні мають початися приблизно через $days днів.", es = "Tu regla debería llegar en unos $days días.", de = "Deine Periode sollte in etwa $days Tagen kommen.")
                }
                notify(kind, settings.pinHash != null, tr("Bientôt vos règles", "Period coming soon", pl = "Wkrótce miesiączka", uk = "Скоро місячні", es = "Pronto tu regla", de = "Periode bald"), text)
            }
            ReminderKind.LOG -> {
                val logged = logs.any { it.date == today }
                if (ReminderPlanner.shouldRemindToLog(state, settings.reminders, today, logged)) {
                    notify(kind, settings.pinHash != null, tr("Un petit point sur votre journée ?", "A quick check-in on your day?", pl = "Krótko o Twoim dniu?", uk = "Коротко про ваш день?", es = "¿Un momento para tu día?", de = "Kurz zu deinem Tag?"), tr("Quelques secondes pour noter comment vous allez.", "A few seconds to note how you are doing.", pl = "Kilka sekund, by zanotować, jak się czujesz.", uk = "Кілька секунд, щоб записати, як ви почуваєтеся.", es = "Unos segundos para anotar cómo estás.", de = "Ein paar Sekunden, um festzuhalten, wie es dir geht."))
                }
            }
            ReminderKind.MEDICATION -> {
                val takenToday = logs.any { it.date == today && it.medicationTaken }
                if (ReminderPlanner.shouldRemindMedication(settings.reminders, takenToday)) {
                    val label = settings.reminders.medicationLabel.trim()
                    val title = if (label.isNotEmpty()) tr("Rappel : $label", "Reminder: $label", pl = "Przypomnienie: $label", uk = "Нагадування: $label", es = "Recordatorio: $label", de = "Erinnerung: $label") else tr("Rappel quotidien", "Daily reminder", pl = "Codzienne przypomnienie", uk = "Щоденне нагадування", es = "Recordatorio diario", de = "Tägliche Erinnerung")
                    val text = tr("Pensez à votre prise du jour et à la noter dans Lunaison.", "Remember your daily dose and log it in Lunaison.", pl = "Pamiętaj o dzisiejszej dawce i zanotuj ją w Lunaison.", uk = "Не забудьте про сьогоднішню дозу й запишіть її в Lunaison.", es = "Recuerda tu toma de hoy y anótala en Lunaison.", de = "Denk an deine heutige Einnahme und trag sie in Lunaison ein.")
                    notify(kind, settings.pinHash != null, title, text)
                }
            }
        }
        ReminderScheduler.reschedule(applicationContext, repository)
        return Result.success()
    }

    // L'autorisation est vérifiée au début de la fonction.
    @SuppressLint("MissingPermission")
    private fun notify(kind: ReminderKind, locked: Boolean, title: String, text: String) {
        val context = applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        ensureChannel(context)
        // Le rappel de saisie ouvre directement la journée ; le code PIN reste demandé à l'ouverture.
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            ?.apply { if (kind == ReminderKind.LOG) putExtra(EXTRA_OPEN, OPEN_LOG) }
        val pending = launch?.let {
            PendingIntent.getActivity(context, kind.ordinal, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        // Sur l'écran verrouillé, seule une version neutre s'affiche.
        val public = NotificationCompat.Builder(context, channelFor(kind))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .build()
        val builder = NotificationCompat.Builder(context, channelFor(kind))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(public)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        // Action en un geste depuis la notification. Pas d'action si un code PIN protège l'appli :
        // on n'écrit rien sans passer par le déverrouillage.
        if (!locked) {
            val action = when (kind) {
                ReminderKind.PERIOD -> ReminderActionReceiver.ACTION_PERIOD_STARTED to
                    tr("Mes règles ont commencé", "My period has started", pl = "Zaczęła się miesiączka", uk = "Місячні почалися", es = "Me ha venido la regla", de = "Meine Periode hat begonnen")
                ReminderKind.MEDICATION -> ReminderActionReceiver.ACTION_MEDICATION_TAKEN to
                    tr("C'est pris", "Taken", pl = "Przyjęte", uk = "Прийнято", es = "Tomado", de = "Genommen")
                ReminderKind.LOG -> null
            }
            action?.let { (name, label) ->
                val intent = Intent(context, ReminderActionReceiver::class.java).setAction(name)
                val actionPending = PendingIntent.getBroadcast(context, kind.ordinal, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                builder.addAction(0, label, actionPending)
            }
        }
        NotificationManagerCompat.from(context).notify(notificationId(kind), builder.build())
    }

    companion object {
        const val KIND = "kind"
        const val EXTRA_OPEN = "fr.lunaison.OPEN"
        const val OPEN_LOG = "log"

        /** Ancien canal unique : remplacé par un canal par type, pour couper un rappel sans les autres. */
        private const val LEGACY_CHANNEL_ID = "rappels"

        fun notificationId(kind: ReminderKind) = kind.ordinal + 1

        private fun channelFor(kind: ReminderKind) = "rappel-${kind.name.lowercase()}"

        fun ensureChannel(context: Context) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            manager.deleteNotificationChannel(LEGACY_CHANNEL_ID)
            val names = mapOf(
                ReminderKind.PERIOD to tr("Annonce des règles", "Period coming", pl = "Zapowiedź miesiączki", uk = "Попередження про місячні", es = "Aviso de la regla", de = "Periodenankündigung"),
                ReminderKind.LOG to tr("Noter ma journée", "Log my day", pl = "Zanotuj mój dzień", uk = "Записати мій день", es = "Anotar mi día", de = "Meinen Tag eintragen"),
                ReminderKind.MEDICATION to tr("Prise quotidienne", "Daily dose", pl = "Codzienna dawka", uk = "Щоденна доза", es = "Toma diaria", de = "Tägliche Einnahme"),
            )
            names.forEach { (kind, name) ->
                val channel = NotificationChannel(channelFor(kind), name, NotificationManager.IMPORTANCE_DEFAULT)
                channel.description = context.getString(R.string.reminders_channel_description)
                channel.lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
                manager.createNotificationChannel(channel)
            }
        }
    }
}
