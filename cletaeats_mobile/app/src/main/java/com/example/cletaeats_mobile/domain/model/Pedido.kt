package com.example.cletaeats_mobile.domain.model

data class Pedido(
    val id:                Int    = 0,
    val estado:            Int    = 0,
    val estadoTexto:       String = "",
    val restauranteNombre: String = "",
    val tipoComida:        String = "",
    val clienteNombre:     String = "",
    val distanciaKm:       Double = 0.0,
    val fechaCreacion:     String = "",
    val fechaEntrega:      String = "",
    val itemsCount:        Int    = 0,
    val restauranteLatitud:  Double? = null,
    val restauranteLongitud: Double? = null
)