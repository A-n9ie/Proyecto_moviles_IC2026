package com.example.cletaeats_mobile.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "combos")
data class ComboEntity(
    @PrimaryKey val id:            Int,
    val restauranteId:             Int,
    val numeroCombo:               Int,
    val nombre:                    String,
    val descripcion:               String = "",
    val precio:                    Double,
    val imagenUrl:                 String = "",
    val productosJson:             String = "[]"  // JSON serializado de List<Producto>
)