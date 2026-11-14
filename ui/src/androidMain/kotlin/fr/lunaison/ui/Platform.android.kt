package fr.lunaison.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LifecycleResumeEffect
import fr.lunaison.core.cycle.HealthReport
import fr.lunaison.core.cycle.Prediction
import fr.lunaison.core.data.AppSettings
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.tr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

actual typealias PlatformContext = Context

@Composable
actual fun rememberPlatformContext(): PlatformContext = LocalContext.current

actual fun openLink(context: PlatformContext, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: ActivityNotFoundException) {
        // Aucun navigateur : rien à faire.
    }
}

private const val PACKAGE_NAME = "fr.lunaison"

actual fun openStore(context: PlatformContext) {
    val marketUri = Uri.parse("market://details?id=$PACKAGE_NAME")
    val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$PACKAGE_NAME")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, marketUri))
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        } catch (_: ActivityNotFoundException) {
            // Aucun magasin ni navigateur disponible
        }
    }
}

actual val storePageAvailable: Boolean = true

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}

actual val platformName: String = "Android"

actual fun showToast(context: PlatformContext, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

/** Les notifications peuvent être envoyées : autorisation accordée (Android 13 et plus) et non coupées dans les réglages d'Android. */
private fun notificationsAllowed(context: Context): Boolean =
    runCatching { NotificationManagerCompat.from(context).areNotificationsEnabled() }.getOrDefault(false)

@Composable
actual fun rememberNotificationAccess(onAnswered: (granted: Boolean) -> Unit): NotificationAccess {
    val context = LocalContext.current
    var allowed by remember { mutableStateOf(notificationsAllowed(context)) }
    val answered by rememberUpdatedState(onAnswered)

    // Au retour des réglages d'Android, l'état des notifications peut avoir changé.
    LifecycleResumeEffect(Unit) {
        allowed = notificationsAllowed(context)
        onPauseOrDispose { }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        allowed = granted || notificationsAllowed(context)
        answered(granted)
    }
    return NotificationAccess(
        allowed = allowed,
        request = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                allowed = notificationsAllowed(context)
                answered(allowed)
            }
        },
        openSettings = {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(intent) }
        },
    )
}

@Composable
actual fun rememberFileExporter(mimeType: String, content: suspend () -> String): (fileName: String) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val produce by rememberUpdatedState(content)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(mimeType)) { uri ->
        if (uri != null) {
            scope.launch {
                val text = produce()
                context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
            }
        }
    }
    return { fileName -> launcher.launch(fileName) }
}

@Composable
actual fun rememberContactPicker(onPicked: (name: String, phone: String) -> Unit): (() -> Unit)? {
    val context = LocalContext.current
    val picked by rememberUpdatedState(onPicked)
    // Le sélecteur de contacts du système ne donne accès qu'au contact choisi : aucune autorisation n'est nécessaire.
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                picked(cursor.getString(0).orEmpty().substringBefore(" "), cursor.getString(1).orEmpty())
            }
        }
    }
    return {
        try {
            picker.launch(Intent(Intent.ACTION_PICK).setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE))
        } catch (_: ActivityNotFoundException) {
            // Pas de carnet d'adresses : la saisie manuelle reste possible.
        }
    }
}

actual val pdfReportAvailable: Boolean = true

actual suspend fun shareHealthReportPdf(
    context: PlatformContext,
    report: HealthReport,
    medicationLabel: String,
    logs: List<DayLog>,
    sections: Set<ReportSection>,
) {
    val file = withContext(Dispatchers.IO) {
        HealthReportPdf.export(context, buildReportDocument(report, medicationLabel, logs, sections))
    }
    sharePdf(context, file)
}

/** Ouvre le sélecteur de partage pour le PDF, lu par les autres applis via le FileProvider de Lunaison. */
private fun sharePdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fichiers", file)
    val subject = tr(
        fr = "Bilan de cycle Lunaison",
        en = "Lunaison cycle health report",
        pl = "Raport o cyklu z Lunaison",
        uk = "Звіт про цикл із Lunaison",
        es = "Informe del ciclo de Lunaison",
        de = "Zyklusbericht von Lunaison",
    )
    val send = Intent(Intent.ACTION_SEND)
        .setType("application/pdf")
        .putExtra(Intent.EXTRA_SUBJECT, subject)
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    send.clipData = ClipData.newRawUri(file.name, uri)
    try {
        context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
    } catch (_: ActivityNotFoundException) {
        val message = tr(
            fr = "Aucune appli pour partager ce PDF.",
            en = "No app available to share this PDF.",
            pl = "Brak aplikacji do udostępnienia tego pliku PDF.",
            uk = "Немає застосунку, щоб поділитися цим PDF.",
            es = "No hay ninguna aplicación para compartir este PDF.",
            de = "Keine App zum Teilen dieses PDFs verfügbar.",
        )
        showToast(context, message)
    }
}

actual val importAvailable: Boolean = true

// Android : la sauvegarde automatique est désactivée dans le manifeste, rien à régler.
actual val cloudBackupControllable: Boolean = false

actual fun isCloudBackupEnabled(): Boolean = false

actual fun setCloudBackupEnabled(enabled: Boolean) = Unit

// Android : WorkManager observe déjà le dépôt dans LunaisonApplication.
actual fun syncReminders(settings: AppSettings, prediction: Prediction?) = Unit
