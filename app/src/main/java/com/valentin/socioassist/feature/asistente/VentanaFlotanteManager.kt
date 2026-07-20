package com.valentin.socioassist.feature.asistente

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.valentin.socioassist.core.TripData
import com.valentin.socioassist.ui.FloatingOverlay
import com.valentin.socioassist.R
class VentanaFlotanteManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var composeView: ComposeView? = null

    fun mostrarOActualizar(tripData: TripData) {
        if (composeView == null) {
            // 1. Configuramos las reglas físicas de la ventana primero
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = 120 // Margen superior
            }

            // 2. Creamos la vista de Compose
            composeView = ComposeView(context).apply {
                // TRUCO: Ciclo de vida artificial para Compose fuera de una Activity
                val lifecycleOwner = MyLifecycleOwner()
                lifecycleOwner.performRestore(null)
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)

                val viewModelStore = ViewModelStore()
                setViewTreeViewModelStoreOwner(object : ViewModelStoreOwner {
                    override val viewModelStore: ViewModelStore get() = viewModelStore
                })

                // 3. AQUÍ INYECTAMOS TU COMPONENTE DEFINITIVO
                setContent {
                    FloatingOverlay(
                        tripData = tripData,
                        onDrag = { dragX, dragY ->
                            // Movemos la ventana física en Android
                            params.x += dragX.toInt()
                            params.y += dragY.toInt()
                            windowManager.updateViewLayout(this, params)
                        },
                        onClose = {
                            // Destruimos la ventana
                            ocultar()
                        }
                    )
                }
            }

            // 4. Inflamos la ventana
            try {
                windowManager.addView(composeView, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            // Si ya existe la ventana en pantalla, solo actualizamos los datos reactivamente
            composeView?.setContent {
                FloatingOverlay(
                    tripData = tripData,
                    onDrag = { dragX, dragY ->
                        val layoutParams = composeView?.layoutParams as? WindowManager.LayoutParams
                        if (layoutParams != null) {
                            layoutParams.x += dragX.toInt()
                            layoutParams.y += dragY.toInt()
                            windowManager.updateViewLayout(composeView, layoutParams)
                        }
                    },
                    onClose = {
                        ocultar()
                    }
                )
            }
        }
    }

    fun ocultar() {
        composeView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                // Ignorar si ya se cerró
            }
            composeView = null
        }
    }

    // =========================================================================
    // BOILERPLATE: El "Corazón artificial" para que Jetpack Compose lata
    // =========================================================================
    private class MyLifecycleOwner : SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle get() = lifecycleRegistry
        override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

        fun performRestore(savedState: android.os.Bundle?) {
            savedStateRegistryController.performRestore(savedState)
        }

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }
    }
}