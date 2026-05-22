package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.data.remote.IRestauranteApi
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IRestauranteRepository
import com.example.cletaeats_mobile.domain.model.Categoria
import com.example.cletaeats_mobile.domain.model.Restaurante
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RestauranteRepositoryImpl(
    private val api:     IRestauranteApi,
    private val session: SessionManager
) : IRestauranteRepository {

    override suspend fun obtenerRestaurantes(): Result<List<Restaurante>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.obtenerRestaurantes("Bearer ${session.getToken()}")
                when (resp.code()) {
                    200 -> Result.Success(resp.body()!!.map { r ->
                        Restaurante(
                            id         = r.id,
                            nombre     = r.nombre,
                            categorias = r.categorias.map { it.nombre },
                            direccion  = r.direccion,
                            imagenUrl  = r.imagenUrl,
                            estado     = r.estado
                        )
                    })
                    401 -> Result.Error("Sesión expirada. Volvé a iniciar sesión.")
                    else -> Result.Error("Error al cargar restaurantes (${resp.code()})")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
            }
        }

    override suspend fun obtenerCategorias(): Result<List<Categoria>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.obtenerCategorias("Bearer ${session.getToken()}")
                if (resp.isSuccessful) {
                    Result.Success(resp.body()!!.map { Categoria(it.id, it.nombre) })
                } else {
                    Result.Error("Error al cargar categorías (${resp.code()})")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
            }
        }
}