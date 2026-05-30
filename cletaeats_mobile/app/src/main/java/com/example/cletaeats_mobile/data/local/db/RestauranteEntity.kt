package com.example.cletaeats_mobile.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurantes")
data class RestauranteEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val categorias: String,   // JSON string: "[\"Rápida\",\"Italiana\"]"
    val direccion: String,
    val imagenUrl: String,
    val estado: Int,
    val latitud: Double,
    val longitud: Double,
    val sincronizado: Boolean = false  // para tracking de sync
)