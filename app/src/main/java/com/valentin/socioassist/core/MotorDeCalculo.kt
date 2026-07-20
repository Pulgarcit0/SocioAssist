package com.valentin.socioassist.core

import kotlin.math.round

// Esta "data class" es el paquetito que le mandaremos a la ventana flotante
data class ResultadoViaje(
    val esRentable: Boolean,
    val gananciaNeta: Double,
    val pagoPorKm: Double,
    val distanciaTotal: Double,
    val motivoRechazo: String? = null // Nos dirá por qué falló (ej. "Muy lejos", "Paga poco")
)

object MotorDeCalculo {

    /**
     * @param tarifaBrutaPantalla Lo que cobró Uber/DiDi (ej. 157.65)
     * @param kmRecogida Distancia hacia el pasajero (ej. 0.3)
     * @param kmViaje Distancia del viaje en sí (ej. 5.9)
     * @param impuestoRetencion Tu configuración de HomeScreen (ej. 10.1%)
     * @param gananciaNetaDeseada Tu configuración de HomeScreen (ej. 7.0 $/km)
     * @param distanciaMaxima Tu configuración (ej. 12.0 km)
     * @param tarifaMinima Tu configuración (ej. 25.0 $)
     */
    fun evaluarViaje(
        tarifaBrutaPantalla: Double,
        kmRecogida: Double,
        kmViaje: Double,
        impuestoRetencion: Double,
        gananciaNetaDeseada: Double,
        distanciaMaxima: Double,
        tarifaMinima: Double
    ): ResultadoViaje {

        // 1. Sumar distancias
        val distanciaTotal = kmRecogida + kmViaje

        // Prevenir error matemático si la distancia es 0
        if (distanciaTotal <= 0.0) {
            return ResultadoViaje(false, 0.0, 0.0, 0.0, "Error: Distancia 0")
        }

        // 2. Calcular la ganancia real (quitando el porcentaje de impuestos)
        val descuentoImpuesto = tarifaBrutaPantalla * (impuestoRetencion / 100)
        val gananciaNeta = tarifaBrutaPantalla - descuentoImpuesto

        // 3. Calcular cuánto nos pagan por kilómetro real
        val pagoPorKm = gananciaNeta / distanciaTotal

        // 4. Evaluar contra TODOS tus filtros de la HomeScreen
        var esRentable = true
        var motivoRechazo: String? = null

        if (distanciaTotal > distanciaMaxima) {
            esRentable = false
            motivoRechazo = "Excede distancia máx"
        } else if (gananciaNeta < tarifaMinima) {
            esRentable = false
            motivoRechazo = "No llega a tarifa mín"
        } else if (pagoPorKm < gananciaNetaDeseada) {
            esRentable = false
            motivoRechazo = "Paga muy poco por km"
        }

        // 5. Redondear a 2 decimales para que se vea bonito en la ventana
        return ResultadoViaje(
            esRentable = esRentable,
            gananciaNeta = redondear(gananciaNeta),
            pagoPorKm = redondear(pagoPorKm),
            distanciaTotal = redondear(distanciaTotal),
            motivoRechazo = motivoRechazo
        )
    }

    // Función auxiliar para redondear a 2 decimales
    private fun redondear(valor: Double): Double {
        return round(valor * 100) / 100
    }
}