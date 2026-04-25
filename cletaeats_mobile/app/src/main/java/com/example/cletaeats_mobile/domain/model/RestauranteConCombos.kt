package com.example.cletaeats_mobile.domain.model

/** DTO de respuesta del endpoint GET /combos?restaurante={id} */
data class RestauranteConCombos(
    val restaurante: Restaurante,
    val combos:      List<Combo>
)