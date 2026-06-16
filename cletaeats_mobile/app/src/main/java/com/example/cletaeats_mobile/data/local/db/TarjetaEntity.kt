package com.example.cletaeats_mobile.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tarjetas")
data class TarjetaEntity(
    @PrimaryKey val id: Int,
    val numero: String,
    val alias: String,
    val fechaVencimiento: String,
    val cvv: String,
    val esPrincipal: Int
)
