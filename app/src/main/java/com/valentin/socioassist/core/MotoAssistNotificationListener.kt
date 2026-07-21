package com.valentin.socioassist.core

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class MotoAssistNotificationListener : NotificationListenerService() {

    private val TAG = "MotoAssist_Listener"

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "🎧 Oído biónico conectado. Escuchando notificaciones...")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let {
            val paquete = it.packageName
            val extras = it.notification.extras
            val titulo = extras.getString("android.title") ?: ""
            val texto = extras.getString("android.text") ?: ""

            
            if (paquete.contains("uber") || paquete.contains("didi")) {
                Log.d(TAG, "🔔 ALERTA DE VIAJE DETECTADA: $paquete")

                
                encenderPantalla()

                
                val intent = Intent("com.valentin.socioassist.DESPERTAR_OCR")
                sendBroadcast(intent)
                Log.d(TAG, "📢 Señal de despertar enviada al FloatingService")
            }
        }
    }

    private fun encenderPantalla() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "MotoAssist::AlertaViajeWakeLock"
            )

            
            wakeLock.acquire(10 * 1000L)
            Log.d(TAG, "💡 Pantalla encendida exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al intentar encender la pantalla: ${e.message}")
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "🔇 Oído desconectado.")
    }
}