package com.example.cletaeats_mobile.domain.interfaces

import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.model.RestauranteConCombos

interface IComboRepository {
    suspend fun obtenerCombosPorRestaurante(restauranteId: Int): Result<RestauranteConCombos>
}