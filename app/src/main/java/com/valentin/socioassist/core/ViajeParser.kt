package com.valentin.socioassist.core

import android.content.Context
import android.util.Log

object ViajeParser {

    
    fun analizarTexto(context: Context, texto: String, isUberMode: Boolean): TripData? {

        var precioBruto = 0.0
        var distanciaTotal = 0.0
        var tiempoTotal = 0

        try {
            if (isUberMode) {
                
                
                

                val palabrasClaveUber = listOf(
                    "uber",       
                    "exclusivo",  
                    "viaje:",     
                    "flash",      
                    "comfort",    
                    "reserva"     
                )

                val esViajeUber = palabrasClaveUber.any { palabra ->
                    texto.contains(palabra, ignoreCase = true)
                }

                if (!esViajeUber) {
                    return null 
                }

                
                val precioRegex = Regex("""\$\s*(\d+[,.]?\d*)""")
                val matchPrecio = precioRegex.find(texto) ?: return null

                var brutoUber = matchPrecio.groupValues[1].replace(",", ".").toDoubleOrNull() ?: 0.0

                
                if (brutoUber > 2000.0) {
                    brutoUber /= 100
                }
                precioBruto = brutoUber

                
                val recogidaRegex = Regex("""(?:A\s*)?(\d+)\s*min\s*(?:y\s*)?\(?\s*(\d+[.,]?\d*)\s*km\)?""", RegexOption.IGNORE_CASE)
                val matchRecogida = recogidaRegex.find(texto)
                val minRecogida = matchRecogida?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val kmRecogida = matchRecogida?.groupValues?.get(2)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

                
                val viajeRegex = Regex("""Viaje:?\s*(\d+)\s*min\s*\(?\s*(\d+[.,]?\d*)\s*km\)?""", RegexOption.IGNORE_CASE)
                val matchViaje = viajeRegex.find(texto)
                val minViaje = matchViaje?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val kmViaje = matchViaje?.groupValues?.get(2)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

                distanciaTotal = kmRecogida + kmViaje
                tiempoTotal = minRecogida + minViaje

            } else {
                
                
                

                
                val palabrasClaveDidi = listOf(
                    "didi",
                    "express",      
                    "moto",         
                    "entrega",      
                    "flex",         
                    "pon tu precio" 
                )

                val esViajeDidi = palabrasClaveDidi.any { palabra ->
                    texto.contains(palabra, ignoreCase = true)
                }

                
                if (!esViajeDidi) {
                    return null
                }

                
                val regexPrecio = Regex("\\$([0-9]+\\.[0-9]{2})")
                val matchPrecio = regexPrecio.find(texto)
                precioBruto = matchPrecio?.groupValues?.get(1)?.toDoubleOrNull() ?: return null

                
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

                
                val regexMin = Regex("([0-9]+)\\s*min")
                val matchesMin = regexMin.findAll(texto)
                for (match in matchesMin) {
                    tiempoTotal += match.groupValues[1].toIntOrNull() ?: 0
                }
            }

            
            if (distanciaTotal == 0.0 || precioBruto == 0.0) return null

            
            val prefs = context.getSharedPreferences("SocioAssistPrefs", Context.MODE_PRIVATE)
            val tarifaMinConf = prefs.getFloat("tarifaMin", 35f).toDouble()
            val impuestoConf = prefs.getFloat("impuesto", 16f).toDouble() / 100.0
            val distMaxConf = prefs.getFloat("distMax", 60f).toDouble()
            val gananciaPorKmConf = prefs.getFloat("ganancia", 8.5f).toDouble()
            

            
            val impuesto = precioBruto * impuestoConf
            val gananciaNeta = precioBruto - impuesto
            val pagoPorKm = gananciaNeta / distanciaTotal

            
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
            Log.e("SocioAssist_OCR", "Error calculando los datos del viaje: ${e.message}")
            return null
        }
    }
}