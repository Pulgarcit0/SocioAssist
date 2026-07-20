package com.valentin.socioassist.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.valentin.socioassist.R
import com.valentin.socioassist.core.FloatingService
import com.valentin.socioassist.feature.permisos.PermisosManager
import com.valentin.socioassist.feature.permisos.rememberScreenCaptureLauncher
import com.valentin.socioassist.ui.screens.SwitchScreen

// 1. EL MARCO PRINCIPAL (Con el sistema de navegación)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotoAssistApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    var tabSeleccionada by remember { mutableIntStateOf(0) }

    // --- MOVIMOS ESTO AQUÍ ARRIBA PARA QUE TODAS LAS PANTALLAS LO PUEDAN USAR ---
    val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as android.media.projection.MediaProjectionManager
    var isServiceRunning by remember { mutableStateOf(false) }

    val screenCaptureLauncher = rememberScreenCaptureLauncher(
        context = context,
        onSuccess = {
            isServiceRunning = true
            Toast.makeText(context, "Asistente y OCR listos", Toast.LENGTH_SHORT).show()
        },
        onError = {
            isServiceRunning = false
            Toast.makeText(context, "Permiso de grabación denegado", Toast.LENGTH_SHORT).show()
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("MotoAssist", fontWeight = FontWeight.Bold, color = Color(0xFF0052CC))
                },
                actions = {
                    IconButton(onClick = { /* Acción de notificaciones */ }) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = "Notificaciones")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = tabSeleccionada == 0,
                    onClick = {
                        tabSeleccionada = 0
                        navController.navigate("home")
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = tabSeleccionada == 1,
                    onClick = {
                        tabSeleccionada = 1
                        navController.navigate("switch")
                    },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Switch") },
                    label = { Text("Switch") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFF0052CC).copy(alpha = 0.2f),
                        selectedIconColor = Color(0xFF0052CC),
                        selectedTextColor = Color(0xFF0052CC)
                    )
                )

                NavigationBarItem(
                    selected = tabSeleccionada == 2,
                    onClick = {
                        tabSeleccionada = 2
                        navController.navigate("settings")
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                // Le pasamos el estado y la función a la pantalla de Inicio
                HomeScreen(
                    isServiceRunning = isServiceRunning,
                    onSetServiceRunning = { isServiceRunning = it },
                    onSolicitarPermiso = {
                        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
                        screenCaptureLauncher.launch(captureIntent)
                    }
                )
            }
            composable("switch") {
                // Le pasamos la función a la pantalla de Switch
                SwitchScreen(
                    onSolicitarPermiso = {
                        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
                        screenCaptureLauncher.launch(captureIntent)
                    }
                )
            }
            composable("settings") { SettingsScreen() }
        }
    }
}

// 2. TU PANTALLA PRINCIPAL
@Composable
fun HomeScreen(
    isServiceRunning: Boolean,
    onSetServiceRunning: (Boolean) -> Unit,
    onSolicitarPermiso: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MotoAssistPrefs", Context.MODE_PRIVATE)

    // Cargamos los valores guardados (si no existen, pone los valores por defecto)
    var tarifaMinima by remember { mutableStateOf(sharedPreferences.getFloat("tarifaMin", 25f).toString()) }
    var tarifaImpuesto by remember { mutableStateOf(sharedPreferences.getFloat("impuesto", 10.1f).toString()) }
    var distanciaMaxima by remember { mutableStateOf(sharedPreferences.getFloat("distMax", 12f).toString()) }
    var gananciaNeta by remember { mutableStateOf(  sharedPreferences.getFloat("ganancia", 7f).toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- LA TARJETA DE ESTADO DINÁMICA ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isServiceRunning) "Estado: Encendido" else "Estado: Apagado",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isServiceRunning) Color(0xFF0052CC) else Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isServiceRunning) "Analizando la pantalla..." else "Listo para recibir viajes",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    text = "El asistente está escaneando solicitudes en segundo plano",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isServiceRunning) {
                            // APAGAR EL SERVICIO
                            val intent = Intent(context, FloatingService::class.java)
                            context.stopService(intent)
                            onSetServiceRunning(false)
                            Toast.makeText(context, "Apagado", Toast.LENGTH_SHORT).show()
                        } else {
                            // ENCENDER EL SERVICIO
                            if (!PermisosManager.tienePermisoSuperposicion(context)) {
                                PermisosManager.solicitarPermisoSuperposicion(context)
                            } else {
                                onSolicitarPermiso()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isServiceRunning) Color(0xFF0052CC) else Color.Gray
                    )
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = "Encender/Apagar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isServiceRunning) "APAGAR" else "ENCENDER",
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- CONFIGURACIONES DEL FILTRO ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Build, contentDescription = "Config", tint = Color(0xFF0052CC))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Configuraciones del Filtro", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        OutlinedTextField(
            value = tarifaMinima,
            onValueChange = { tarifaMinima = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_payments), contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tarifa Mínima por Viaje ($)")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(90.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = tarifaImpuesto,
            onValueChange = { tarifaImpuesto = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_request_quote), contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retención de Impuestos (%)")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(90.dp)
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = distanciaMaxima,
            onValueChange = { distanciaMaxima = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_route), contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Distancia Máxima (km)")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(90.dp)
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = gananciaNeta,
            onValueChange = { gananciaNeta = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_trending), contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ganancia Neta por km")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(90.dp)
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val tarifaMin = tarifaMinima.toDoubleOrNull() ?: 0.0
                val impuesto = tarifaImpuesto.toDoubleOrNull() ?: 0.0
                val distMax = distanciaMaxima.toDoubleOrNull() ?: 0.0
                val ganancia = gananciaNeta.toDoubleOrNull() ?: 0.0

                guardarConfiguracionFiltros(context, tarifaMin, impuesto, distMax, ganancia)

                Toast.makeText(context, "¡Configuración guardada con éxito!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F1F1)),
            shape = RoundedCornerShape(90.dp),
            border = BorderStroke(2.dp, Color(0xFF0052CC))
        ) {
            Text("GUARDAR CONFIGURACIÓN", fontSize = 16.sp, color = Color(0xFF0052CC))
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// 3. PANTALLA DE RELLENO (Solo queda Settings)
@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pantalla de Configuración en construcción ⚙️", fontSize = 24.sp, color = Color.Gray)
    }
}

// 4. FUNCIÓN PARA GUARDAR PREFERENCIAS
fun guardarConfiguracionFiltros(context: Context, tarifaMin: Double, impuesto: Double, distMax: Double, ganancia: Double) {
    val sharedPreferences = context.getSharedPreferences("MotoAssistPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit {
        putFloat("tarifaMin", tarifaMin.toFloat())
        putFloat("impuesto", impuesto.toFloat())
        putFloat("distMax", distMax.toFloat())
        putFloat("ganancia", ganancia.toFloat())
    }
}

// 5. PREVIEW
@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    MotoAssistApp()
}