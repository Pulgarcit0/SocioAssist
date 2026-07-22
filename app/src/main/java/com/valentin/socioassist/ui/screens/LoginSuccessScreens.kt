package com.valentin.socioassist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- PANTALLA 1: CÓDIGO VALIDADO ---
@Composable
fun CodeValidatedScreen(
    onContinueClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    SuccessScreenTemplate(
        title = "¡Código Validado!",
        subtitle = "Descuento del 10% aplicado a tu\npróxima moto.",
        showDivider = true,
        primaryButtonText = "Continuar al Dashboard",
        secondaryButtonText = "Ver detalles del beneficio",
        onPrimaryClick = onContinueClick,
        onSecondaryClick = onDetailsClick
    )
}

// --- PANTALLA 2: CONECTADO CON GOOGLE ---
@Composable
fun GoogleSuccessScreen(
    userName: String = "Valentín", // Puedes pasar el nombre que recuperes de Google aquí
    onContinueClick: () -> Unit
) {
    SuccessScreenTemplate(
        title = "¡Conectado con éxito!",
        subtitle = "Estamos preparando tu ruta,\n$userName...",
        showDivider = false,
        primaryButtonText = "Continuar a Configuración", // Agregado como pediste
        secondaryButtonText = null,
        onPrimaryClick = onContinueClick,
        onSecondaryClick = {}
    )
}

// --- PLANTILLA BASE PARA AMBAS PANTALLAS ---
// Como ambas pantallas son casi idénticas visualmente, usamos una plantilla reutilizable
@Composable
private fun SuccessScreenTemplate(
    title: String,
    subtitle: String,
    showDivider: Boolean,
    primaryButtonText: String,
    secondaryButtonText: String?,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit
) {
    val primaryBlue = Color(0xFF0058BE)
    val bgSurface = Color(0xFFF8F9FF)
    val textDark = Color(0xFF0B1C30)
    val textGray = Color(0xFF333333) // Un gris oscuro para el subtítulo

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgSurface)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- CÍRCULO AZUL CON PALOMITA ---
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFE5EEFF), CircleShape), // Fondo azul claro exterior
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(primaryBlue, CircleShape), // Círculo azul fuerte interior
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Éxito",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- TÍTULO ---
                Text(
                    text = title,
                    color = primaryBlue,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- SUBTÍTULO ---
                Text(
                    text = subtitle,
                    color = textGray,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- DIVIDER OPCIONAL ---
                if (showDivider) {
                    HorizontalDivider(
                        modifier = Modifier
                            .width(64.dp)
                            .padding(bottom = 32.dp),
                        color = Color(0xFFC2C6D6).copy(alpha = 0.5f),
                        thickness = 3.dp
                    )
                }

                // --- BOTÓN PRINCIPAL ---
                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp), // Botón más redondeado (estilo pastilla)
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(primaryButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Continuar",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // --- BOTÓN SECUNDARIO (OPCIONAL) ---
                if (secondaryButtonText != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onSecondaryClick) {
                        Text(
                            text = secondaryButtonText,
                            color = primaryBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}