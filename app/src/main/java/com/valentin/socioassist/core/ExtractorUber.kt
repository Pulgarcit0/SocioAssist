package com.valentin.socioassist.core // Cambia esto si el nombre de tu paquete es distinto

// Esta pequeña clase es solo para empaquetar los 3 números que vamos a sacar
data class DatosExtraidos(
    val tarifaBruta: Double,
    val kmRecogida: Double,
    val kmViaje: Double
)

object ExtractorUber {

    // 1. Regex para el Precio: Busca el signo de dólar y atrapa los números (ej. "$197.44")
    private val regexPrecio = "\\\$([0-9]+(?:\\.[0-9]{1,2})?)".toRegex()

    // 2. Regex para Recogida (2026): Busca el patrón exacto "A X min y (Y km)" y atrapa la Y
    private val regexRecogida = "A\\s+\\d+\\s+min\\s+y\\s+\\(([0-9.]+)\\s+km\\)".toRegex()

    // 3. Regex para el Viaje (2026): Busca "Viaje: X min (Y km)" y atrapa la Y
    private val regexViaje = "Viaje:\\s+\\d+\\s+min\\s+\\(([0-9.]+)\\s+km\\)".toRegex()

    /**
     * @param textoPantalla Todo el texto crudo que el servicio de accesibilidad logre leer
     * @return Los 3 datos limpios, o 'null' si no detectó que sea una alerta de viaje válida
     */
    fun extraerDatos(textoPantalla: String): DatosExtraidos? {
        try {
            // Mandamos a los sabuesos a buscar en el texto
            val matchPrecio = regexPrecio.find(textoPantalla)
            val matchRecogida = regexRecogida.find(textoPantalla)
            val matchViaje = regexViaje.find(textoPantalla)

            // Si los TRES sabuesos encontraron su objetivo, sacamos los números
            if (matchPrecio != null && matchRecogida != null && matchViaje != null) {

                // groupValues[1] extrae solamente lo que está adentro de los paréntesis del Regex
                val precio = matchPrecio.groupValues[1].toDouble()
                val kmRecogida = matchRecogida.groupValues[1].toDouble()
                val kmViaje = matchViaje.groupValues[1].toDouble()

                return DatosExtraidos(
                    tarifaBruta = precio,
                    kmRecogida = kmRecogida,
                    kmViaje = kmViaje
                )
            }
        } catch (e: Exception) {
            // Si hay un error raro al convertir a Double, evitamos que la app se cierre sola
            e.printStackTrace()
        }

        // Si falta algún dato (por ejemplo, si estamos en el menú de Uber y no en un viaje), regresa null
        return null
    }
}