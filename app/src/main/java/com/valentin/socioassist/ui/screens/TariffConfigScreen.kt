package com.valentin.socioassist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TariffConfigScreen(
    onNextClick: (String) -> Unit
) {
    var minimumTariff by remember { mutableStateOf("") }

    // Colores base
    val primaryBlue = Color(0xFF0058BE)
    val textDark = Color(0xFF0B1C30)
    val textGray = Color(0xFF585F67)
    val cardBg = Color(0xFFF4F7FB) // Color azul/gris muy claro para las tarjetas

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // --- CABECERA Y BARRA DE PROGRESO ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.TwoWheeler,
                    contentDescription = "Logo",
                    tint = primaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SocioAssist",
                    color = primaryBlue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Paso 1 de 4",
                color = textGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de progreso (25% llena)
        LinearProgressIndicator(
            progress = { 0.25f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = primaryBlue,
            trackColor = Color(0xFFE5E7EB),
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- TÍTULO Y DESCRIPCIÓN ---
        Text(
            text = "¿Cuál es tu tarifa mínima?",
            color = textDark,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Ingresa el monto mínimo por el que estás dispuesto a aceptar un viaje.",
            color = textGray,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- CAMPO DE ENTRADA DE TEXTO ---
        OutlinedTextField(
            value = minimumTariff,
            onValueChange = { minimumTariff = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("0.00", color = Color(0xFFC2C6D6), fontSize = 18.sp) },
            leadingIcon = {
                Text(
                    text = "$",
                    color = primaryBlue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), // Teclado numérico
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryBlue,
                unfocusedBorderColor = Color(0xFFC2C6D6),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = textDark,
                unfocusedTextColor = textDark
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- BOTÓN SIGUIENTE ---
        Button(
            onClick = { onNextClick(minimumTariff) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Siguiente", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Siguiente",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- TARJETAS INFORMATIVAS INFERIORES ---
        InfoFeatureCard(
            icon = Icons.Outlined.Security,
            iconTint = primaryBlue,
            title = "Datos Seguros",
            description = "Tu información financiera se utiliza localmente para optimizar tus rutas y no se comparte con terceros.",
            bgColor = cardBg
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoFeatureCard(
            icon = Icons.Outlined.BarChart,
            iconTint = Color(0xFF0F9D58), // Verde para cálculo
            title = "Cálculo Inteligente",
            description = "Calculamos el desgaste de tu moto y el costo de combustible actual para darte números reales.",
            bgColor = cardBg
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoFeatureCard(
            icon = Icons.Outlined.FlashOn,
            iconTint = textGray,
            title = "Sin Filtros",
            description = "Configura tus preferencias una vez y deja que SocioAssist filtre las mejores ofertas para ti.",
            bgColor = cardBg
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// --- COMPOSABLE PARA LAS TARJETAS DE INFORMACIÓN ---
@Composable
fun InfoFeatureCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    bgColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                color = Color(0xFF0B1C30),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                color = Color(0xFF585F67),
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}