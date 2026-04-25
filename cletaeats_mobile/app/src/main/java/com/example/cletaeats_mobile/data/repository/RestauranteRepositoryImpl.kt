package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.data.remote.RestauranteApiService
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IRestauranteRepository
import com.example.cletaeats_mobile.domain.model.Restaurante

class RestauranteRepositoryImpl(
    private val api:     RestauranteApiService,
    private val session: SessionManager
) : IRestauranteRepository {

    override suspend fun obtenerRestaurantes(): Result<List<Restaurante>> {
        return try {
            val (status, lista) = api.obtenerRestaurantes(session.getToken())
            when (status) {
                200  -> Result.Success(lista)
                401  -> Result.Error("Sesión expirada. Volvé a iniciar sesión.")
                -1   -> Result.Error("Sin conexión al servidor")
                else -> Result.Error("Error al cargar restaurantes ($status)")
            }
        } catch (e: Exception) {
            Result.Error("Error inesperado: ${e.message}")
        }
    }
}