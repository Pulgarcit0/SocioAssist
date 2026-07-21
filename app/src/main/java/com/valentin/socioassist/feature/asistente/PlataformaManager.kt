package com.valentin.socioassist.feature.asistente

import android.content.Context
import androidx.core.content.edit

object PlataformaManager {

    private const val PREFS_NAME = "SocioAssistPrefs"
    private const val KEY_PLATAFORMA = "plataforma_activa"

    
    fun guardarPlataformaActiva(context: Context, plataforma: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_PLATAFORMA, plataforma)
        }
    }

    
    fun obtenerPlataformaActiva(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        return prefs.getString(KEY_PLATAFORMA, "Didi")
    }
}