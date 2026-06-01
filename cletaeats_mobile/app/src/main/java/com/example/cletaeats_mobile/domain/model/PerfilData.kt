package com.example.cletaeats_mobile.domain.model

data class PerfilData(
    val nombre:    String = "",
    val telefono:  String = "",
    val direccion: String = "",
    val cedula:    String = ""   // solo lectura, no se edita
)
