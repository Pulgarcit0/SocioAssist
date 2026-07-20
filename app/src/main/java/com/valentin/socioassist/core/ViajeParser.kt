package com.valentin.socioassist.core

import android.content.Context
import android.util.Log

object ViajeParser {

    // Le agregamos el Context como parámetro para poder leer las preferencias
    fun analizarTexto(context: Context, texto: String): TripData? {
        // 1. Filtro de seguridad: Si no es una alerta de DiDi Moto, abortamos rápido
        if (!texto.contains("Aceptar", ignoreCase = true) || !texto.contains("Moto", ignoreCase = true)) {
            return null
        }

        try {
            // 2. Extraer el precio bruto (Ej. $48.50)
            val regexPrecio = Regex("\\$([0-9]+\\.[0-9]{2})")
            val matchPrecio = regexPrecio.find(texto)
            val precioBruto = matchPrecio?.groupValues?.get(1)?.toDoubleOrNull() ?: return null

            // 3. Extraer y sumar distancias (Kilómetros y Metros)
            var distanciaTotal = 0.0

            // A) Buscar Kilómetros (Ej. "2.2km" o "2.2 km")
            val regexKm = Regex("(\\d+(?:\\.\\d+)?)\\s*km", RegexOption.IGNORE_CASE)
            val matchesKm = regexKm.findAll(texto)
            for (match in matchesKm) {
                distanciaTotal += match.groupValues[1].toDoubleOrNull() ?: 0.0
            }

            // B) Buscar Metros (Ej. "690m" o "590 m")
            val regexMetros = Regex("(\\d+)\\s*m(?![a-zA-Z])", RegexOption.IGNORE_CASE)
            val matchesMetros = regexMetros.findAll(texto)
            for (match in matchesMetros) {
                val metros = match.groupValues[1].toDoubleOrNull() ?: 0.0
                distanciaTotal += (metros / 1000.0) // Convertimos los metros a kilómetros
            }

            // 4. Extraer y sumar todos los minutos
            val regexMin = Regex("([0-9]+)\\s*min")
            val matchesMin = regexMin.findAll(texto)

            var tiempoTotal = 0
            for (match in matchesMin) {
                tiempoTotal += match.groupValues[1].toIntOrNull() ?: 0
            }

            // Si no encontró kilómetros o el precio es cero, abortamos para evitar dividir entre cero
            if (distanciaTotal == 0.0 || precioBruto == 0.0) return null

            // --- LECTURA DE PREFERENCIAS EN TIEMPO REAL ---
            val prefs = context.getSharedPreferences("MotoAssistPrefs", Context.MODE_PRIVATE)
            val tarifaMinConf = prefs.getFloat("tarifaMin", 35f).toDouble()
            val impuestoConf = prefs.getFloat("impuesto", 16f).toDouble() / 100.0
            val distMaxConf = prefs.getFloat("distMax", 60f).toDouble()
            val gananciaPorKmConf = prefs.getFloat("ganancia", 8.5f).toDouble()
            // ----------------------------------------------

            // 5. LÓGICA DE RENTABILIDAD Y MATEMÁTICAS FINANCIERAS
            // Restamos el impuesto usando el valor configurado
            val impuesto = precioBruto * impuestoConf

            // La ganancia neta libre de comisiones de la plataforma
            val gananciaNeta = precioBruto - impuesto

            // ¿A cómo te están pagando el kilómetro realmente?
            val pagoPorKm = gananciaNeta / distanciaTotal

            // 6. SEMÁFORO DE DECISIÓN DINÁMICO
            val nivel: NivelRentabilidad
            val sugerenciaTexto: String

            if (distanciaTotal > distMaxConf) {
                nivel = NivelRentabilidad.RECHAZAR
                sugerenciaTexto = "MUY LEJOS"
            } else if (gananciaNeta < tarifaMinConf) {
                nivel = NivelRentabilidad.RECHAZAR
                sugerenciaTexto = "TARIFA MUY BAJA"
            } else if (pagoPorKm >= gananciaPorKmConf) {
                nivel = NivelRentabilidad.ALTA
                sugerenciaTexto = "¡TÓMALO!"
            } else if (pagoPorKm >= (gananciaPorKmConf * 0.75)) {
                nivel = NivelRentabilidad.MEDIA
                sugerenciaTexto = "PIÉNSALO"
            } else {
                nivel = NivelRentabilidad.BAJA
                sugerenciaTexto = "RECHAZAR"
            }

            // Funciones auxiliares para redondear valores numéricos
            fun Double.redondear2Dec(): Double = Math.round(this * 100.0) / 100.0
            fun Double.redondear1Dec(): Double = Math.round(this * 10.0) / 10.0

            // 7. Empaquetamos todo
            return TripData(
                distanciaTotal = distanciaTotal.redondear1Dec(),
                tiempoTotal = tiempoTotal,
                pagoBruto = precioBruto.redondear2Dec(),
                gananciaNeta = gananciaNeta.redondear2Dec(),
                pagoPorKm = pagoPorKm.redondear2Dec(),
                nivelRentabilidad = nivel,
                sugerencia = sugerenciaTexto
            )

        } catch (e: Exception) {
            // Si la conversión de números falla en algún momento, registramos el error y soltamos el fotograma
            Log.e("MotoAssist_OCR", "Error calculando los datos del viaje: ${e.message}")
            return null
        }
    }
}