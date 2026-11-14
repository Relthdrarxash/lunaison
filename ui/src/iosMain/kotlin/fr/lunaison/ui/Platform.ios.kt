@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package fr.lunaison.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.lifecycle.compose.LifecycleResumeEffect
import fr.lunaison.core.cycle.HealthReport
import fr.lunaison.core.cycle.LogReminderMode
import fr.lunaison.core.cycle.Prediction
import fr.lunaison.core.data.AppSettings
import fr.lunaison.core.data.Partner
import fr.lunaison.core.data.PartnerChannel
import fr.lunaison.core.data.isBackupExcluded
import fr.lunaison.core.data.setBackupExcluded
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.PartnerSnapshot
import fr.lunaison.core.model.minusDays
import fr.lunaison.core.model.tr
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.launch
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Contacts.CNContact
import platform.Contacts.CNLabeledValue
import platform.Contacts.CNPhoneNumber
import platform.ContactsUI.CNContactPickerDelegateProtocol
import platform.ContactsUI.CNContactPickerViewController
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceGray
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.CoreImage.CIContext
import platform.CoreImage.CIFilter
import platform.CoreImage.createCGImage
import platform.CoreImage.filterWithName
import platform.Foundation.NSDateComponents
import platform.Foundation.NSString
import platform.Foundation.setValue
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsPDFRenderer
import platform.UIKit.UIGraphicsPDFRendererFormat
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.drawInRect
import platform.UIKit.popoverPresentationController
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject
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

private class ContactPickerDelegate(
    private val onPicked: (String, String) -> Unit,
    private val onDone: () -> Unit,
) : NSObject(), CNContactPickerDelegateProtocol {
    override fun contactPicker(picker: CNContactPickerViewController, didSelectContact: CNContact) {
        val name = "${didSelectContact.givenName} ${didSelectContact.familyName}".trim()
        val firstPhone = (didSelectContact.phoneNumbers.firstOrNull() as? CNLabeledValue)?.value as? CNPhoneNumber
        onPicked(name, firstPhone?.stringValue.orEmpty())
        onDone()
    }

    override fun contactPickerDidCancel(picker: CNContactPickerViewController) {
        onDone()
    }
}

@Composable
actual fun rememberContactPicker(onPicked: (name: String, phone: String) -> Unit): (() -> Unit)? {
    val latestOnPicked by rememberUpdatedState(onPicked)
    var activeDelegate by remember { mutableStateOf<ContactPickerDelegate?>(null) }
    return {
        val top = topViewController()
        if (top != null) {
            val picker = CNContactPickerViewController()
            val delegate = ContactPickerDelegate(
                onPicked = { name, phone -> latestOnPicked(name, phone) },
                onDone = { activeDelegate = null },
            )
            activeDelegate = delegate
            picker.delegate = delegate
            top.presentViewController(picker, animated = true, completion = null)
        }
    }
}

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

actual val pdfReportAvailable: Boolean = true

actual suspend fun shareHealthReportPdf(
    context: PlatformContext,
    report: HealthReport,
    medicationLabel: String,
    logs: List<DayLog>,
    sections: Set<ReportSection>,
) {
    val pageRect = CGRectMake(0.0, 0.0, 595.0, 842.0)
    val renderer = UIGraphicsPDFRenderer(bounds = pageRect, format = UIGraphicsPDFRendererFormat())
    val titleFont = UIFont.boldSystemFontOfSize(18.0)
    val headingFont = UIFont.boldSystemFontOfSize(13.0)
    val bodyFont = UIFont.systemFontOfSize(11.0)
    val ink = UIColor.blackColor
    val muted = UIColor.darkGrayColor

    val lines = buildList<Pair<UIFont, String>> {
        add(headingFont to tr("Bilan de cycle — Lunaison", "Cycle health report — Lunaison", pl = "Raport cyklu — Lunaison", uk = "Звіт про цикл — Lunaison", es = "Informe del ciclo — Lunaison", de = "Zyklusbericht — Lunaison"))
        add(bodyFont to "${tr("Jour du cycle", "Cycle day", pl = "Dzień cyklu", uk = "День циклу", es = "Día del ciclo", de = "Zyklustag")} : ${report.dayOfCycle}")
        add(bodyFont to "${tr("Dernières règles", "Last period", pl = "Ostatnia miesiączka", uk = "Остання менструація", es = "Última regla", de = "Letzte Periode")} : ${report.lastPeriodStart}")
        report.overview?.let { ov ->
            if (ReportSection.OVERVIEW in sections) {
                add(headingFont to tr("Vue d'ensemble (90 jours)", "Overview (90 days)", pl = "Podsumowanie (90 dni)", uk = "Огляд (90 днів)", es = "Resumen (90 días)", de = "Übersicht (90 Tage)"))
                add(bodyFont to "${tr("Durée moyenne de cycle", "Average cycle length", pl = "Średnia długość cyklu", uk = "Середня тривалість циклу", es = "Duración media del ciclo", de = "Durchschnittliche Zykluslänge")} : ${ov.usualLength} j")
                ov.usualPeriodLength?.let { add(bodyFont to "${tr("Durée moyenne des règles", "Average period length", pl = "Średnia długość miesiączki", uk = "Середня тривалість менструації", es = "Duración media de la regla", de = "Durchschnittliche Periodenlänge")} : $it j") }
            }
        }
        if (ReportSection.ALERTS in sections && report.alerts.isNotEmpty()) {
            add(headingFont to tr("Signaux observés", "Observed signals", pl = "Zaobserwowane sygnały", uk = "Помічені сигнали", es = "Señales observadas", de = "Beobachtete Signale"))
            report.alerts.forEach { add(bodyFont to "• ${it.name}") }
        }
        if (ReportSection.NOTES in sections && logs.isNotEmpty()) {
            add(headingFont to tr("Journal récent", "Recent logs", pl = "Ostatnie wpisy", uk = "Останні записи", es = "Registros recientes", de = "Letzte Einträge"))
            logs.sortedByDescending { it.date }.take(30).forEach { log ->
                val note = log.note.takeIf { it.isNotBlank() }?.let { " — $it" }.orEmpty()
                add(bodyFont to "• ${log.date}${note}")
            }
        }
        if (medicationLabel.isNotBlank()) {
            add(bodyFont to "${tr("Traitement suivi", "Tracked medication", pl = "Śledzony lek", uk = "Відстежувані ліки", es = "Tratamiento seguido", de = "Verfolgtes Medikament")} : $medicationLabel")
        }
    }

    val pdfData = renderer.PDFDataWithActions { pdfCtx ->
        var y = 48.0
        pdfCtx?.beginPage()
        val title = tr("Lunaison — Document d'échange médical", "Lunaison — Consultation summary", pl = "Lunaison — Podsumowanie na wizytę", uk = "Lunaison — Підсумок для консультації", es = "Lunaison — Resumen para consulta", de = "Lunaison — Gesprächsgrundlage")
        (title as NSString).drawInRect(
            CGRectMake(40.0, y, 515.0, 28.0),
            withAttributes = mapOf(NSFontAttributeName to titleFont, NSForegroundColorAttributeName to ink),
        )
        y += 34.0
        for ((font, text) in lines) {
            if (y > 780.0) {
                pdfCtx?.beginPage()
                y = 48.0
            }
            val color = if (font == headingFont) ink else muted
            (text as NSString).drawInRect(
                CGRectMake(40.0, y, 515.0, 22.0),
                withAttributes = mapOf(NSFontAttributeName to font, NSForegroundColorAttributeName to color),
            )
            y += if (font == headingFont) 24.0 else 18.0
        }
    }
    val path = NSTemporaryDirectory() + "lunaison-bilan.pdf"
    NSFileManager.defaultManager.createFileAtPath(path, contents = pdfData, attributes = null)
    onMain { presentShareSheet(listOf(NSURL.fileURLWithPath(path))) }
}

actual val importAvailable: Boolean = false

@Composable
actual fun ImportDialogs(showSource: Boolean, onDismiss: () -> Unit, actions: Actions, onResult: (String) -> Unit) = Unit

actual val cloudBackupControllable: Boolean = true

actual fun isCloudBackupEnabled(): Boolean = !isBackupExcluded()

actual fun setCloudBackupEnabled(enabled: Boolean) = setBackupExcluded(!enabled)

actual fun encodeQr(content: String): QrMatrix? {
    val filter = CIFilter.filterWithName("CIQRCodeGenerator") ?: return null
    val data = content.encodeToByteArray().toNSData()
    filter.setValue(data, forKey = "inputMessage")
    filter.setValue("M", forKey = "inputCorrectionLevel")
    val output = filter.outputImage ?: return null
    val (width, height) = output.extent.useContents { size.width.toInt() to size.height.toInt() }
    if (width <= 0 || height <= 0) return null
    val ciContext = CIContext.context()
    val cgImage = ciContext.createCGImage(output, fromRect = output.extent) ?: return null
    return try {
        val pixels = ByteArray(width * height)
        val colorSpace = CGColorSpaceCreateDeviceGray() ?: return null
        try {
            pixels.usePinned { pinned ->
                val cgContext = CGBitmapContextCreate(
                    data = pinned.addressOf(0),
                    width = width.toULong(),
                    height = height.toULong(),
                    bitsPerComponent = 8u,
                    bytesPerRow = width.toULong(),
                    space = colorSpace,
                    bitmapInfo = CGImageAlphaInfo.kCGImageAlphaNone.value,
                ) ?: return null
                try {
                    platform.CoreGraphics.CGContextDrawImage(
                        cgContext,
                        CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()),
                        cgImage,
                    )
                } finally {
                    CGContextRelease(cgContext)
                }
            }
        } finally {
            CGColorSpaceRelease(colorSpace)
        }
        val modules = BooleanArray(width * height) { i -> (pixels[i].toInt() and 0xFF) < 128 }
        QrMatrix(width = width, height = height, modules = modules)
    } finally {
        CGImageRelease(cgImage)
    }
}

private class QrMetadataDelegate(
    private val onScanned: (PartnerSnapshot) -> Unit,
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
    private var handled = false

    override fun captureOutput(
        output: platform.AVFoundation.AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection,
    ) {
        if (handled) return
        val raw = didOutputMetadataObjects
            .filterIsInstance<AVMetadataMachineReadableCodeObject>()
            .firstOrNull { it.type == AVMetadataObjectTypeQRCode }
            ?.stringValue ?: return
        val snapshot = PartnerSnapshot.decode(raw) ?: return
        handled = true
        onMain { onScanned(snapshot) }
    }
}

@Composable
actual fun rememberScanner(onSnapshot: (PartnerSnapshot) -> Unit): ScannerState {
    val latestOnSnapshot by rememberUpdatedState(onSnapshot)
    var authorized by remember {
        mutableStateOf(AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) == AVAuthorizationStatusAuthorized)
    }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!authorized) {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                onMain { authorized = granted }
            }
        }
    }
    val hasHardware = remember { AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) != null }
    return ScannerState(
        hasCamera = hasHardware && authorized,
        preview = {
            if (!hasHardware || !authorized) return@ScannerState
            val delegate = remember { QrMetadataDelegate { latestOnSnapshot(it) } }
            val session = remember {
                AVCaptureSession().apply {
                    val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
                    val input = device?.let { runCatching { AVCaptureDeviceInput.deviceInputWithDevice(it, null) }.getOrNull() }
                    if (input != null && canAddInput(input)) addInput(input)
                    val metadataOutput = AVCaptureMetadataOutput()
                    if (canAddOutput(metadataOutput)) {
                        addOutput(metadataOutput)
                        metadataOutput.setMetadataObjectsDelegate(delegate, queue = dispatch_get_main_queue())
                        if (metadataOutput.availableMetadataObjectTypes.contains(AVMetadataObjectTypeQRCode)) {
                            metadataOutput.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
                        }
                    }
                }
            }
            DisposableEffect(session) {
                session.startRunning()
                onDispose { if (session.isRunning()) session.stopRunning() }
            }
            UIKitView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    val previewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
                        videoGravity = AVLayerVideoGravityResizeAspectFill
                    }
                    object : UIView(frame = CGRectMake(0.0, 0.0, 320.0, 320.0)) {
                        init {
                            layer.addSublayer(previewLayer)
                        }

                        override fun layoutSubviews() {
                            super.layoutSubviews()
                            previewLayer.frame = bounds
                        }
                    }
                },
            )
        },
    )
}

private const val ID_LOG_REMINDER = "lunaison.reminder.log"
private const val ID_MED_REMINDER = "lunaison.reminder.medication"
private const val ID_PERIOD_REMINDER = "lunaison.reminder.period"

actual fun syncReminders(settings: AppSettings, prediction: Prediction?) {
    val center = UNUserNotificationCenter.currentNotificationCenter()
    val ids = listOf(ID_LOG_REMINDER, ID_MED_REMINDER, ID_PERIOD_REMINDER)
    center.removePendingNotificationRequestsWithIdentifiers(ids)

    val reminders = settings.reminders
    if (reminders.logReminder != LogReminderMode.OFF) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Lunaison")
            setBody(tr("Prends un instant pour noter ta journée.", "Take a moment to log your day.", pl = "Poświęć chwilę na zapisanie dnia.", uk = "Знайди хвилинку, щоб записати свій день.", es = "Tómate un momento para anotar tu día.", de = "Nimm dir einen Moment, um deinen Tag einzutragen."))
            setSound(UNNotificationSound.defaultSound())
        }
        val components = NSDateComponents().apply {
            setHour(reminders.logReminderTime.hour.toLong())
            setMinute(reminders.logReminderTime.minute.toLong())
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, repeats = true)
        center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(ID_LOG_REMINDER, content, trigger), withCompletionHandler = null)
    }

    if (reminders.medicationReminder) {
        val label = reminders.medicationLabel.trim().ifBlank { tr("Traitement", "Medication", pl = "Lek", uk = "Ліки", es = "Tratamiento", de = "Medikament") }
        val content = UNMutableNotificationContent().apply {
            setTitle("Lunaison")
            setBody(label)
            setSound(UNNotificationSound.defaultSound())
        }
        val components = NSDateComponents().apply {
            setHour(reminders.medicationReminderTime.hour.toLong())
            setMinute(reminders.medicationReminderTime.minute.toLong())
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, repeats = true)
        center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(ID_MED_REMINDER, content, trigger), withCompletionHandler = null)
    }

    if (reminders.periodReminder && prediction != null) {
        val targetDate = prediction.start.minusDays(reminders.periodReminderDaysBefore.toLong())
        val content = UNMutableNotificationContent().apply {
            setTitle("Lunaison")
            setBody(tr("Tes règles approchent selon ton estimation.", "Your period may start soon based on your estimate.", pl = "Zbliża się przewidywana miesiączka.", uk = "За твоїм прогнозом незабаром почнеться менструація.", es = "Tu regla se acerca según tu estimación.", de = "Deine Periode nähert sich laut Schätzung."))
            setSound(UNNotificationSound.defaultSound())
        }
        val components = NSDateComponents().apply {
            setYear(targetDate.year.toLong())
            setMonth(targetDate.monthNumber.toLong())
            setDay(targetDate.dayOfMonth.toLong())
            setHour(9L)
            setMinute(0L)
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, repeats = false)
        center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(ID_PERIOD_REMINDER, content, trigger), withCompletionHandler = null)
    }
}
