package com.valentin.socioassist.ui.screens

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.valentin.socioassist.R
import com.valentin.socioassist.core.FloatingService
import com.valentin.socioassist.feature.permisos.PermisosManager
import com.valentin.socioassist.feature.permisos.rememberScreenCaptureLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocioAssistApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val sharedPreferences = context.getSharedPreferences("SocioAssistPrefs", Context.MODE_PRIVATE)
    var tabSeleccionada by remember { mutableIntStateOf(0) }

    val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val pantallasConMenu = listOf("home", "switch", "settings")
    val mostrarMenus = currentRoute in pantallasConMenu

    Scaffold(
        topBar = {
            if (mostrarMenus) {
                TopAppBar(
                    title = { Text("SocioAssist", fontWeight = FontWeight.Bold, color = Color(0xFF0052CC)) },
                    actions = {
                        IconButton(onClick = { /* Acción de notificaciones */ }) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = "Notificaciones")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        },
        bottomBar = {
            if (mostrarMenus) {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = tabSeleccionada == 0,
                        onClick = {
                            tabSeleccionada = 0
                            navController.navigate("home") { popUpTo("home") { inclusive = true } }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = tabSeleccionada == 1,
                        onClick = {
                            tabSeleccionada = 1
                            navController.navigate("switch") { popUpTo("home") }
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
                            navController.navigate("settings") { popUpTo("home") }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("login") {
                LoginScreen(
                    onValidateCodeClick = { _ -> // <--- Solución a la advertencia
                        navController.navigate("code_validated")
                    },
                    onGoogleLoginClick = {
                        navController.navigate("google_success")
                    }
                )
            }
            composable("code_validated") {
                CodeValidatedScreen(
                    onContinueClick = { navController.navigate("step1_tariff") },
                    onDetailsClick = { /* Mostrar modal */ }
                )
            }
            composable("google_success") {
                GoogleSuccessScreen(
                    onContinueClick = { navController.navigate("step1_tariff") }
                )
            }
            composable("step1_tariff") {
                TariffConfigScreen(
                    onNextClick = { tarifa ->
                        val tarifaFloat = tarifa.toFloatOrNull() ?: 0f
                        sharedPreferences.edit { putFloat("tarifaMin", tarifaFloat) }
                        navController.navigate("step2_distance")
                    }
                )
            }
            composable("step2_distance") {
                FilterConfigStepScreen(
                    stepNumber = 2,
                    title = "¿Distancia máxima por viaje?",
                    description = "Ingresa el máximo de kilómetros que estás dispuesto a recorrer por un solo servicio.",
                    placeholder = "Ej: 15",
                    suffixText = "km",
                    onNextClick = { distancia ->
                        val distanciaFloat = distancia.toFloatOrNull() ?: 0f
                        sharedPreferences.edit { putFloat("distMax", distanciaFloat) }
                        navController.navigate("step3_tax")
                    }
                )
            }
            composable("step3_tax") {
                FilterConfigStepScreen(
                    stepNumber = 3,
                    title = "Retención de Impuestos",
                    description = "Ingresa el porcentaje que la plataforma te retiene para calcular tu ganancia real.",
                    placeholder = "Ej: 10.1",
                    suffixText = "%",
                    onNextClick = { impuesto ->
                        val impuestoFloat = impuesto.toFloatOrNull() ?: 0f
                        sharedPreferences.edit { putFloat("impuesto", impuestoFloat) }
                        navController.navigate("step4_gain")
                    }
                )
            }
            composable("step4_gain") {
                FilterConfigStepScreen(
                    stepNumber = 4,
                    title = "Ganancia Neta",
                    description = "Ingresa cuánto esperas ganar como mínimo por cada kilómetro recorrido.",
                    placeholder = "Ej: 7.00",
                    prefixText = "$",
                    onNextClick = { ganancia ->
                        val gananciaFloat = ganancia.toFloatOrNull() ?: 0f
                        sharedPreferences.edit { putFloat("ganancia", gananciaFloat) }
                        navController.navigate("setup_complete")
                    }
                )
            }
            composable("setup_complete") {
                SetupCompleteScreen(
                    onGoToHomeClick = {
                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                    }
                )
            }
            composable("home") {
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
                SwitchScreen(
                    isServiceRunning = isServiceRunning,
                    onSetServiceRunning = { isRunning ->
                        isServiceRunning = isRunning
                        if (!isRunning) {
                            val intent = Intent(context, FloatingService::class.java)
                            context.stopService(intent)
                        }
                    },
                    onSolicitarPermiso = {
                        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
                        screenCaptureLauncher.launch(captureIntent)
                    }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onLogoutClick = {
                        navController.navigate("login") { popUpTo(0) }
                    },
                    onManageSubscriptionClick = {
                        navController.navigate("subscription")
                    }
                )
            }
            composable("subscription") {
                SubscriptionScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    isServiceRunning: Boolean,
    onSetServiceRunning: (Boolean) -> Unit,
    onSolicitarPermiso: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("SocioAssistPrefs", Context.MODE_PRIVATE)

    var tarifaMinima by remember { mutableStateOf(sharedPreferences.getFloat("tarifaMin", 25f).toString()) }
    var tarifaImpuesto by remember { mutableStateOf(sharedPreferences.getFloat("impuesto", 10.1f).toString()) }
    var distanciaMaxima by remember { mutableStateOf(sharedPreferences.getFloat("distMax", 12f).toString()) }
    var gananciaNeta by remember { mutableStateOf(sharedPreferences.getFloat("ganancia", 7f).toString()) }

    var mostrarDialogoPermiso by remember { mutableStateOf(false) }

    if (mostrarDialogoPermiso) {
        PermissionExplanationDialog(
            onDismiss = { mostrarDialogoPermiso = false },
            onConfirm = {
                mostrarDialogoPermiso = false
                PermisosManager.solicitarPermisoNotificaciones(context)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                            val intent = Intent(context, FloatingService::class.java)
                            context.stopService(intent)
                            onSetServiceRunning(false)
                            Toast.makeText(context, "Apagado", Toast.LENGTH_SHORT).show()
                        } else {
                            if (!PermisosManager.tienePermisoSuperposicion(context)) {
                                PermisosManager.solicitarPermisoSuperposicion(context)
                            } else if (!PermisosManager.tienePermisoBateria(context)) {
                                PermisosManager.solicitarPermisoBateria(context)
                                Toast.makeText(context, "Acepta ignorar la optimización de batería", Toast.LENGTH_LONG).show()
                            } else if (!PermisosManager.tienePermisoNotificaciones(context)) {
                                mostrarDialogoPermiso = true
                            } else {
                                onSolicitarPermiso()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isServiceRunning) Color(0xFF0052CC) else Color.Gray)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = "Encender/Apagar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isServiceRunning) "APAGAR" else "ENCENDER", fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Build, contentDescription = "Config", tint = Color(0xFF0052CC))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Configuraciones del Filtro", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        OutlinedTextField(
            value = tarifaMinima, onValueChange = { tarifaMinima = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(painterResource(id = R.drawable.ic_payments), null); Spacer(Modifier.width(4.dp)); Text("Tarifa Mínima por Viaje ($)") } },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(90.dp)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = tarifaImpuesto, onValueChange = { tarifaImpuesto = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(painterResource(id = R.drawable.ic_request_quote), null); Spacer(Modifier.width(4.dp)); Text("Retención de Impuestos (%)") } },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(90.dp)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = distanciaMaxima, onValueChange = { distanciaMaxima = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(painterResource(id = R.drawable.ic_route), null); Spacer(Modifier.width(4.dp)); Text("Distancia Máxima (km)") } },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(90.dp)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = gananciaNeta, onValueChange = { gananciaNeta = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(painterResource(id = R.drawable.ic_trending), null); Spacer(Modifier.width(4.dp)); Text("Ganancia Neta por km") } },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(90.dp)
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
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F1F1)),
            shape = RoundedCornerShape(90.dp),
            border = BorderStroke(2.dp, Color(0xFF0052CC))
        ) {
            Text("GUARDAR CONFIGURACIÓN", fontSize = 16.sp, color = Color(0xFF0052CC))
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

fun guardarConfiguracionFiltros(context: Context, tarifaMin: Double, impuesto: Double, distMax: Double, ganancia: Double) {
    val sharedPreferences = context.getSharedPreferences("SocioAssistPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit {
        putFloat("tarifaMin", tarifaMin.toFloat())
        putFloat("impuesto", impuesto.toFloat())
        putFloat("distMax", distMax.toFloat())
        putFloat("ganancia", ganancia.toFloat())
    }
}

@Composable
fun PermissionExplanationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.NotificationsActive, null, tint = Color(0xFF0052CC)); Spacer(Modifier.width(8.dp)); Text("Despertador Inteligente", fontWeight = FontWeight.Bold) } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Para ahorrar batería y no grabar la pantalla todo el tiempo, SocioAssist necesita escuchar las alertas de viaje de las plataformas.")
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("⚠️ Nota para dispositivos Xiaomi / Redmi / Poco:", fontWeight = FontWeight.Bold, color = Color(0xFF856404), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tu teléfono mostrará una advertencia de 'Peligro' y te pedirá esperar 10 segundos. Es solo un protocolo del sistema del fabricante. SocioAssist NO lee mensajes personales ni contraseñas.", color = Color(0xFF856404), fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC))) { Text("Continuar a Ajustes") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) } },
        shape = RoundedCornerShape(16.dp), containerColor = Color.White
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    SocioAssistApp()
}