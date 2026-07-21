package com.valentin.socioassist.core

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

import com.valentin.socioassist.ui.overlay.FloatingOverlay

@Suppress("DEPRECATION")
class FloatingService : LifecycleService() {
    companion object {
        var isRunning = false
    }

    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView
    private val customLifecycleOwner = ServiceLifecycleOwner()
    private val currentTripState = mutableStateOf<TripData?>(null)

    private lateinit var ocrManager: OcrCaptureManager

    private val mainHandler = Handler(Looper.getMainLooper())
    private val resetRunnable = Runnable { currentTripState.value = null }
    private val TAG = "MotoAssist_Service"

    
    private var sleepJob: Job? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        isRunning = true
        super.onCreate()
        customLifecycleOwner.onCreate()
        customLifecycleOwner.onStart()

        ocrManager = OcrCaptureManager(this) { viajeDetectado ->
            if (viajeDetectado != null) {
                currentTripState.value = viajeDetectado
                mainHandler.removeCallbacks(resetRunnable)
                mainHandler.postDelayed(resetRunnable, 10000)
            } else {
                if (currentTripState.value != null) {
                    currentTripState.value = null
                    mainHandler.removeCallbacks(resetRunnable)
                }
            }
        }

        setupFloatingWindow()

        ContextCompat.registerReceiver(
            this, internalWakeUpReceiver,
            IntentFilter("com.valentin.socioassist.DESPERTAR_OCR"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        val screenIntentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        ContextCompat.registerReceiver(
            this, systemScreenReceiver, screenIntentFilter, ContextCompat.RECEIVER_EXPORTED
        )

        ocrManager.isScannerPaused = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "🟢 SERVICIO INICIADO")
        super.onStartCommand(intent, flags, startId)
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "SocioAssistChannel")
            .setContentTitle("SocioAssist")
            .setContentText("Analizando viajes...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()
        startForeground(1, notification)

        val resultCode = intent?.getIntExtra("RESULT_CODE", Activity.RESULT_CANCELED) ?: Activity.RESULT_CANCELED
        val dataIntent: Intent? = intent?.getParcelableExtra("DATA_INTENT")

        if (resultCode == Activity.RESULT_OK && dataIntent != null) {
            ocrManager.iniciarCaptura(resultCode, dataIntent)
        }
        return START_STICKY
    }

    private fun setupFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0; y = 80
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(customLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(customLifecycleOwner)
            setViewTreeViewModelStoreOwner(customLifecycleOwner)
            setContent {
                val currentTrip by currentTripState
                FloatingOverlay(
                    tripData = currentTrip,
                    onDrag = { deltaX, deltaY ->
                        params.x += deltaX.roundToInt()
                        params.y += deltaY.roundToInt()
                        windowManager.updateViewLayout(composeView, params)
                    },
                    onClose = {
                        currentTripState.value = null
                        sleepJob?.cancel() 
                        sleepJob = CoroutineScope(Dispatchers.Main).launch {
                            ocrManager.isScannerPaused = true
                            delay(3000.milliseconds)
                            ocrManager.isScannerPaused = false
                        }
                    }
                )
            }
        }
        windowManager.addView(composeView, params)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("SocioAssistChannel", "OCR Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        Log.e(TAG, "🔴 SERVICIO DESTRUIDO")
        isRunning = false
        super.onDestroy()
        sleepJob?.cancel() 

        try { unregisterReceiver(internalWakeUpReceiver) } catch (e: Exception) {}
        try { unregisterReceiver(systemScreenReceiver) } catch (e: Exception) {}
        try { if (::composeView.isInitialized) windowManager.removeView(composeView) } catch (e: Exception) {}

        ocrManager.detenerCaptura()
        try { customLifecycleOwner.onDestroy() } catch (e: Exception) {}
        stopForeground(true)
    }

    private val internalWakeUpReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.valentin.socioassist.DESPERTAR_OCR") {
                Log.d(TAG, "⏰ Despertador recibido. Reactivando...")
                ocrManager.isScannerPaused = false

                
                sleepJob?.cancel()
                sleepJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(15000.milliseconds)
                    ocrManager.isScannerPaused = true
                    Log.d(TAG, "😴 Modo ahorro: OCR en pausa.")
                }
            }
        }
    }

    private val systemScreenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d(TAG, "📱 Pantalla apagada. Pausando OCR INMEDIATAMENTE.")
                    ocrManager.isScannerPaused = true
                    sleepJob?.cancel() 
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    Log.d(TAG, "📱 Pantalla encendida. Despertando OCR...")
                    ocrManager.isScannerPaused = false

                    
                    sleepJob?.cancel()
                    sleepJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(15000.milliseconds)
                        ocrManager.isScannerPaused = true
                        Log.d(TAG, "😴 OCR vuelto a dormir por inactividad.")
                    }
                }
            }
        }
    }
}