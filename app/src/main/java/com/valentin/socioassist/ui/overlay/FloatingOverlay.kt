package com.valentin.socioassist.ui.overlay
    
    import androidx.compose.animation.core.*
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.gestures.detectDragGestures
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.automirrored.filled.Sort
    import androidx.compose.material.icons.filled.*
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.graphicsLayer
    import androidx.compose.ui.input.pointer.pointerInput
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import com.valentin.socioassist.core.TripData
    import com.valentin.socioassist.core.NivelRentabilidad
    import kotlinx.coroutines.delay
    import kotlin.time.Duration.Companion.milliseconds
    import kotlin.math.sin
    import kotlin.math.PI

    @Composable
    fun FloatingOverlay(
        tripData: TripData?,
        onDrag: (Float, Float) -> Unit,
        onClose: () -> Unit 
    ) {
        Box(
            modifier = Modifier.pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
        ) {
            if (tripData != null) {
                SocioAssistCard(
                    tripData = tripData,
                    onClose = onClose 
                )
            } else {
                WaitingTripCard()
            }
        }
    }

    @Composable
    fun WaitingTripCard() {
        
        var estaAburrido by remember { mutableStateOf(false) }

        
        LaunchedEffect(Unit) {
            delay(60000L.milliseconds)
            estaAburrido = true
        }

        
        
        
        val infiniteTransition = rememberInfiniteTransition(label = "animacion_gusano")

        
        val progreso by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart 
            ),
            label = "progreso_ciclo"
        )

        
        val vaHaciaLaDerecha = progreso <= 1f

        
        val desplazamientoX = if (vaHaciaLaDerecha) {
            (progreso * 16f) - 8f 
        } else {
            8f - ((progreso - 1f) * 16f) 
        }

        
        
        
        val desplazamientoY = (sin(progreso * PI * 6).toFloat()) * -3f

        
        
        
        val escalaX = if (vaHaciaLaDerecha) -1f else 1f

        
        val emojiFlotante = if (estaAburrido) "🐛" else "🍏"

        
        val modifierEmoji = if (estaAburrido) {
            Modifier
                .offset(x = desplazamientoX.dp, y = desplazamientoY.dp)
                .graphicsLayer(scaleX = escalaX) 
        } else {
            Modifier
        }

        Card(
            modifier = Modifier
                .padding(8.dp)
                .wrapContentSize(),
            shape = RoundedCornerShape(50),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A).copy(alpha = 0.85f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emojiFlotante,
                    fontSize = 16.sp,
                    modifier = modifierEmoji
                )
            }
        }
    }
    
    @Composable
    fun SocioAssistCard(tripData: TripData, onClose: () -> Unit) {
        
        val (badgeColor, actionColor, iconRes) = when(tripData.nivelRentabilidad) {
            NivelRentabilidad.ALTA -> Triple(Color(0xFF16A34A), Color(0xFF16A34A), Icons.Default.CheckBox) 
            NivelRentabilidad.MEDIA -> Triple(Color(0xFFD97706), Color(0xFFD97706), Icons.Default.Warning) 
            NivelRentabilidad.BAJA, NivelRentabilidad.RECHAZAR -> Triple(Color(0xFFDC2626), Color(0xFFDC2626), Icons.Default.Cancel) 
        }
    
        
        val cardBackground = Color(0xFFFFFFFF)
        val headerBackground = Color(0xFFF8F9FA)
        val iconBlue = Color(0xFF0052CC)
        val textPrimary = Color(0xFF111827)
        val textSecondary = Color(0xFF4B5563)
        val dividerColor = Color(0xFFE5E7EB)
    
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
    
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBackground)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = "App Icon",
                        tint = iconBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "SocioAssist",
                        color = textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
    
                    Spacer(modifier = Modifier.weight(1f))
    
                    
                    Card(
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = badgeColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Badge Icon",
                                tint = Color.White,
                                modifier = Modifier.size(8.dp)
                            )
                            Text(
                                text = tripData.nivelRentabilidad.name.replace("_", " "),
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
    
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = textSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                onClose() 
                            }
                    )
                }
    
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GANANCIA NETA",
                        color = textSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "$${tripData.gananciaNeta}",
                        color = textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        thickness = 1.dp,
                        color = dividerColor
                    )
    
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort, 
                                contentDescription = "Rentabilidad Icon",
                                tint = iconBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$${tripData.pagoPorKm}/km",
                                color = textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Rentabilidad",
                                color = textSecondary,
                                fontSize = 10.sp
                            )
                        }
    
                        
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(35.dp)
                                .background(dividerColor)
                        )
    
                        
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Tiempo Icon",
                                tint = iconBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${tripData.tiempoTotal} min",
                                color = textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tiempo",
                                color = textSecondary,
                                fontSize = 10.sp
                            )
                        }
    
                        
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(35.dp)
                                .background(dividerColor)
                        )
    
                        
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Ubicación Icon",
                                tint = iconBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${tripData.distanciaTotal} km",
                                color = textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Total",
                                color = textSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        thickness = 1.dp,
                        color = dividerColor
                    )
    
                    
                    Row(
                        modifier = Modifier.padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = iconRes,
                            contentDescription = "Accion Icon",
                            tint = actionColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = tripData.sugerencia,
                            color = actionColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
    
                    
                    if (tripData.nivelRentabilidad == NivelRentabilidad.BAJA || tripData.nivelRentabilidad == NivelRentabilidad.RECHAZAR) {
                        Text(
                            text = "El pago por kilómetro no es conveniente",
                            color = Color(0xFFDC2626),
                            fontSize = 10.sp
                        )
                    }
                }
    
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(dividerColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.6f)
                            .background(iconBlue)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.4f)
                    )
                }
            }
        }
    }
