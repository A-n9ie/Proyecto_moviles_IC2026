package com.example.cletaeats_mobile.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// Categorías de restaurantes.
@Entity(tableName = "categorias_local")
data class CategoriaLocalEntity(
    @PrimaryKey val id: Int,
    val nombre: String
)