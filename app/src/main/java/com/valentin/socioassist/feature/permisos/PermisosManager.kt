package com.valentin.socioassist.feature.permisos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import com.valentin.socioassist.core.FloatingService

object PermisosManager {

    // Verifica si ya tenemos el permiso de sobreponer apps
    fun tienePermisoSuperposicion(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    // Abre la pantalla de ajustes para pedir el permiso
    fun solicitarPermisoSuperposicion(context: Context) {
        Toast.makeText(context, "Concede el permiso para mostrar sobre otras apps", Toast.LENGTH_LONG).show()
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:${context.packageName}".toUri()
        )
        context.startActivity(intent)
    }
}

// Fíjate que aquí solo hay 3 parámetros (context, onSuccess, onError). ¡Nada de 'function'!
@Composable
fun rememberScreenCaptureLauncher(
    context: Context,
    onSuccess: () -> Unit,
    onError: () -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val intent = Intent(context, FloatingService::class.java).apply {
                putExtra("RESULT_CODE", result.resultCode)
                putExtra("DATA_INTENT", result.data)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            // Disparamos la acción de éxito (cambiar el botón a azul)
            onSuccess()
        } else {
            // Disparamos la acción de error (mantener el botón en gris)
            onError()
        }
    }
}