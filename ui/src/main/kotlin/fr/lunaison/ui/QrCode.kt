package fr.lunaison.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import fr.lunaison.core.content.ShareMessages
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.model.PartnerSnapshot
import fr.lunaison.core.model.tr
import java.util.concurrent.Executors

/** Affiche un QR Code net et contrasté directement dans l'application, lisible même en thème sombre. */
@Composable
fun QrCodeBox(
    content: String,
    modifier: Modifier = Modifier,
) {
    val matrix = remember(content) {
        runCatching {
            QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                0,
                0,
                mapOf(
                    EncodeHintType.MARGIN to 1,
                    EncodeHintType.CHARACTER_SET to "UTF-8",
                ),
            )
        }.getOrNull()
    }
    if (matrix == null) return

    val qrDescription = tr("QR Code de partage Lunaison", "Lunaison sharing QR Code", pl = "Kod QR do udostępniania Lunaison", uk = "QR-код для обміну Lunaison", es = "Código QR para compartir de Lunaison", de = "Lunaison-QR-Code zum Teilen")
    Surface(
        color = Color(0xFFFBF5F2),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .size(220.dp)
            .semantics { contentDescription = qrDescription },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
        ) {
            val cols = matrix.width
            val rows = matrix.height
            val cellWidth = size.width / cols
            val cellHeight = size.height / rows
            val ink = Color(0xFF241426)
            for (y in 0 until rows) {
                for (x in 0 until cols) {
                    if (matrix.get(x, y)) {
                        drawRect(
                            color = ink,
                            topLeft = Offset(x * cellWidth, y * cellHeight),
                            size = Size(cellWidth + 0.5f, cellHeight + 0.5f),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fenêtre de scan QR Code intégrée à l'application (avec CameraX + ZXing local)
 * et champ de saisie pour coller un code reçu par SMS.
 */
@Composable
fun QrScannerDialog(
    onScanned: (PartnerSnapshot) -> Unit,
    onDismiss: () -> Unit,
    existingPartner: PartnerSnapshot? = null,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var pastedCode by rememberSaveable { mutableStateOf("") }
    var pasteError by rememberSaveable { mutableStateOf(false) }
    var scannedDelivered by remember { mutableStateOf(false) }

    val deliver: (PartnerSnapshot) -> Unit = { decoded ->
        if (!scannedDelivered) {
            scannedDelivered = true
            onScanned(existingPartner?.mergeWith(decoded) ?: decoded)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (existingPartner != null) {
                    tr("Mettre à jour le cycle de ${existingPartner.partnerName}", "Update ${existingPartner.partnerName}'s cycle", pl = "Zaktualizuj cykl: ${existingPartner.partnerName}", uk = "Оновити цикл: ${existingPartner.partnerName}", es = "Actualizar el ciclo de ${existingPartner.partnerName}", de = "Zyklus von ${existingPartner.partnerName} aktualisieren")
                } else {
                    tr("Scanner ou coller un code", "Scan or paste a code", pl = "Zeskanuj lub wklej kod", uk = "Відскануйте або вставте код", es = "Escanear o pegar un código", de = "Code scannen oder einfügen")
                },
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (hasCameraPermission) {
                    Text(
                        if (existingPartner != null) {
                            tr(
                                "Scannez le QR Code de mise à jour affiché sur le téléphone de ${existingPartner.partnerName} :",
                                "Scan the update QR Code shown on ${existingPartner.partnerName}'s phone:",
                                pl = "Zeskanuj kod QR aktualizacji na telefonie partnera (${existingPartner.partnerName}):",
                                uk = "Відскануйте QR-код оновлення на телефоні партнера (${existingPartner.partnerName}):",
                                es = "Escanea el código QR de actualización que aparece en el teléfono de ${existingPartner.partnerName}:",
                                de = "Scanne den Aktualisierungs-QR-Code auf dem Smartphone von ${existingPartner.partnerName}:",
                            )
                        } else {
                            tr(
                                "Visez le QR Code affiché dans Lunaison sur le téléphone de votre partenaire :",
                                "Point at the QR Code shown in Lunaison on your partner's phone:",
                                pl = "Skieruj aparat na kod QR w aplikacji Lunaison na telefonie partnera:",
                                uk = "Наведіть камеру на QR-код у застосунку Lunaison на телефоні партнера:",
                                es = "Apunta al código QR mostrado en Lunaison en el teléfono de tu pareja:",
                                de = "Ziele auf den QR-Code in Lunaison auf dem Smartphone deines Partners:",
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                    CameraQrPreview(
                        onCodeDetected = { raw ->
                            PartnerSnapshot.decode(raw)?.let(deliver)
                        },
                    )
                } else {
                    Text(
                        tr(
                            "Sans accès à la caméra, vous pouvez coller ci-dessous le message ou le code LUNA1:... envoyé par SMS :",
                            "Without camera access, you can paste below the message or LUNA1:... code sent by SMS:",
                            pl = "Bez dostępu do aparatu możesz wkleić poniżej wiadomość lub kod LUNA1:... z SMS-a:",
                            uk = "Без доступу до камери ви можете вставити нижче повідомлення або код LUNA1:..., отриманий в SMS:",
                            es = "Sin acceso a la cámara, puedes pegar a continuación el mensaje o código LUNA1:... enviado por SMS:",
                            de = "Ohne Kamerazugriff kannst du unten die per SMS gesendete Nachricht oder den LUNA1:...-Code einfügen:",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                OutlinedTextField(
                    value = pastedCode,
                    onValueChange = {
                        pastedCode = it
                        pasteError = false
                        PartnerSnapshot.decode(it)?.let(deliver)
                    },
                    label = { Text(tr("Ou coller le SMS / code LUNA1:...", "Or paste SMS / LUNA1:... code", pl = "Lub wklej SMS / kod LUNA1:...", uk = "Або вставте SMS / код LUNA1:...", es = "O pega el SMS / código LUNA1:...", de = "Oder SMS / LUNA1:...-Code einfügen")) },
                    isError = pasteError,
                    supportingText = if (pasteError) {
                        { Text(tr("Aucun code Lunaison valide trouvé dans ce texte.", "No valid Lunaison code found in this text.", pl = "Nie znaleziono prawidłowego kodu Lunaison w tym tekście.", uk = "У цьому тексті не знайдено дійсного коду Lunaison.", es = "No se ha encontrado ningún código Lunaison válido en este texto.", de = "In diesem Text wurde kein gültiger Lunaison-Code gefunden.")) }
                    } else {
                        null
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            if (pastedCode.isNotBlank()) {
                TextButton(
                    onClick = {
                        val decoded = PartnerSnapshot.decode(pastedCode)
                        if (decoded != null) deliver(decoded) else pasteError = true
                    },
                ) {
                    Text(if (existingPartner != null) tr("Mettre à jour", "Update", pl = "Aktualizuj", uk = "Оновити", es = "Actualizar", de = "Aktualisieren") else tr("Importer", "Import", pl = "Importuj", uk = "Імпортувати", es = "Importar", de = "Importieren"))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Fermer", "Close", pl = "Zamknij", uk = "Закрити", es = "Cerrar", de = "Schließen"))
            }
        },
    )
}

@Composable
private fun CameraQrPreview(onCodeDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val reader = remember { QRCodeReader() }

    DisposableEffect(Unit) {
        onDispose { executor.shutdown() }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener(
                    {
                        val provider = runCatching { cameraProviderFuture.get() }.getOrNull() ?: return@addListener
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        analysis.setAnalyzer(executor) { imageProxy ->
                            try {
                                val plane = imageProxy.planes.firstOrNull()
                                if (plane != null) {
                                    val buffer = plane.buffer
                                    val bytes = ByteArray(buffer.remaining())
                                    buffer.get(bytes)
                                    val source = PlanarYUVLuminanceSource(
                                        bytes,
                                        imageProxy.width,
                                        imageProxy.height,
                                        0,
                                        0,
                                        imageProxy.width,
                                        imageProxy.height,
                                        false,
                                    )
                                    val bitmap = BinaryBitmap(HybridBinarizer(source))
                                    val result = runCatching { reader.decode(bitmap) }.getOrNull()
                                    if (result != null && "LUNA1:" in result.text) {
                                        previewView.post { onCodeDetected(result.text) }
                                    }
                                }
                            } finally {
                                reader.reset()
                                imageProxy.close()
                            }
                        }
                        runCatching {
                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analysis,
                            )
                        }
                    },
                    ContextCompat.getMainExecutor(ctx),
                )
                previewView
            },
        )
    }
}

/** Carte affichant les estimations du cycle d'un ou d'une partenaire suivi(e) par QR Code ou SMS. */
@Composable
fun PartnerSnapshotCard(
    snapshot: PartnerSnapshot,
    cycle: CycleState.Known,
    onRescan: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
) {
    var confirmRemove by rememberSaveable { mutableStateOf(false) }
    val guide = remember(cycle.phase) { ShareMessages.partnerGuide(cycle.phase) }
    LunaCard(container = MaterialTheme.colorScheme.secondaryContainer) {
        Text(
            tr("Cycle de ${snapshot.partnerName} · Jour ${cycle.dayOfCycle} (${Format.phaseName(cycle.phase)})", "${snapshot.partnerName}'s cycle · Day ${cycle.dayOfCycle} (${Format.phaseName(cycle.phase)})", pl = "Cykl: ${snapshot.partnerName} · Dzień ${cycle.dayOfCycle} (${Format.phaseName(cycle.phase)})", uk = "Цикл: ${snapshot.partnerName} · День ${cycle.dayOfCycle} (${Format.phaseName(cycle.phase)})", es = "Ciclo de ${snapshot.partnerName} · Día ${cycle.dayOfCycle} (${Format.phaseName(cycle.phase)})", de = "Zyklus von ${snapshot.partnerName} · Tag ${cycle.dayOfCycle} (${Format.phaseName(cycle.phase)})"),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
            Format.nextPeriod(cycle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        if (!cycle.inPeriod && cycle.daysLate == 0) {
            Text(
                Format.range(cycle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            if (snapshot.showFertility) {
                Text(
                    Format.fertileWindow(cycle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        Text(
            tr("Comment soutenir aujourd'hui :", "How to support today:", pl = "Jak wspierać dzisiaj:", uk = "Як підтримати сьогодні:", es = "Cómo apoyar hoy:", de = "So kann man heute unterstützen:"),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        guide.supportTips.take(2).forEach { tip ->
            Bullet(tip)
        }
        Text(
            tr("Partagé le ${Format.dayMonth(snapshot.sharedAt)}. Rescannez ou demandez un SMS au prochain cycle pour garder des estimations à jour.", "Shared on ${Format.dayMonth(snapshot.sharedAt)}. Rescan or request an SMS next cycle to keep estimates up to date.", pl = "Udostępniono ${Format.dayMonth(snapshot.sharedAt)}. Zeskanuj ponownie lub poproś o SMS w następnym cyklu, aby prognozy były aktualne.", uk = "Передано ${Format.dayMonth(snapshot.sharedAt)}. Скануйте знову або попросіть SMS наступного циклу, щоб прогнози залишалися актуальними.", es = "Compartido el ${Format.dayMonth(snapshot.sharedAt)}. Vuelve a escanear o pide un SMS el próximo ciclo para mantener las estimaciones al día.", de = "Geteilt am ${Format.dayMonth(snapshot.sharedAt)}. Scanne erneut oder fordere im nächsten Zyklus eine SMS an, damit die Schätzungen aktuell bleiben."),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        if (onRescan != null || onRemove != null) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (onRemove != null) {
                    TextButton(onClick = { confirmRemove = true }) {
                        Text(
                            tr(
                                "Arrêter ce suivi",
                                "Stop following",
                                pl = "Zatrzymaj śledzenie",
                                uk = "Припинити стеження",
                                es = "Dejar de seguir",
                                de = "Begleitung beenden",
                            ),
                        )
                    }
                }
                if (onRescan != null) {
                    TextButton(onClick = onRescan) {
                        Text(
                            tr(
                                "Mettre à jour (QR / SMS)",
                                "Update (QR / SMS)",
                                pl = "Aktualizuj (QR / SMS)",
                                uk = "Оновити (QR / SMS)",
                                es = "Actualizar (QR / SMS)",
                                de = "Aktualisieren (QR / SMS)",
                            ),
                        )
                    }
                }
            }
        }
    }

    if (confirmRemove) {
        AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = {
                Text(
                    tr(
                        "Arrêter de suivre le cycle de ${snapshot.partnerName} ?",
                        "Stop following ${snapshot.partnerName}'s cycle?",
                        pl = "Przestać śledzić cykl: ${snapshot.partnerName}?",
                        uk = "Припинити стежити за циклом ${snapshot.partnerName}?",
                        es = "¿Dejar de seguir el ciclo de ${snapshot.partnerName}?",
                        de = "Zyklus von ${snapshot.partnerName} nicht mehr begleiten?",
                    ),
                )
            },
            text = {
                Text(
                    tr(
                        "Les prévisions et repères de ${snapshot.partnerName} seront retirés de votre écran. Vous pourrez reprendre ce suivi à tout moment en scannant à nouveau son QR Code ou par SMS.",
                        "${snapshot.partnerName}'s forecasts and notes will be removed from your screen. You can resume this tracking at any time by scanning their QR Code again or by SMS.",
                        pl = "Prognozy i wskazówki dla: ${snapshot.partnerName} zostaną usunięte z Twojego ekranu. Możesz wznowić to śledzenie w dowolnym momencie, skanując ponownie kod QR lub SMS.",
                        uk = "Прогнози та орієнтири для ${snapshot.partnerName} буде видалено з вашого екрана. Ви зможете відновити відстеження в будь-який час, повторно відсканувавши QR-код або SMS.",
                        es = "Las previsiones y referencias de ${snapshot.partnerName} se eliminarán de tu pantalla. Podrás reanudar el seguimiento en cualquier momento volviendo a escanear su código QR o por SMS.",
                        de = "Die Vorhersagen und Notizen für ${snapshot.partnerName} werden von deinem Bildschirm entfernt. Du kannst die Begleitung jederzeit wieder aufnehmen, indem du den QR-Code erneut scannst oder per SMS.",
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmRemove = false
                        onRemove?.invoke()
                    },
                ) {
                    Text(
                        tr(
                            "Arrêter ce suivi",
                            "Stop following",
                            pl = "Zatrzymaj śledzenie",
                            uk = "Припинити стеження",
                            es = "Dejar de seguir",
                            de = "Begleitung beenden",
                        ),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmRemove = false }) {
                    Text(
                        tr("Annuler", "Cancel", pl = "Anuluj", uk = "Скасувати", es = "Cancelar", de = "Abbrechen"),
                    )
                }
            },
        )
    }
}
