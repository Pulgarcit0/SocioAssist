package com.valentin.socioassist.core

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.valentin.socioassist.feature.asistente.PlataformaManager

enum class NivelRentabilidad {
    ALTA, MEDIA, BAJA, RECHAZAR
}

data class TripData(
    val distanciaTotal: Double,
    val tiempoTotal: Int,
    val pagoBruto: Double,
    val gananciaNeta: Double,
    val pagoPorKm: Double,
    val nivelRentabilidad: NivelRentabilidad,
    val sugerencia: String
)

class OcrCaptureManager(
    private val context: Context,
    private val onTripDetected: (TripData?) -> Unit
) {
    var isScannerPaused = false

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private var isProcessing = false
    private var ultimaCapturaTime = 0L
    private var tiempoInicioProcesamiento = 0L
    private val TAG = "MotoAssist_OCR"

    fun iniciarCaptura(resultCode: Int, dataIntent: Intent) {
        val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, dataIntent)

        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                Log.e(TAG, "🚫 ALERTA CRÍTICA: Permiso de captura revocado por el sistema.")
                super.onStop()
                detenerCaptura()
            }
        }, Handler(Looper.getMainLooper()))

        val metrics = context.resources.displayMetrics
        val width = metrics.widthPixels / 2
        val height = metrics.heightPixels / 2
        val density = metrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture", width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )

        imageReader?.setOnImageAvailableListener({ reader ->
            val image = try {
                reader.acquireLatestImage()
            } catch (_: Exception) {
                null
            }

            if (image == null) return@setOnImageAvailableListener

            if (isScannerPaused) {
                try { image.close() } catch (_: Exception) {}
                return@setOnImageAvailableListener
            }

            if (isProcessing) {
                val tiempoAtorado = System.currentTimeMillis() - tiempoInicioProcesamiento
                if (tiempoAtorado > 5000L) {
                    Log.e(TAG, "🚨 SALVAVIDAS: Forzando reinicio del OCR.")
                    isProcessing = false
                } else {
                    try { image.close() } catch (_: Exception) {}
                    return@setOnImageAvailableListener
                }
            }

            val tiempoActual = System.currentTimeMillis()
            if (tiempoActual - ultimaCapturaTime < 1000L) {
                try { image.close() } catch (_: Exception) {}
                return@setOnImageAvailableListener
            }
            ultimaCapturaTime = tiempoActual

            isProcessing = true
            tiempoInicioProcesamiento = System.currentTimeMillis()

            try {
                val bitmap = imageToBitmap(image)
                try { image.close() } catch (_: Exception) { }

                val inputImage = InputImage.fromBitmap(bitmap, 0)

                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val textoCrudo = visionText.text

                        
                        Log.d("MotoAssist_Debug", "Texto capturado por OCR:\n=================\n$textoCrudo\n=================")

                        val plataformaActiva = PlataformaManager.obtenerPlataformaActiva(context)
                        val isUberMode = (plataformaActiva == "Uber")

                        val viajeDetectado = ViajeParser.analizarTexto(context, textoCrudo, isUberMode)

                        if (viajeDetectado != null) {
                            Log.d(TAG, "✅ ¡Viaje válido encontrado!")
                            onTripDetected(viajeDetectado)
                        } else {
                            onTripDetected(null)
                        }
                    }
                    .addOnFailureListener { e -> Log.e(TAG, "❌ Fallo en ML Kit", e) }
                    .addOnCompleteListener {
                        bitmap.recycle()
                        isProcessing = false
                    }

            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Error al convertir frame", e)
                try { image.close() } catch (_: Exception) {}
                isProcessing = false
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmapOriginal = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmapOriginal.copyPixelsFromBuffer(buffer)
        val bitmapRecortado = Bitmap.createBitmap(bitmapOriginal, 0, 0, image.width, image.height)

        if (bitmapOriginal != bitmapRecortado) bitmapOriginal.recycle()
        return bitmapRecortado
    }

    fun detenerCaptura() {
        try { virtualDisplay?.release() } catch (e: Exception) {}
        try { imageReader?.close() } catch (e: Exception) {}
        try { mediaProjection?.stop() } catch (e: Exception) {}
        mediaProjection = null
    }
}