package com.example.cletaeats_mobile.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey val id:            Int,
    val estado:                    Int    = 0,
    val estadoTexto:               String = "",
    val restauranteNombre:         String = "",
    val tipoComida:                String = "",
    val clienteNombre:             String = "",
    val distanciaKm:               Double = 0.0,
    val fechaCreacion:             String = "",
    val fechaEntrega:              String = "",
    val itemsCount:                Int    = 0,
    val restauranteLatitud:        Double = 0.0,
    val restauranteLongitud:       Double = 0.0
)