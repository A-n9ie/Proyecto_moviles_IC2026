package com.example.habitosapp.domain.model


/**
 * Modelo de dominio del usuario autenticado.
 */
data class Usuario(
    val id: Int = 0,
    val nombreUsuario: String = "",
    val token: String = ""
)