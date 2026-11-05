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

    val qrDescription = tr("QR Code de partage Lunaison", "Lunaison sharing QR Code")
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
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var pastedCode by rememberSaveable { mutableStateOf("") }
    var pasteError by rememberSaveable { mutableStateOf(false) }

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
        title = { Text(tr("Scanner ou coller un code", "Scan or paste a code")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (hasCameraPermission) {
                    Text(
                        tr("Visez le QR Code affiché dans Lunaison sur le téléphone de votre partenaire :", "Point at the QR Code shown in Lunaison on your partner's phone:"),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    CameraQrPreview(
                        onCodeDetected = { raw ->
                            PartnerSnapshot.decode(raw)?.let(onScanned)
                        },
                    )
                } else {
                    Text(
                        tr("Sans accès à la caméra, vous pouvez coller ci-dessous le message ou le code LUNA1:... envoyé par SMS :", "Without camera access, you can paste below the message or LUNA1:... code sent by SMS:"),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                OutlinedTextField(
                    value = pastedCode,
                    onValueChange = {
                        pastedCode = it
                        pasteError = false
                        PartnerSnapshot.decode(it)?.let(onScanned)
                    },
                    label = { Text(tr("Ou coller le SMS / code LUNA1:...", "Or paste SMS / LUNA1:... code")) },
                    isError = pasteError,
                    supportingText = if (pasteError) {
                        { Text(tr("Aucun code Lunaison valide trouvé dans ce texte.", "No valid Lunaison code found in this text.")) }
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
                        if (decoded != null) onScanned(decoded) else pasteError = true
                    },
                ) {
                    Text(tr("Importer", "Import"))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Fermer", "Close"))
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
    val guide = remember(cycle.phase) { ShareMessages.partnerGuide(cycle.phase) }
    LunaCard(container = MaterialTheme.colorScheme.secondaryContainer) {
        Text(
            tr("Cycle de ${snapshot.partnerName} · Jour ${cycle.dayOfCycle} (${Format.phaseName(cycle.phase)})", "${snapshot.partnerName}'s cycle · Day ${cycle.dayOfCycle} (${Format.phaseName(cycle.phase)})"),
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
            tr("Comment soutenir aujourd'hui :", "How to support today:"),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        guide.supportTips.take(2).forEach { tip ->
            Bullet(tip)
        }
        Text(
            tr("Partagé le ${Format.dayMonth(snapshot.sharedAt)}. Rescannez ou demandez un SMS au prochain cycle pour garder des estimations à jour.", "Shared on ${Format.dayMonth(snapshot.sharedAt)}. Rescan or request an SMS next cycle to keep estimates up to date."),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        if (onRescan != null || onRemove != null) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (onRemove != null) {
                    TextButton(onClick = onRemove) {
                        Text(tr("Arrêter ce suivi", "Stop following"))
                    }
                }
                if (onRescan != null) {
                    TextButton(onClick = onRescan) {
                        Text(tr("Mettre à jour (QR / SMS)", "Update (QR / SMS)"))
                    }
                }
            }
        }
    }
}
