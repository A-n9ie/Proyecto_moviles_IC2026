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
                        val combos = resp.body()!!.map { c ->
                            Combo(
                                id = c.id,
                                restauranteId = c.restauranteId,
                                numeroCombo = c.numeroCombo,
                                nombre = c.nombre,
                                descripcion = c.descripcion,
                                precio = c.precio,
                                imagenUrl = c.imagenUrl,
                                productos = c.productos.map {  // ← nuevo
                                    com.example.cletaeats_mobile.domain.model.Producto(
                                        id = it.id,
                                        nombre = it.nombre,
                                        descripcion = it.descripcion
                                    )
                                }
                            )
                        }
                        Result.Success(RestauranteConCombos(restaurante = null, combos = combos))
                    }

                    401 -> Result.Error("Sesión expirada.")
                    else -> Result.Error("Error al cargar combos (${resp.code()})")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
            }
        }

}