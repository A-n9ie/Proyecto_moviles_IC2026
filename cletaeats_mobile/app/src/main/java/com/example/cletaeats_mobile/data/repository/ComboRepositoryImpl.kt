package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.data.remote.ComboApiService
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IComboRepository
import com.example.cletaeats_mobile.domain.model.RestauranteConCombos

class ComboRepositoryImpl(
    private val api:     ComboApiService,
    private val session: SessionManager
) : IComboRepository {

    override suspend fun obtenerCombosPorRestaurante(
        restauranteId: Int
    ): Result<RestauranteConCombos> {
        return try {
            val (status, data) = api.obtenerCombos(restauranteId, session.getToken())
            when {
                status == 200 && data != null -> Result.Success(data)
                status == 401 -> Result.Error("Sesión expirada")
                status == 404 -> Result.Error("Restaurante no disponible")
                status == -1  -> Result.Error("Sin conexión al servidor")
                else          -> Result.Error("Error al cargar el menú ($status)")
            }
        } catch (e: Exception) {
            Result.Error("Error inesperado: ${e.message}")
        }
    }
}