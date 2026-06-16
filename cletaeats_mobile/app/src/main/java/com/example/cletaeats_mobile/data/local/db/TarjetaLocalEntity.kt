package com.example.cletaeats_mobile.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tarjetas del cliente activo.
@Entity(tableName = "tarjetas_local")
data class TarjetaLocalEntity(
    @PrimaryKey val id: Int,
    val clienteId:        Int,
    val numero:           String,
    val alias:            String  = "",
    val fechaVencimiento: String  = "",
    val cvv:              String  = "",
    val esPrincipal:      Int     = 0
)