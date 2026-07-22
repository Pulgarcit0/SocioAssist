package com.valentin.socioassist.core

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.valentin.socioassist.model.UsuarioMotoAssist
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

fun crearUsuarioEnFirestore(onComplete: (Boolean) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    if (currentUser != null) {
        // 1. Calculamos las fechas
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("es", "MX"))
        val fechaActual = Date()
        val fechaRegistroLegible = formatoFecha.format(fechaActual)

        // Calculamos la fecha de vencimiento sumando 7 días
        val calendar = Calendar.getInstance()
        calendar.time = fechaActual
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val fechaVencimientoLegible = formatoFecha.format(calendar.time)

        // 2. Preparamos los datos
        val nuevoUsuario = UsuarioMotoAssist(
            uid = currentUser.uid,
            nombre = currentUser.displayName ?: "Socio",
            email = currentUser.email ?: "",
            fotoUrl = currentUser.photoUrl?.toString() ?: "",
            fechaRegistro = fechaRegistroLegible,
            fechaVencimiento = fechaVencimientoLegible,
            tipoPlan = "prueba" // Plan inicial por 7 días
        )

        val usuarioRef = db.collection("Usuarios").document(currentUser.uid)

        usuarioRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                // Si el usuario no existe, lo guardamos en Firestore
                usuarioRef.set(nuevoUsuario)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            } else {
                // Si ya existe, simplemente completamos con éxito
                onComplete(true)
            }
        }.addOnFailureListener {
            onComplete(false)
        }
    } else {
        onComplete(false)
    }
}

fun actualizarConfiguracionEnFirestore(
    tarifaMin: Double,
    impuesto: Double,
    distMax: Double,
    ganancia: Double,
    onComplete: (Boolean) -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    if (currentUser != null) {
        val usuarioRef = db.collection("Usuarios").document(currentUser.uid)

        // Mapeamos exactamente los nombres que pusimos en tu Data Class
        val updates = mapOf(
            "tarifaMinima" to tarifaMin,
            "retencionImpuestos" to impuesto,
            "distanciaMaxima" to distMax,
            "gananciaNetaPorKm" to ganancia
        )

        usuarioRef.update(updates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    } else {
        onComplete(false)
    }
}