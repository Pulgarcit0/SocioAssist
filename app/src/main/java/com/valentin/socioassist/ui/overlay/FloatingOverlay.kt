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
        onClose: () -> Unit // <-- Parámetro para cerrar
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
                MotoAssistCard(
                    tripData = tripData,
                    onClose = onClose // <-- Se lo pasamos a la tarjeta
                )
            } else {
                WaitingTripCard()
            }
        }
    }

    @Composable
    fun WaitingTripCard() {
        // 1. Estado para saber si el viaje está tardando
        var estaAburrido by remember { mutableStateOf(false) }

        // 2. Temporizador de 1 minuto
        LaunchedEffect(Unit) {
            delay(60000L.milliseconds)
            estaAburrido = true
        }

        // ==========================================
        // 3. ANIMACIÓN AVANZADA DEL GUSANITO
        // ==========================================
        val infiniteTransition = rememberInfiniteTransition(label = "animacion_gusano")

        // Creamos un ciclo que va de 0f a 2f (1 segundo de ida, 1 segundo de vuelta)
        val progreso by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart // Reinicia porque nosotros controlaremos la reversa
            ),
            label = "progreso_ciclo"
        )

        // Calculamos si está en el viaje de ida (hacia la derecha) o de regreso
        val vaHaciaLaDerecha = progreso <= 1f

        // Calculamos la posición en X (horizontal)
        val desplazamientoX = if (vaHaciaLaDerecha) {
            (progreso * 16f) - 8f // Va de -8 a 8
        } else {
            8f - ((progreso - 1f) * 16f) // Regresa de 8 a -8
        }

        // Calculamos la posición en Y (ondulado / saltitos) usando función Seno
        // Multiplicado por 6 significa que dará 3 "saltitos" de ida y 3 de vuelta.
        // Multiplicado por -3f define la altura del salto en píxeles (hacia arriba).
        val desplazamientoY = (sin(progreso * PI * 6).toFloat()) * -3f

        // Volteamos el emoji.
        // OJO: El emoji original "🐛" por defecto mira hacia la izquierda.
        // Si va a la derecha, lo volteamos (-1f). Si va a la izquierda, lo dejamos normal (1f).
        val escalaX = if (vaHaciaLaDerecha) -1f else 1f

        // 4. Elegimos el emoji
        val emojiFlotante = if (estaAburrido) "🐛" else "🍏"

        // 5. Aplicamos el movimiento y el volteo SOLO si está aburrido
        val modifierEmoji = if (estaAburrido) {
            Modifier
                .offset(x = desplazamientoX.dp, y = desplazamientoY.dp)
                .graphicsLayer(scaleX = escalaX) // ¡Esto es lo que hace que se voltee!
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
    fun MotoAssistCard(tripData: TripData, onClose: () -> Unit) {
        // Lógica dinámica para los estados (Semáforo)
        val (badgeColor, actionColor, iconRes) = when(tripData.nivelRentabilidad) {
            NivelRentabilidad.ALTA -> Triple(Color(0xFF16A34A), Color(0xFF16A34A), Icons.Default.CheckBox) // Verde
            NivelRentabilidad.MEDIA -> Triple(Color(0xFFD97706), Color(0xFFD97706), Icons.Default.Warning) // Naranja
            NivelRentabilidad.BAJA, NivelRentabilidad.RECHAZAR -> Triple(Color(0xFFDC2626), Color(0xFFDC2626), Icons.Default.Cancel) // Rojo
        }
    
        // Colores base extraídos de tu XML
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
    
                // ================= HEADER COMPACTO =================
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
                        text = "MotoAssist",
                        color = textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
    
                    Spacer(modifier = Modifier.weight(1f))
    
                    // Badge de Rentabilidad
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
    
                    // NUEVO: Botón de "X" para cerrar
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = textSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                onClose() // ¡AHORA SÍ, ESTÁ PERFECTO!
                            }
                    )
                }
    
                // ================= CONTENIDO PRINCIPAL =================
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
    
                    // LAS 3 COLUMNAS (Rentabilidad | Tiempo | Total)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Columna Rentabilidad
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort, // <-- ÍCONO ACTUALIZADO
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
    
                        // Divisor Vertical 1
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(35.dp)
                                .background(dividerColor)
                        )
    
                        // 2. Columna Tiempo (Minutos)
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
    
                        // Divisor Vertical 2
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(35.dp)
                                .background(dividerColor)
                        )
    
                        // 3. Columna Recogida (Total Distancia)
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
    
                    // ZONA DE DECISIÓN
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
    
                    // Motivo (Solo se muestra si es advertencia o rechazo)
                    if (tripData.nivelRentabilidad == NivelRentabilidad.BAJA || tripData.nivelRentabilidad == NivelRentabilidad.RECHAZAR) {
                        Text(
                            text = "El pago por kilómetro no es conveniente",
                            color = Color(0xFFDC2626),
                            fontSize = 10.sp
                        )
                    }
                }
    
                // ================= LÍNEA DE PROGRESO INFERIOR =================
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
