package com.college.attendx.ui.components

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executors
import kotlin.coroutines.resume

/**
 * Custom QR scanner built on CameraX + ML Kit, replacing ZXing's
 * ScanContract/CaptureActivity (which forced landscape capability, had
 * a built-in laser-line overlay, and no pinch-to-zoom).
 *
 * - Portrait-locked via the Activity's screenOrientation in the manifest
 *   PLUS PreviewView's natural portrait aspect handling here.
 * - Pinch-to-zoom wired directly to CameraX's CameraControl.setZoomRatio.
 * - No barcode-style red scan line - caller supplies its own overlay
 *   (see ScannerOverlay below) for a custom brutalist frame instead.
 * - Fires onQrCodeScanned ONCE per successful decode, then the analyzer
 *   should be torn down by the caller (e.g. by navigating away) to avoid
 *   repeat-firing on the same code while still in frame.
 */
@Composable
fun CameraQrScanner(
    modifier: Modifier = Modifier,
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var camera by remember { mutableStateOf<Camera?>(null) }
    var currentZoomRatio by remember { mutableStateOf(1f) }
    var hasScanned by remember { mutableStateOf(false) }

    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProvider = context.getCameraProvider()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val barcodeScannerOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val scanner = BarcodeScanning.getClient(barcodeScannerOptions)

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
            processImageProxy(scanner, imageProxy) { qrValue ->
                if (!hasScanned) {
                    hasScanned = true
                    onQrCodeScanned(qrValue)
                }
            }
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            Log.e("CameraQrScanner", "Camera binding failed: ${e.message}")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(camera) {
                // Pinch-to-zoom: scale gesture maps directly to CameraX's
                // zoom ratio, clamped to whatever range this device's
                // lens actually supports.
                detectTransformGestures { _, _, gestureZoom, _ ->
                    val cam = camera ?: return@detectTransformGestures
                    val zoomState = cam.cameraInfo.zoomState.value
                    val minZoom = zoomState?.minZoomRatio ?: 1f
                    val maxZoom = zoomState?.maxZoomRatio ?: 1f

                    val newZoom = (currentZoomRatio * gestureZoom).coerceIn(minZoom, maxZoom)
                    currentZoomRatio = newZoom
                    cam.cameraControl.setZoomRatio(newZoom)
                }
            }
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Runs one ML Kit barcode decode pass on a single camera frame.
 * Always closes the ImageProxy when done (success or failure) - CameraX
 * will stop delivering new frames if you forget this.
 */
@androidx.camera.core.ExperimentalGetImage
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onQrFound: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            for (barcode in barcodes) {
                val value = barcode.rawValue
                if (!value.isNullOrBlank()) {
                    onQrFound(value)
                    break
                }
            }
        }
        .addOnFailureListener {
            Log.e("CameraQrScanner", "Barcode scan failed: ${it.message}")
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

/** Coroutine wrapper around CameraX's listenable future provider. */
private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCancellableCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { future ->
            future.addListener(
                { continuation.resume(future.get()) },
                Executors.newSingleThreadExecutor()
            )
        }
    }