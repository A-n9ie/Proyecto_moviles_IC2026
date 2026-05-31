package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.data.local.db.CletaEatsDatabase
import com.example.cletaeats_mobile.data.local.db.RestauranteEntity
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IRestauranteRepository
import com.example.cletaeats_mobile.domain.model.Categoria
import com.example.cletaeats_mobile.domain.model.Restaurante
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RestauranteLocalRepositoryImpl(
    private val db: CletaEatsDatabase
) : IRestauranteRepository {

    private val dao = db.restauranteDao()

    override suspend fun obtenerRestaurantes(): Result<List<Restaurante>> =
        withContext(Dispatchers.IO) {
            try {
                val entities = dao.obtenerTodos()
                Result.Success(entities.map { it.toDomain() })
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error al leer SQLite")
            }
        }

    override suspend fun obtenerCategorias(): Result<List<Categoria>> =
        withContext(Dispatchers.IO) {
            // Las categorías en local las derivamos de los restaurantes guardados
            try {
                val restaurantes = dao.obtenerTodos()
                val categorias = restaurantes
                    .flatMap { it.categorias.split(",").map { c -> c.trim() }.filter { it.isNotEmpty() } }
                    .distinct()
                    .mapIndexed { i, nombre -> Categoria(i, nombre) }
                Result.Success(categorias)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error al leer categorías")
            }
        }

    // Extras para CRUD completo
    suspend fun insertar(restaurante: Restaurante) =
        withContext(Dispatchers.IO) { dao.insertar(restaurante.toEntity()) }

    suspend fun actualizar(restaurante: Restaurante) =
        withContext(Dispatchers.IO) { dao.actualizar(restaurante.toEntity()) }

    suspend fun eliminar(id: Int) =
        withContext(Dispatchers.IO) { dao.eliminarPorId(id) }

    suspend fun buscar(query: String): Result<List<Restaurante>> =
        withContext(Dispatchers.IO) {
            try {
                Result.Success(dao.buscar(query).map { it.toDomain() })
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error en búsqueda")
            }
        }

    // Helpers de conversión
    private fun RestauranteEntity.toDomain() = Restaurante(
        id        = id,
        nombre    = nombre,
        categorias = categorias.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        direccion  = direccion,
        imagenUrl  = imagenUrl,
        estado     = estado,
        latitud    = latitud,
        longitud   = longitud
    )

    private fun Restaurante.toEntity() = RestauranteEntity(
        id          = id,
        nombre      = nombre,
        categorias  = categorias.joinToString(","),
        direccion   = direccion,
        imagenUrl   = imagenUrl,
        estado      = estado,
        latitud     = latitud,
        longitud    = longitud,
        sincronizado = false
    )
}