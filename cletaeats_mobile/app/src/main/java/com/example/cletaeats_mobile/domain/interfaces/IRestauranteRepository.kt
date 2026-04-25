package com.example.cletaeats_mobile.domain.interfaces

import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.model.Restaurante

interface IRestauranteRepository {
    suspend fun obtenerRestaurantes(): Result<List<Restaurante>>
}