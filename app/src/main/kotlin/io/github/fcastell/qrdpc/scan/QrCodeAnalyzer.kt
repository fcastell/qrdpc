package io.github.fcastell.qrdpc.scan

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

private const val BOM = '\uFEFF'

/**
 * Detects QR codes in camera frames and hands their decoded text off via [onDecoded].
 * Pauses itself after a detection (see [pause]/[resume]) so the same code isn't
 * repeatedly re-triggered while its result is being shown.
 */
class QrCodeAnalyzer(
    private val onDecoded: (String) -> Unit,
) : ImageAnalysis.Analyzer {
    @Volatile
    private var paused = false

    private val scanner =
        BarcodeScanning.getClient(
            BarcodeScannerOptions
                .Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build(),
        )

    fun pause() {
        paused = true
    }

    fun resume() {
        paused = false
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (paused || mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner
            .process(image)
            .addOnSuccessListener { barcodes -> handleBarcodes(barcodes) }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun handleBarcodes(barcodes: List<Barcode>) {
        if (paused) return
        val barcode = barcodes.firstOrNull() ?: return
        paused = true
        onDecoded(decodeBarcodeText(barcode))
    }
}

/**
 * Decodes a detected barcode's raw bytes as UTF-8 explicitly (falling back to ML Kit's
 * own decoded [Barcode.getRawValue] only if raw bytes are unavailable), then strips a
 * leading UTF-8 BOM if present, per `qr-payload-format`.
 */
private fun decodeBarcodeText(barcode: Barcode): String {
    val bytes = barcode.rawBytes
    val text = if (bytes != null) String(bytes, Charsets.UTF_8) else barcode.rawValue.orEmpty()
    return text.removePrefix(BOM.toString())
}
