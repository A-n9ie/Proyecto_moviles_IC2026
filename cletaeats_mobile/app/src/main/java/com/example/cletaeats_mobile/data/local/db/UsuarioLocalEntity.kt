package com.example.cletaeats_mobile.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// Guarda SOLO el usuario que inició sesión en este dispositivo.
// No es una copia de todos los usuarios del sistema.
@Entity(tableName = "usuario_local")
data class UsuarioLocalEntity(
    @PrimaryKey val idUsuario: Int,
    val email:    String,
    val nombre:   String,
    val rol:      String,
    val idPerfil: Int,
    val token:    String   // JWT de la última sesión remota
)