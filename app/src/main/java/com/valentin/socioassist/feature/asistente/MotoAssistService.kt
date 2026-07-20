package com.valentin.socioassist.feature.asistente

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.valentin.socioassist.core.TripData

class MotoAssistService : Service() {

    // Aquí declaras tu manager para que viva mientras el servicio esté encendido
    private lateinit var ventanaManager: VentanaFlotanteManager

    override fun onCreate() {
        super.onCreate()
        // 1. Inicializamos el manager al arrancar el servicio
        ventanaManager = VentanaFlotanteManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // NOTA: Aquí, más adelante, pondremos la lógica para arrancar
        // la captura de pantalla (MediaProjection) y la notificación persistente.

        return START_NOT_STICKY
    }

    // Tu función que será llamada cada vez que el OCR lea algo nuevo
    fun alDetectarViaje(tripData: TripData?) {
        if (tripData != null) {
            // 2. ¡Lanzamos tu FloatingOverlay a la pantalla!
            ventanaManager.mostrarOActualizar(tripData)
        } else {
            // Si no hay viaje en pantalla, la ocultamos
            ventanaManager.ocultar()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 3. Limpiamos la ventana si apagas el servicio por completo
        ventanaManager.ocultar()
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Los Foreground Services normales no usan Bind, así que regresamos null
        return null
    }
}