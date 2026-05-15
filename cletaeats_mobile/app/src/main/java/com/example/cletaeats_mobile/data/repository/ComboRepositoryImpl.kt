package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.data.remote.IComboApi
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IComboRepository
import com.example.cletaeats_mobile.domain.model.Combo
import com.example.cletaeats_mobile.domain.model.Restaurante
import com.example.cletaeats_mobile.domain.model.RestauranteConCombos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ComboRepositoryImpl(
    private val api:     IComboApi,
    private val session: SessionManager
) : IComboRepository {

    override suspend fun obtenerCombosPorRestaurante(restauranteId: Int): Result<RestauranteConCombos> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.obtenerCombos("Bearer ${session.getToken()}", restauranteId)
                when (resp.code()) {
                    200 -> {
                        val body = resp.body()!!
                        val r    = body.restaurante
                        val restaurante = Restaurante(r.id, r.nombre, r.tipoComida,
                                                      r.direccion, r.imagenUrl)
                        val combos = body.combos.map {
                            Combo(it.id, restaurante.id, it.numeroCombo, it.nombre,
                                  it.descripcion, it.precio, it.imagenUrl)
                        }
                        Result.Success(RestauranteConCombos(restaurante, combos))
                    }
                    401 -> Result.Error("Sesión expirada")
                    404 -> Result.Error("Restaurante no disponible")
                    else -> Result.Error("Error al cargar el menú (${resp.code()})")
                }
            } catch (e: Exception) {
                Result.Error("Sin conexión al servidor")
            }
        }
}