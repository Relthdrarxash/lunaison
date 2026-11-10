package fr.lunaison.ui

import android.Manifest
import android.app.Activity
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
import fr.lunaison.core.model.PartnerSnapshot
import java.util.concurrent.Executors

actual fun encodeQr(content: String): QrMatrix? = runCatching {
    val matrix = QRCodeWriter().encode(
        content,
        BarcodeFormat.QR_CODE,
        0,
        0,
        mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8",
        ),
    )
    val modules = BooleanArray(matrix.width * matrix.height)
    for (y in 0 until matrix.height) {
        for (x in 0 until matrix.width) modules[y * matrix.width + x] = matrix.get(x, y)
    }
    QrMatrix(matrix.width, matrix.height, modules)
}.getOrNull()

@Composable
actual fun rememberScanner(onSnapshot: (PartnerSnapshot) -> Unit): ScannerState {
    val context = LocalContext.current
    val deliver by rememberUpdatedState(onSnapshot)
    val activity = remember(context) {
        generateSequence(context) { (it as? ContextWrapper)?.baseContext }
            .filterIsInstance<Activity>()
            .firstOrNull()
    }
    val nfcAdapter = remember(activity) {
        activity?.let { runCatching { NfcAdapter.getDefaultAdapter(it) }.getOrNull() }
    }
    val hasNfc = nfcAdapter != null && nfcAdapter.isEnabled

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }

    DisposableEffect(activity, nfcAdapter) {
        val currentActivity = activity
        val currentAdapter = nfcAdapter
        if (currentActivity != null && currentAdapter != null && currentAdapter.isEnabled) {
            val callback = NfcAdapter.ReaderCallback { tag ->
                val isoDep = IsoDep.get(tag) ?: return@ReaderCallback
                try {
                    isoDep.connect()
                    isoDep.timeout = 2500
                    val command = LunaisonHceService.buildSelectAidApdu()
                    val response = isoDep.transceive(command)
                    if (response != null && response.size >= 2) {
                        val sw1 = response[response.size - 2].toInt() and 0xFF
                        val sw2 = response[response.size - 1].toInt() and 0xFF
                        if (sw1 == 0x90 && sw2 == 0x00) {
                            val payloadBytes = response.copyOfRange(0, response.size - 2)
                            val text = String(payloadBytes, java.nio.charset.StandardCharsets.UTF_8)
                            val decoded = PartnerSnapshot.decode(text)
                            if (decoded != null) {
                                currentActivity.runOnUiThread {
                                    deliver(decoded)
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Tag déconnecté prématurément ou non reconnu
                } finally {
                    runCatching { isoDep.close() }
                }
            }
            val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
            currentAdapter.enableReaderMode(currentActivity, callback, flags, null)
        }
        onDispose {
            if (currentActivity != null && currentAdapter != null && currentAdapter.isEnabled) {
                runCatching { currentAdapter.disableReaderMode(currentActivity) }
            }
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

    return ScannerState(
        hasCamera = hasCameraPermission,
        hasNfc = hasNfc,
        preview = { CameraQrPreview(onCodeDetected = { raw -> PartnerSnapshot.decode(raw)?.let { deliver(it) } }) },
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
