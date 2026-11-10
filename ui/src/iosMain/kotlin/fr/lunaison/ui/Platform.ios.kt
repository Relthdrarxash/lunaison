@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package fr.lunaison.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import fr.lunaison.core.cycle.HealthReport
import fr.lunaison.core.data.Partner
import fr.lunaison.core.data.PartnerChannel
import fr.lunaison.core.data.isBackupExcluded
import fr.lunaison.core.data.setBackupExcluded
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.PartnerSnapshot
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIViewController
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual abstract class PlatformContext

private object IosContext : PlatformContext()

@Composable
actual fun rememberPlatformContext(): PlatformContext = IosContext

actual fun openLink(context: PlatformContext, url: String) {
    val target = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(target, options = emptyMap<Any?, Any>(), completionHandler = null)
}

// À renseigner avec l'identifiant App Store une fois la fiche créée.
actual fun openStore(context: PlatformContext) = Unit

actual val storePageAvailable: Boolean = false

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

actual val platformName: String = "iOS"

private fun onMain(block: () -> Unit) {
    dispatch_async(dispatch_get_main_queue()) { block() }
}

private fun topViewController(): UIViewController? {
    var top = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return null
    while (true) top = top.presentedViewController ?: return top
}

private fun presentAlert(message: String) {
    val alert = UIAlertController.alertControllerWithTitle(title = null, message = message, preferredStyle = UIAlertControllerStyleAlert)
    alert.addAction(UIAlertAction.actionWithTitle(title = "OK", style = UIAlertActionStyleDefault, handler = null))
    topViewController()?.presentViewController(alert, animated = true, completion = null)
}

/** Feuille de partage du système, avec son point d'ancrage sur iPad. */
internal fun presentShareSheet(items: List<Any>) {
    val top = topViewController() ?: return
    val controller = UIActivityViewController(activityItems = items, applicationActivities = null)
    controller.popoverPresentationController?.let { popover ->
        popover.sourceView = top.view
        val (width, height) = top.view.bounds.useContents { size.width to size.height }
        popover.sourceRect = CGRectMake(width / 2, height / 2, 0.0, 0.0)
    }
    top.presentViewController(controller, animated = true, completion = null)
}

private fun ByteArray.toNSData(): NSData =
    if (isEmpty()) NSData() else usePinned { NSData.create(bytes = it.addressOf(0), length = size.toULong()) }

actual fun showToast(context: PlatformContext, message: String) = onMain { presentAlert(message) }

@Composable
actual fun rememberNotificationAccess(onAnswered: (granted: Boolean) -> Unit): NotificationAccess {
    var allowed by remember { mutableStateOf(false) }
    val answered by rememberUpdatedState(onAnswered)
    val center = UNUserNotificationCenter.currentNotificationCenter()

    // Au retour des réglages d'iOS, l'état des notifications peut avoir changé.
    LifecycleResumeEffect(Unit) {
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val status = settings?.authorizationStatus
            val granted = status == UNAuthorizationStatusAuthorized || status == UNAuthorizationStatusProvisional
            onMain { allowed = granted }
        }
        onPauseOrDispose { }
    }
    return NotificationAccess(
        allowed = allowed,
        request = {
            center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound) { granted, _ ->
                onMain {
                    allowed = granted
                    answered(granted)
                }
            }
        },
        openSettings = {
            NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let {
                UIApplication.sharedApplication.openURL(it, options = emptyMap<Any?, Any>(), completionHandler = null)
            }
        },
    )
}

@Composable
actual fun rememberFileExporter(mimeType: String, content: suspend () -> String): (fileName: String) -> Unit {
    val scope = rememberCoroutineScope()
    val produce by rememberUpdatedState(content)
    return { fileName ->
        scope.launch {
            val path = NSTemporaryDirectory() + fileName
            NSFileManager.defaultManager.createFileAtPath(path, contents = produce().encodeToByteArray().toNSData(), attributes = null)
            presentShareSheet(listOf(NSURL.fileURLWithPath(path)))
        }
    }
}

// Le sélecteur de contacts d'iOS (ContactsUI) viendra avec une prochaine étape : la saisie manuelle reste possible.
@Composable
actual fun rememberContactPicker(onPicked: (name: String, phone: String) -> Unit): (() -> Unit)? = null

private fun percentEncoded(text: String): String = buildString {
    for (byte in text.encodeToByteArray()) {
        val value = byte.toInt() and 0xFF
        val char = value.toChar()
        if (char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' || char in "-._~") {
            append(char)
        } else {
            append('%').append(value.toString(16).uppercase().padStart(2, '0'))
        }
    }
}

actual object ShareLauncher {
    actual fun send(context: PlatformContext, partner: Partner, text: String) {
        val phone = partner.phone.orEmpty().filter { it.isDigit() || it == '+' }
        when {
            partner.channel == PartnerChannel.SMS && phone.isNotEmpty() ->
                openLink(context, "sms:$phone&body=${percentEncoded(text)}")
            partner.channel == PartnerChannel.WHATSAPP && phone.any { it.isDigit() } ->
                openLink(context, "https://wa.me/${phone.filter { it.isDigit() }}?text=${percentEncoded(text)}")
            else -> shareAny(context, text)
        }
    }

    actual fun shareAny(context: PlatformContext, text: String) {
        presentShareSheet(listOf(text))
    }
}

// Pas de NFC pour échanger le code sur iPhone : QR Code ou message.
@Composable
actual fun NfcShareEffect(payload: String) = Unit

// Le PDF du bilan dessine avec les API graphiques d'Android : à refaire pour iOS.
actual val pdfReportAvailable: Boolean = false

actual suspend fun shareHealthReportPdf(
    context: PlatformContext,
    report: HealthReport,
    medicationLabel: String,
    logs: List<DayLog>,
    sections: Set<ReportSection>,
): Unit = throw UnsupportedOperationException("Export PDF indisponible sur iOS")

actual val importAvailable: Boolean = false

@Composable
actual fun ImportDialogs(showSource: Boolean, onDismiss: () -> Unit, actions: Actions, onResult: (String) -> Unit) = Unit

actual val cloudBackupControllable: Boolean = true

actual fun isCloudBackupEnabled(): Boolean = !isBackupExcluded()

actual fun setCloudBackupEnabled(enabled: Boolean) = setBackupExcluded(!enabled)

// L'encodeur de QR Code pour iOS reste à écrire : en attendant, le code se partage par message.
actual fun encodeQr(content: String): QrMatrix? = null

@Composable
actual fun rememberScanner(onSnapshot: (PartnerSnapshot) -> Unit): ScannerState =
    ScannerState(hasCamera = false, hasNfc = false, preview = { })
