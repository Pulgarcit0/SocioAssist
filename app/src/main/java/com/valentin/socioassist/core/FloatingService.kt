package com.valentin.socioassist.core

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.math.roundToInt

// IMPORTS PARA LA CORRUTINA (Pausa de 3 segundos)
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

// IMPORT PARA LEER EL ESTADO DEL SWITCH
import com.valentin.socioassist.feature.asistente.PlataformaManager
import com.valentin.socioassist.ui.overlay.FloatingOverlay

// Modelo de datos global para el servicio y la UI
enum class NivelRentabilidad {
    ALTA, MEDIA, BAJA, RECHAZAR // Ajusta estos según los que uses en tu ViajeParser
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

@Suppress("DEPRECATION")
class FloatingService : LifecycleService() {
    companion object {
        var isRunning = false
    }
    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView
    private val customLifecycleOwner = ServiceLifecycleOwner()
    private val currentTripState = mutableStateOf<TripData?>(null)
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var isProcessing = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val resetRunnable = Runnable {
        currentTripState.value = null
    }

    // NUEVO: Bandera para la pausa de 3 segundos al presionar "X"
    private var isScannerPaused = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        isRunning = true
        super.onCreate()
        customLifecycleOwner.onCreate()
        customLifecycleOwner.onStart()
        setupFloatingWindow()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "MotoAssistChannel")
            .setContentTitle("MotoAssist")
            .setContentText("Analizando viajes...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()
        startForeground(1, notification)

        val resultCode = intent?.getIntExtra("RESULT_CODE", Activity.RESULT_CANCELED) ?: Activity.RESULT_CANCELED
        val dataIntent: Intent? = intent?.getParcelableExtra("DATA_INTENT")

        if (resultCode == Activity.RESULT_OK && dataIntent != null) {
            iniciarCapturaDePantalla(resultCode, dataIntent)
        }

        return START_STICKY
    }

    private fun setupFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 80
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(customLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(customLifecycleOwner)
            setViewTreeViewModelStoreOwner(customLifecycleOwner)
            setContent {
                val currentTrip by currentTripState

                FloatingOverlay(
                    tripData = currentTrip,
                    onDrag = { deltaX: Float, deltaY: Float ->
                        params.x += deltaX.roundToInt()
                        params.y += deltaY.roundToInt()
                        windowManager.updateViewLayout(composeView, params)
                    },
                    onClose = {
                        // 1. Regresamos la tarjeta visual a "Esperando viaje..."
                        currentTripState.value = null

                        // 2. Iniciamos la pausa de 3 segundos
                        CoroutineScope(Dispatchers.Main).launch {
                            isScannerPaused = true
                            delay(3000.milliseconds) // Pausa de 3000 milisegundos
                            isScannerPaused = false
                        }
                    }
                )
            }
        }
        windowManager.addView(composeView, params)
    }

    private fun iniciarCapturaDePantalla(resultCode: Int, dataIntent: Intent) {
        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, dataIntent)

        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
                virtualDisplay?.release()
                imageReader?.close()
                mediaProjection = null
            }
        }, Handler(Looper.getMainLooper()))

        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
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

            // ========================================================
            // NUEVO CANDADO: Si estamos en la pausa de 3 segundos,
            // descartamos el frame inmediatamente y salimos.
            // ========================================================
            if (isScannerPaused) {
                try { image.close() } catch (_: Exception) {}
                return@setOnImageAvailableListener
            }

            if (isProcessing) {
                try { image.close() } catch (_: Exception) {}
                return@setOnImageAvailableListener
            }

            isProcessing = true

            try {
                val bitmap = imageToBitmap(image)

                try { image.close() } catch (_: Exception) { }

                val inputImage = InputImage.fromBitmap(bitmap, 0)

                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val textoCrudo = visionText.text

                        // ==========================================
                        // 🚀 LOGS DE DEPURACIÓN (DEBUG)
                        // ==========================================
                        Log.d("MotoAssist_Debug", "========================================")
                        Log.d("MotoAssist_Debug", "📸 TEXTO CAPTURADO POR OCR:")
                        Log.d("MotoAssist_Debug", "\n$textoCrudo")
                        Log.d("MotoAssist_Debug", "========================================")

                        // 1. LEEMOS EL SWITCH EN TIEMPO REAL
                        val plataformaActiva = PlataformaManager.obtenerPlataformaActiva(this@FloatingService)
                        val isUberMode = (plataformaActiva == "Uber")

                        Log.d("MotoAssist_Debug", "⚙️ Modo actual detectado: $plataformaActiva (isUberMode = $isUberMode)")

                        // 2. LLAMAMOS AL PARSER CON EL MODO CORRECTO
                        val viajeDetectado = ViajeParser.analizarTexto(
                            context = this@FloatingService,
                            texto = textoCrudo,
                            isUberMode = isUberMode
                        )

                        if (viajeDetectado != null) {
                            Log.d("MotoAssist_OCR", "✅ ¡Viaje válido de $plataformaActiva encontrado! Actualizando tarjeta.")
                            currentTripState.value = viajeDetectado

                            mainHandler.removeCallbacks(resetRunnable)
                            mainHandler.postDelayed(resetRunnable, 10000)
                        } else {
                            if (currentTripState.value != null) {
                                Log.d("MotoAssist_OCR", "🧹 El viaje desapareció. Limpiando tarjeta al instante.")
                                currentTripState.value = null
                                mainHandler.removeCallbacks(resetRunnable)
                            } else {
                                Log.d("MotoAssist_Debug", "⚠️ El Parser devolvió NULL. Las expresiones regulares no encontraron coincidencias.")
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("MotoAssist_OCR", "❌ Fallo al procesar imagen con ML Kit", e)
                    }
                    .addOnCompleteListener {
                        bitmap.recycle()
                        isProcessing = false
                    }

            } catch (e: Exception) {
                Log.e("MotoAssist_OCR", "⚠️ Error al convertir/leer el frame", e)
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

        val bitmap = createBitmap(image.width + rowPadding / pixelStride, image.height)
        bitmap.copyPixelsFromBuffer(buffer)
        return Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MotoAssistChannel",
                "Servicio de Lectura OCR",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        isRunning = false // MARCAMOS COMO APAGADO
        super.onDestroy()

        // Protegemos la remoción de la vista con un try-catch para evitar que se trabe
        try {
            if (::composeView.isInitialized) {
                windowManager.removeView(composeView)
            }
        } catch (e: Exception) {
            Log.e("MotoAssist", "Error al remover la ventana flotante", e)
        }

        // Cerramos los recursos de forma segura
        try { virtualDisplay?.release() } catch (e: Exception) {}
        try { imageReader?.close() } catch (e: Exception) {}
        try { mediaProjection?.stop() } catch (e: Exception) {}
        try { customLifecycleOwner.onDestroy() } catch (e: Exception) {}

        // Obligamos a Android a matar la notificación y el servicio de primer plano
        stopForeground(true)
    }
}