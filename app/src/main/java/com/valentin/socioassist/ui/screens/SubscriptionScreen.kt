package com.valentin.socioassist.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SubscriptionScreen(
    onBackClick: () -> Unit
) {
    val hasActiveSubscription = true
    var selectedPlan by remember { mutableStateOf("Mensual") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FF))
    ) {
        // --- TOP APP BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFFF8F9FF))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color(0xFF424754)
                    )
                }
                Text(
                    text = "Mi Suscripción",
                    color = Color(0xFF0058BE),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            IconButton(onClick = { /* TODO */ }) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notificaciones",
                    tint = Color(0xFF424754)
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)

        // --- CONTENIDO SCROLL ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 100.dp)
        ) {
            if (hasActiveSubscription) {
                ActiveSubscriptionCard()
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                PlanCard(
                    title = "SEMANAL", price = "$15", period = "/semana",
                    isSelected = selectedPlan == "Semanal", onClick = { selectedPlan = "Semanal" }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    PlanCard(
                        modifier = Modifier.padding(top = 10.dp),
                        title = "MENSUAL", price = "$50", period = "/mes", discount = "Ahorra un 16%",
                        isSelected = selectedPlan == "Mensual", onClick = { selectedPlan = "Mensual" }
                    )
                    Text(
                        text = "MÁS POPULAR", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .background(Color(0xFF0058BE), RoundedCornerShape(percent = 50))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                PlanCard(
                    title = "ANUAL", price = "$500", period = "/año", discount = "Ahorra más del 30% - 2 meses gratis",
                    isSelected = selectedPlan == "Anual", onClick = { selectedPlan = "Anual" }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- DETALLES DE FACTURACIÓN ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("DETALLES DE FACTURACIÓN", color = Color(0xFF585F67), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp)) {
                        Box(modifier = Modifier.size(40.dp).background(Color(0xFFE5EEFF), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = Color(0xFF0058BE))
                        }
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text("Próxima renovación", fontSize = 12.sp, color = Color(0xFF585F67), fontWeight = FontWeight.SemiBold)
                            Text("15 de Octubre, 2023", fontSize = 16.sp, color = Color(0xFF0B1C30), fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.size(40.dp).background(Color(0xFFE5EEFF), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = Color(0xFF0058BE))
                        }
                        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                            Text("Método de pago", fontSize = 12.sp, color = Color(0xFF585F67), fontWeight = FontWeight.SemiBold)
                            Text("Visa **** 1234", fontSize = 16.sp, color = Color(0xFF0B1C30), fontWeight = FontWeight.Bold)
                        }
                        Text("Editar", color = Color(0xFF0058BE), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { })
                    }
                }
            }

            // --- BENEFICIOS ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("TUS BENEFICIOS PREMIUM", color = Color(0xFF585F67), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 20.dp))
                    BenefitItem("Filtros ilimitados", "Búsqueda sin restricciones de talleres.")
                    Spacer(modifier = Modifier.height(16.dp))
                    BenefitItem("Alertas en tiempo real", "Notificaciones de tráfico y averías.")
                    Spacer(modifier = Modifier.height(16.dp))
                    BenefitItem("Estadísticas avanzadas", "Análisis detallado de tu consumo.")
                    Spacer(modifier = Modifier.height(16.dp))
                    BenefitItem("Soporte prioritario", "Atención en menos de 5 minutos.")
                }
            }

            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0058BE))
            ) {
                Text(
                    text = if (hasActiveSubscription) "Cambiar Plan" else "Confirmar Selección",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            TextButton(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 8.dp)
            ) {
                Text("Cancelar Suscripción", color = Color(0xFFBA1A1A), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ActiveSubscriptionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0058BE)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(percent = 50)).padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF4ADE80), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Activo", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Outlined.TwoWheeler, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Membresía\nPremium", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = "$9.99", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(text = " /mes", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
fun PlanCard(
    modifier: Modifier = Modifier,
    title: String,
    price: String,
    period: String,
    discount: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) Color(0xFFEFF4FF) else Color(0xFFFFFFFF)
    val strokeColor = if (isSelected) Color(0xFF0058BE) else Color(0xFFE5E7EB)
    val strokeWidth = if (isSelected) 2.dp else 1.dp
    val titleColor = if (isSelected) Color(0xFF0058BE) else Color(0xFF585F67)

    Card(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(strokeWidth, strokeColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 0.dp else 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = titleColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = price, color = Color(0xFF0B1C30), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(text = period, color = Color(0xFF585F67), fontSize = 16.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
                if (discount != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = discount, color = Color(0xFF006947), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(
                modifier = Modifier.size(24.dp).border(width = 2.dp, color = if (isSelected) Color(0xFF0058BE) else Color(0xFFC2C6D6), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) Box(modifier = Modifier.size(12.dp).background(Color(0xFF0058BE), CircleShape))
            }
        }
    }
}

@Composable
fun BenefitItem(title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(32.dp).background(Color(0xFFE6F4EA), CircleShape), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0F9D58), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B1C30))
            Text(subtitle, fontSize = 13.sp, color = Color(0xFF585F67))
        }
    }
}