package com.example.cletaeats_mobile.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// Guarda el perfil del usuario autenticado en este dispositivo.
// Un solo registro a la vez (el usuario activo).
// Guarda el perfil del usuario autenticado en este dispositivo.
// Un solo registro a la vez (el usuario activo).
@Entity(tableName = "perfil_local")
data class PerfilLocalEntity(
    @PrimaryKey val idPerfil: Int,
    val rol:        String,   // "CLIENTE" o "REPARTIDOR"
    val nombre:     String,
    val telefono:   String,
    val direccion:  String,
    val cedula:     String,
    val imagenUrl:  String    = "",
    // Solo para repartidor:
    val correo:     String    = "",
    val tarjeta:    String    = "",
    val rating:     Double    = 0.0,
    val amonestaciones: Int   = 0
)