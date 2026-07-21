package com.valentin.socioassist.feature.permisos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.app.NotificationManagerCompat 
import androidx.core.net.toUri
import com.valentin.socioassist.core.FloatingService

object PermisosManager {

    
    
    
    fun tienePermisoSuperposicion(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun solicitarPermisoSuperposicion(context: Context) {
        Toast.makeText(context, "Concede el permiso para mostrar sobre otras apps", Toast.LENGTH_LONG).show()
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:${context.packageName}".toUri()
        )
        context.startActivity(intent)
    }

    
    
    
    fun tienePermisoBateria(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun solicitarPermisoBateria(context: Context) {
        if (!tienePermisoBateria(context)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = "package:${context.packageName}".toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    
    
    
    fun tienePermisoNotificaciones(context: Context): Boolean {
        val paquetesConPermiso = NotificationManagerCompat.getEnabledListenerPackages(context)
        return paquetesConPermiso.contains(context.packageName)
    }

    fun solicitarPermisoNotificaciones(context: Context) {
        Toast.makeText(context, "Concede acceso a las notificaciones para detectar viajes", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}




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
            onSuccess()
        } else {
            onError()
        }
    }
}