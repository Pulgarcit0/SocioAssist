package com.valentin.socioassist.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onValidateCodeClick: (String) -> Unit,
    onGoogleLoginClick: () -> Unit
) {
    // Estado para guardar lo que el usuario escribe en el campo de código
    var inviteCode by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // Colores base de SocioAssist
    val primaryBlue = Color(0xFF0058BE)
    val bgSurface = Color(0xFFF8F9FF)
    val textDark = Color(0xFF0B1C30)
    val textGray = Color(0xFF585F67)
    val outlineGray = Color(0xFFC2C6D6)

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
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- ICONO CIRCULAR SUPERIOR ---
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(primaryBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TwoWheeler, // Ícono de moto
                        contentDescription = "SocioAssist Logo",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- TÍTULO Y SUBTÍTULO ---
                Text(
                    text = "SocioAssist",
                    color = primaryBlue,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tu compañero de ruta confiable.\nInicia sesión para continuar.",
                    color = textGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- CAMPO DE CÓDIGO DE INVITACIÓN ---
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Código de Invitación",
                        color = textDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = inviteCode,
                        onValueChange = { inviteCode = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Introduce tu código", color = outlineGray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LocalOffer, // Ícono de etiqueta
                                contentDescription = null,
                                tint = textGray
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = outlineGray,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- BOTÓN VALIDAR ---
                Button(
                    onClick = { onValidateCodeClick(inviteCode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                ) {
                    Text("Validar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- SEPARADOR "O CONTINUAR CON" ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = outlineGray.copy(alpha = 0.5f))
                    Text(
                        text = "O CONTINUAR CON",
                        color = textGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = outlineGray.copy(alpha = 0.5f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- BOTÓN DE GOOGLE ---
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val exito = iniciarSesionConGoogle(context)
                            if (exito) {
                                // Si Firebase lo aprueba, ejecutamos tu navegación
                                onGoogleLoginClick()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, outlineGray.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Letra 'G' con colores de Google como placeholder del logo
                        Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Continuar con Google", color = textDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- TEXTO DE TÉRMINOS Y CONDICIONES ---
                val annotatedString = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = textGray)) {
                        append("Al continuar, aceptas nuestros ")
                    }
                    pushStringAnnotation(tag = "TOS", annotation = "TOS")
                    withStyle(style = SpanStyle(color = primaryBlue, fontWeight = FontWeight.Bold)) {
                        append("Términos de\nServicio")
                    }
                    pop()
                    withStyle(style = SpanStyle(color = textGray)) {
                        append(" y ")
                    }
                    pushStringAnnotation(tag = "PRIVACY", annotation = "PRIVACY")
                    withStyle(style = SpanStyle(color = primaryBlue, fontWeight = FontWeight.Bold)) {
                        append("Política de Privacidad")
                    }
                    pop()
                    withStyle(style = SpanStyle(color = textGray)) {
                        append(".")
                    }
                }

                ClickableText(
                    text = annotatedString,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    ),
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(tag = "TOS", start = offset, end = offset)
                            .firstOrNull()?.let {
                                // TODO: Navegar a Términos de Servicio
                            }
                        annotatedString.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                            .firstOrNull()?.let {
                                // TODO: Navegar a Política de Privacidad
                            }
                    }
                )
            }
        }
    }
}

suspend fun iniciarSesionConGoogle(context: Context): Boolean {
    val credentialManager = CredentialManager.create(context)

    // Aquí va tu Web Client ID exacto
    val webClientId = "670654947146-36ebjq7qhhgncqi09jeun28mrgqtdnlp.apps.googleusercontent.com"

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        // 1. Mostrar la ventanita nativa de Google
        val result = credentialManager.getCredential(context = context, request = request)
        val credential = result.credential

        // 2. Extraer el token de seguridad
        if (credential is androidx.credentials.CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            // 3. Mandar el token a Firebase para registrar al usuario
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()

            Log.d("SocioAssistAuth", "¡Inicio de sesión exitoso!")
            true
        } else {
            Log.e("SocioAssistAuth", "Credencial no reconocida")
            false
        }
    } catch (e: Exception) {
        Log.e("SocioAssistAuth", "Error al iniciar sesión: ${e.message}")
        false
    }
}