package com.valentin.socioassist.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

val CardBackgroundColor = Color.White
val LightBlueCardColor = Color(0xFFF1F5F9)
val TextRedColor = Color(0xFFE02424)

@Composable
fun SettingsScreen(
    isLoggedIn: Boolean = false,
    onLogoutClick: () -> Unit,
    onManageSubscriptionClick: () -> Unit
) {
    // 1. ESTADOS PARA GUARDAR LA INFORMACIÓN DE GOOGLE
    var userName by remember { mutableStateOf("Cargando perfil...") }
    var userPhotoUrl by remember { mutableStateOf<String?>(null) }
    var calificacionMostrada by remember { mutableStateOf("4.95") }

    // 2. EFECTO PARA EXTRAER LOS DATOS AL ABRIR LA PANTALLA
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userName = currentUser.displayName ?: "Socio"
            userPhotoUrl = currentUser.photoUrl?.toString()
        } else {
            userName = "Socio"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // --- SECCIÓN DE PERFIL ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Foto de perfil usando Coil de forma segura
            AsyncImage(
                model = userPhotoUrl,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop,
                // SOLUCIÓN AL CRASH 1: Usar un icono de Compose en lugar del de Android
                fallback = rememberVectorPainter(Icons.Default.AccountCircle),
                error = rememberVectorPainter(Icons.Default.AccountCircle)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = userName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Calificación",
                        tint = PrimaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = calificacionMostrada,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(Calificación)",
                        fontSize = 14.sp,
                        color = OnSurfaceVariantColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- TARJETA SUSCRIPCIÓN ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.WorkspacePremium,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MEMBRESÍA PREMIUM",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Próxima renovación: 15 de Octubre",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurfaceColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onManageSubscriptionClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text(text = "Gestionar Suscripción", fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- TARJETA RECOMENDAR ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LightBlueCardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CardGiftcard,
                            contentDescription = null,
                            tint = Color(0xFF057A55)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Recomendar Aplicación",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceColor
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Invita a tus amigos a usar MotoAssist y obtén descuentos exclusivos en tu próxima renovación.",
                    fontSize = 14.sp,
                    color = OnSurfaceVariantColor,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { /* TODO: Compartir código */ }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Compartir Código",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- AJUSTES ---
        SettingsSection(
            items = listOf(
                SettingsItem("Notificaciones", Icons.Outlined.Notifications),
                SettingsItem("Idioma (Español)", Icons.Outlined.Language),
                SettingsItem("Seguridad", Icons.Outlined.Security)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(
            items = listOf(
                // SOLUCIÓN AL CRASH 2: Quitamos los AutoMirrored que causan conflictos
                SettingsItem("Centro de Ayuda", Icons.Outlined.HelpOutline),
                SettingsItem("Términos y Condiciones", Icons.Outlined.Description)
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- CERRAR SESIÓN ---
        OutlinedButton(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                onLogoutClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextRedColor)
        ) {
            Icon(
                // SOLUCIÓN AL CRASH 2: Quitamos el AutoMirrored del Logout
                imageVector = Icons.Outlined.Logout,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cerrar Sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

data class SettingsItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit = {}
)

@Composable
fun SettingsSection(items: List<SettingsItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { item.onClick() }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = OnSurfaceColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = item.title,
                            fontSize = 16.sp,
                            color = OnSurfaceColor
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ir",
                        tint = Color(0xFF9CA3AF)
                    )
                }

                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = Color(0xFFF3F4F6),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}