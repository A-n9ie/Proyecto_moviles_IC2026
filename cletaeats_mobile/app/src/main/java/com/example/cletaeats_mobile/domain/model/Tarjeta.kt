package com.example.cletaeats_mobile.domain.model

data class Tarjeta(
    val id:          Int     = 0,
    val clienteId:   Int     = 0,
    val numero:      String  = "",
    val alias:       String  = "",
    val fechaVencimiento: String = "",   // MM/YY
    val cvv:              String = "",   // solo para mostrar enmascarado
    val esPrincipal: Int     = 0
)