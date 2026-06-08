package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IComboRepository
import com.example.cletaeats_mobile.domain.model.Combo
import com.example.cletaeats_mobile.domain.model.RestauranteConCombos
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ComboCloudRepositoryImpl : IComboRepository {

    private val db = FirebaseFirestore.getInstance()

    override suspend fun obtenerCombosPorRestaurante(restauranteId: Int): Result<RestauranteConCombos> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = db.collection("combos")
                    .whereEqualTo("restauranteId", restauranteId)
                    .get()
                    .await()
                val combos = snapshot.documents.mapNotNull { doc ->
                    Combo(
                        id            = doc.getLong("id")?.toInt()           ?: 0,
                        restauranteId = doc.getLong("restauranteId")?.toInt() ?: restauranteId,
                        numeroCombo   = doc.getLong("numeroCombo")?.toInt()   ?: 0,
                        nombre        = doc.getString("nombre")               ?: "",
                        descripcion   = doc.getString("descripcion")          ?: "",
                        precio        = doc.getDouble("precio")               ?: 0.0,
                        imagenUrl     = doc.getString("imagenUrl")            ?: ""
                    )
                }
                Result.Success(RestauranteConCombos(restaurante = null, combos = combos))
            } catch (e: Exception) {
                Result.Error("Error al leer combos de Cloud: ${e.message}")
            }
        }
}