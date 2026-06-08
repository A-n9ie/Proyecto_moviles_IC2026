package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.data.local.db.ComboDao
import com.example.cletaeats_mobile.data.local.db.ComboEntity
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IComboRepository
import com.example.cletaeats_mobile.domain.model.Combo
import com.example.cletaeats_mobile.domain.model.RestauranteConCombos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ComboLocalRepositoryImpl(private val dao: ComboDao) : IComboRepository {

    override suspend fun obtenerCombosPorRestaurante(restauranteId: Int): Result<RestauranteConCombos> =
        withContext(Dispatchers.IO) {
            try {
                val entities = dao.obtenerPorRestaurante(restauranteId)
                val combos = entities.map { it.toDomain() }
                Result.Success(RestauranteConCombos(restaurante = null, combos = combos))
            } catch (e: Exception) {
                Result.Error("Error al leer combos locales: ${e.message}")
            }
        }
}

private fun ComboEntity.toDomain() = Combo(
    id            = id,
    restauranteId = restauranteId,
    numeroCombo   = numeroCombo,
    nombre        = nombre,
    descripcion   = descripcion,
    precio        = precio,
    imagenUrl     = imagenUrl
)