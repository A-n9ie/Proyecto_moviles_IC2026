package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IRestauranteRepository
import com.example.cletaeats_mobile.domain.model.Categoria
import com.example.cletaeats_mobile.domain.model.Restaurante
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RestauranteCloudRepositoryImpl : IRestauranteRepository {

    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection("restaurantes")

    override suspend fun obtenerRestaurantes(): Result<List<Restaurante>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = col.get().await()
                val lista = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(RestauranteFirestore::class.java)?.toDomain(doc.id)
                }
                Result.Success(lista)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error al leer Cloud")
            }
        }

    override suspend fun obtenerCategorias(): Result<List<Categoria>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = db.collection("categorias").get().await()
                val lista = snapshot.documents.mapIndexed { i, doc ->
                    Categoria(i, doc.getString("nombre") ?: "")
                }
                Result.Success(lista)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error al leer categorías Cloud")
            }
        }

    // CRUD extras
    suspend fun guardar(restaurante: Restaurante) = withContext(Dispatchers.IO) {
        col.document(restaurante.id.toString())
            .set(restaurante.toFirestore())
            .await()
    }

    suspend fun eliminar(id: Int) = withContext(Dispatchers.IO) {
        col.document(id.toString()).delete().await()
    }

    // ── Modelos Firestore ────────────────────────────────────────
    data class RestauranteFirestore(
        val nombre: String = "",
        val categorias: List<String> = emptyList(),
        val direccion: String = "",
        val imagenUrl: String = "",
        val estado: Int = 1,
        val latitud: Double = 0.0,
        val longitud: Double = 0.0
    ) {
        fun toDomain(docId: String) = Restaurante(
            id        = docId.toIntOrNull() ?: 0,
            nombre    = nombre,
            categorias = categorias,
            direccion  = direccion,
            imagenUrl  = imagenUrl,
            estado     = estado,
            latitud    = latitud,
            longitud   = longitud
        )
    }

    private fun Restaurante.toFirestore() = mapOf(
        "nombre"     to nombre,
        "categorias" to categorias,
        "direccion"  to direccion,
        "imagenUrl"  to imagenUrl,
        "estado"     to estado,
        "latitud"    to latitud,
        "longitud"   to longitud
    )
}