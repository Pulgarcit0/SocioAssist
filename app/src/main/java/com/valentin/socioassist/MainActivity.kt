package com.valentin.socioassist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.valentin.socioassist.ui.MotoAssistApp
import com.valentin.socioassist.ui.theme.AsistenteVirtualTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AsistenteVirtualTheme {
                // Aquí llamamos a la estructura que creamos
                MotoAssistApp()
            }
        }
    }
}