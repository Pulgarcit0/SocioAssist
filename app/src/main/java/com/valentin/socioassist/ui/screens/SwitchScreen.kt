package com.valentin.socioassist.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentin.socioassist.feature.asistente.PlataformaManager

// Colores extraídos de tu Tailwind Config
val BackgroundColor = Color(0xFFF8F9FF)
val OnSurfaceColor = Color(0xFF0B1C30)
val OnSurfaceVariantColor = Color(0xFF424754)
val PrimaryColor = Color(0xFF0058BE)
val OutlineVariantColor = Color(0xFFC2C6D6)

@Composable
fun SwitchScreen(
    isServiceRunning: Boolean, // AÑADIDO: Recibimos el estado del servicio
    onSetServiceRunning: (Boolean) -> Unit, // AÑADIDO: Función para apagar/encender el servicio
    onSolicitarPermiso: () -> Unit
) {
    val context = LocalContext.current
    // Si no hay plataforma guardada, ponemos "Didi" por default
    var plataformaActiva by remember {
        mutableStateOf(PlataformaManager.obtenerPlataformaActiva(context) ?: "Didi")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Título y descripción
        Text(
            text = "Plataformas de Viaje",
            fontSize = 30.sp, // Tamaño adaptado de Tailwind headline-lg-mobile
            fontWeight = FontWeight.Bold,
            color = OnSurfaceColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Gestiona tus aplicaciones activas para recibir viajes de diferentes fuentes.",
            fontSize = 16.sp, // body-md
            color = OnSurfaceVariantColor,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Grid de Plataformas (En móvil se muestra como columna)
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Didi
            PlataformaCardHtml(
                nombre = "Didi",
                icono = Icons.Default.DirectionsCar,
                colorTema = Color(0xFFFF7D00), // Naranja exacto de Tailwind
                // AÑADIDO: Verificamos si la plataforma está activa Y el servicio está corriendo
                isActivo = (plataformaActiva == "Didi" && isServiceRunning),
                onCheckedChange = { activo ->
                    manejarCambioPlataforma(activo, "Didi", context, onSolicitarPermiso, onSetServiceRunning) { plataformaActiva = it }
                }
            )

            // Uber
            PlataformaCardHtml(
                nombre = "Uber",
                icono = Icons.Default.LocalTaxi,
                colorTema = Color(0xFF000000), // Negro
                isActivo = (plataformaActiva == "Uber" && isServiceRunning),
                onCheckedChange = { activo ->
                    manejarCambioPlataforma(activo, "Uber", context, onSolicitarPermiso, onSetServiceRunning) { plataformaActiva = it }
                }
            )

            // Cabify
            PlataformaCardHtml(
                nombre = "Cabify",
                icono = Icons.Default.AirportShuttle,
                colorTema = Color(0xFF7145D6), // Morado exacto
                isActivo = (plataformaActiva == "Cabify" && isServiceRunning),
                onCheckedChange = { activo ->
                    manejarCambioPlataforma(activo, "Cabify", context, onSolicitarPermiso, onSetServiceRunning) { plataformaActiva = it }
                }
            )

            // Rappi
            PlataformaCardHtml(
                nombre = "Rappi",
                icono = Icons.Default.TwoWheeler,
                colorTema = Color(0xFFFF441F), // Rojo exacto
                isActivo = (plataformaActiva == "Rappi" && isServiceRunning),
                onCheckedChange = { activo ->
                    manejarCambioPlataforma(activo, "Rappi", context, onSolicitarPermiso, onSetServiceRunning) { plataformaActiva = it }
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Banner "Próximamente" estilo Bento HTML
        BannerProximamenteHtml()

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// Función auxiliar para no repetir la lógica del manager en cada tarjeta
private fun manejarCambioPlataforma(
    activo: Boolean,
    nombrePlataforma: String,
    context: android.content.Context,
    onSolicitarPermiso: () -> Unit,
    onSetServiceRunning: (Boolean) -> Unit,
    setPlataformaActiva: (String) -> Unit
) {
    if (activo) {
        setPlataformaActiva(nombrePlataforma)
        PlataformaManager.guardarPlataformaActiva(context, nombrePlataforma)
        onSolicitarPermiso()
    } else {
        // Apagamos el servicio global si el usuario desactiva el switch
        onSetServiceRunning(false)
    }
}

@Composable
fun PlataformaCardHtml(
    nombre: String,
    icono: ImageVector,
    colorTema: Color,
    isActivo: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    // Si está inactivo, bajamos la opacidad del texto como en tu script JS
    val opacidad = if (isActivo) 1f else 0.7f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), // rounded-xl de Tailwind
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariantColor.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActivo) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Círculo con el icono y fondo al 10% de opacidad
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colorTema.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = nombre,
                        tint = colorTema,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Custom Switch imitando Tailwind
                Switch(
                    checked = isActivo,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryColor,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFD3E4FE), // surface-variant
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = nombre,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurfaceColor,
                modifier = Modifier.alpha(opacidad)
            )
            Text(
                text = if (isActivo) "Activo - Recibiendo viajes" else "Inactivo",
                fontSize = 14.sp,
                color = OnSurfaceVariantColor,
                modifier = Modifier.alpha(opacidad)
            )
        }
    }
}

@Composable
fun BannerProximamenteHtml() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp), // rounded-2xl
        colors = CardDefaults.cardColors(containerColor = PrimaryColor.copy(alpha = 0.05f)), // bg-primary-container/10
        border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(32.dp) // p-8
        ) {
            // Etiqueta (Badge)
            Surface(
                color = PrimaryColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Funciones Próximamente",
                        fontSize = 12.sp,
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Integración Automática de Tarifas",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurfaceColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Estamos trabajando duro para ofrecerte una vista unificada de las tarifas en tiempo real de todas tus plataformas activas. ¡Mantente atento!",
                fontSize = 16.sp,
                color = OnSurfaceVariantColor,
                lineHeight = 24.sp
            )
        }
    }
}