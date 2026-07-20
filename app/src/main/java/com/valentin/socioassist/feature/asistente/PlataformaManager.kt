package com.valentin.socioassist.feature.asistente

import android.content.Context
import androidx.core.content.edit

object PlataformaManager {

    private const val PREFS_NAME = "MotoAssistPrefs"
    private const val KEY_PLATAFORMA = "plataforma_activa"

    // Función para guardar la plataforma seleccionada (ej. "Didi", "Uber", "inDrive")
    fun guardarPlataformaActiva(context: Context, plataforma: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_PLATAFORMA, plataforma)
        }
    }

    // Función para leer qué plataforma está activa al abrir la app
    fun obtenerPlataformaActiva(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Por defecto, podemos dejar "Didi" como la principal
        return prefs.getString(KEY_PLATAFORMA, "Didi")
    }
}