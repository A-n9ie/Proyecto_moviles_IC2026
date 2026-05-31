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
                val token = "Bearer ${session.getToken()}"

                // Llamadas en paralelo
                val respCombos      = api.obtenerCombos(token, restauranteId)
                val respRestaurante = try {
                    api.obtenerRestaurante(token, restauranteId)
                } catch (_: Exception) { null }

                when (respCombos.code()) {
                    200 -> {
                        val combos = respCombos.body()!!.map { c ->
                            Combo(
                                id            = c.id,
                                restauranteId = c.restauranteId,
                                numeroCombo   = c.numeroCombo,
                                nombre        = c.nombre,
                                descripcion   = c.descripcion,
                                precio        = c.precio,
                                imagenUrl     = c.imagenUrl,
                                productos     = c.productos.map {
                                    com.example.cletaeats_mobile.domain.model.Producto(
                                        id          = it.id,
                                        nombre      = it.nombre,
                                        descripcion = it.descripcion
                                    )
                                }
                            )
                        }

                        // Armar el restaurante con coordenadas si la llamada funcionó
                        val restaurante = if (respRestaurante?.isSuccessful == true) {
                            respRestaurante.body()!!.let { r ->
                                Restaurante(
                                    id         = r.id,
                                    nombre     = r.nombre,
                                    categorias = r.categorias.map { it.nombre },
                                    direccion  = r.direccion,
                                    imagenUrl  = r.imagenUrl,
                                    estado     = r.estado,
                                    latitud    = r.latitud,
                                    longitud   = r.longitud
                                )
                            }
                        } else null

                        Result.Success(RestauranteConCombos(restaurante = restaurante, combos = combos))
                    }
                    401  -> Result.Error("Sesión expirada.")
                    else -> Result.Error("Error al cargar combos (${respCombos.code()})")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
            }
        }
}