package com.example.cletaeats_mobile.domain.model

/**
 * Datos del usuario autenticado que se almacenan en sesión local.
 * ROL: "CLIENTE" | "REPARTIDOR"
 */
data class Usuario(
    val idUsuario:  Int    = 0,
    val email:      String = "",
    val rol:        String = "",
    val nombre:     String = "",
    val idPerfil:   Int    = 0,
    val token:      String = ""
)