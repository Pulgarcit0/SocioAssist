package com.valentin.socioassist.core

import android.content.Context
import android.util.Log

object ViajeParser {

    // Añadimos 'isUberMode' para saber el estado del switch
    fun analizarTexto(context: Context, texto: String, isUberMode: Boolean): TripData? {

        var precioBruto = 0.0
        var distanciaTotal = 0.0
        var tiempoTotal = 0

        try {
            if (isUberMode) {
                // ==========================================
                // LÓGICA PARA UBER MOTO / UBER X
                // ==========================================

                val palabrasClaveUber = listOf(
                    "uber",       // Atrapa UberX, Uber Moto, Uber XL, Uber Planet...
                    "exclusivo",  // Etiquetas de prioridad de la plataforma
                    "viaje:",     // Formato de detalle específico
                    "flash",      // Servicio de entregas y paquetería
                    "comfort",    // Autos preferenciales
                    "reserva"     // Viajes programados anticipadamente
                )

                val esViajeUber = palabrasClaveUber.any { palabra ->
                    texto.contains(palabra, ignoreCase = true)
                }

                if (!esViajeUber) {
                    return null // Si no encuentra ninguna de las palabras, lo descarta
                }

                // 1. Extrae el precio (ej. "$19744" o "$197.44")
                val precioRegex = Regex("""\$\s*(\d+[,.]?\d*)""")
                val matchPrecio = precioRegex.find(texto) ?: return null

                var brutoUber = matchPrecio.groupValues[1].replace(",", ".").toDoubleOrNull() ?: 0.0

                // PARCHE OCR: Si lee un precio exagerado por comerse el punto decimal (ej. 19744)
                if (brutoUber > 2000.0) {
                    brutoUber /= 100
                }
                precioBruto = brutoUber

                // 2. Extrae tiempo y distancia de recogida (ej. "A3 min y (0.8 km)")
                val recogidaRegex = Regex("""(?:A\s*)?(\d+)\s*min\s*(?:y\s*)?\(?\s*(\d+[.,]?\d*)\s*km\)?""", RegexOption.IGNORE_CASE)
                val matchRecogida = recogidaRegex.find(texto)
                val minRecogida = matchRecogida?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val kmRecogida = matchRecogida?.groupValues?.get(2)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

                // 3. Extrae tiempo y distancia del trayecto (ej. "Viaje: 51 min (37.2 km)")
                val viajeRegex = Regex("""Viaje:?\s*(\d+)\s*min\s*\(?\s*(\d+[.,]?\d*)\s*km\)?""", RegexOption.IGNORE_CASE)
                val matchViaje = viajeRegex.find(texto)
                val minViaje = matchViaje?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val kmViaje = matchViaje?.groupValues?.get(2)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

                distanciaTotal = kmRecogida + kmViaje
                tiempoTotal = minRecogida + minViaje

            } else {
                // ==========================================
                // LÓGICA PARA DIDI (AUTOS, MOTOS Y ENTREGAS)
                // ==========================================

                // Filtro de seguridad: Buscamos huellas exclusivas de DiDi
                val palabrasClaveDidi = listOf(
                    "didi",
                    "express",      // Autos estándar
                    "moto",         // Motocicletas
                    "entrega",      // Paquetería
                    "flex",         // Tarifas negociables
                    "pon tu precio" // Modalidad de oferta
                )

                val esViajeDidi = palabrasClaveDidi.any { palabra ->
                    texto.contains(palabra, ignoreCase = true)
                }

                // Si no encuentra ninguna palabra clave de DiDi, o si es un texto vacío, lo descarta
                if (!esViajeDidi) {
                    return null
                }

                // Extraer el precio bruto (Ej. $48.50)
                val regexPrecio = Regex("\\$([0-9]+\\.[0-9]{2})")
                val matchPrecio = regexPrecio.find(texto)
                precioBruto = matchPrecio?.groupValues?.get(1)?.toDoubleOrNull() ?: return null

                // Buscar Kilómetros y Metros de DiDi
                val regexKm = Regex("(\\d+(?:\\.\\d+)?)\\s*km", RegexOption.IGNORE_CASE)
                val matchesKm = regexKm.findAll(texto)
                for (match in matchesKm) {
                    distanciaTotal += match.groupValues[1].toDoubleOrNull() ?: 0.0
                }

                val regexMetros = Regex("(\\d+)\\s*m(?![a-zA-Z])", RegexOption.IGNORE_CASE)
                val matchesMetros = regexMetros.findAll(texto)
                for (match in matchesMetros) {
                    val metros = match.groupValues[1].toDoubleOrNull() ?: 0.0
                    distanciaTotal += (metros / 1000.0)
                }

                // Extraer minutos para DiDi
                val regexMin = Regex("([0-9]+)\\s*min")
                val matchesMin = regexMin.findAll(texto)
                for (match in matchesMin) {
                    tiempoTotal += match.groupValues[1].toIntOrNull() ?: 0
                }
            }

            // Validar que tengamos datos reales antes de hacer matemáticas
            if (distanciaTotal == 0.0 || precioBruto == 0.0) return null

            // --- LECTURA DE PREFERENCIAS EN TIEMPO REAL ---
            val prefs = context.getSharedPreferences("MotoAssistPrefs", Context.MODE_PRIVATE)
            val tarifaMinConf = prefs.getFloat("tarifaMin", 35f).toDouble()
            val impuestoConf = prefs.getFloat("impuesto", 16f).toDouble() / 100.0
            val distMaxConf = prefs.getFloat("distMax", 60f).toDouble()
            val gananciaPorKmConf = prefs.getFloat("ganancia", 8.5f).toDouble()
            // ----------------------------------------------

            // 5. LÓGICA DE RENTABILIDAD Y MATEMÁTICAS FINANCIERAS
            val impuesto = precioBruto * impuestoConf
            val gananciaNeta = precioBruto - impuesto
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
            Log.e("MotoAssist_OCR", "Error calculando los datos del viaje: ${e.message}")
            return null
        }
    }
}