package com.example.cletaeats_mobile.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.cletaeats_mobile.data.local.db.CletaEatsDatabase
import com.example.cletaeats_mobile.data.local.db.ComboEntity
import com.example.cletaeats_mobile.data.local.db.RestauranteEntity
import com.example.cletaeats_mobile.data.remote.IComboApi
import com.example.cletaeats_mobile.data.remote.RetrofitClient
import com.example.cletaeats_mobile.data.repository.RestauranteCloudRepositoryImpl
import com.example.cletaeats_mobile.domain.model.Restaurante
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncManager(private val context: Context) {

    private val cloud = RestauranteCloudRepositoryImpl()
    private val dao   = CletaEatsDatabase.getInstance(context).restauranteDao()

    private val daoCombo  = CletaEatsDatabase.getInstance(context).comboDao()
    private val daoPedido = CletaEatsDatabase.getInstance(context).pedidoDao()
    fun hayInternet(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Descarga restaurantes desde Cloud y los guarda en SQLite.
     * Llamar cuando el usuario entra en modo CLOUD o al iniciar app con internet.
     */
    suspend fun sincronizarDesdeCloud() = withContext(Dispatchers.IO) {
        if (!hayInternet()) return@withContext SyncResult.SinInternet
        try {
            val result = cloud.obtenerRestaurantes()
            if (result is com.example.cletaeats_mobile.domain.Result.Success) {
                val entities = result.data.map { r ->
                    RestauranteEntity(
                        id           = r.id,
                        nombre       = r.nombre,
                        categorias   = r.categorias.joinToString(","),
                        direccion    = r.direccion,
                        imagenUrl    = r.imagenUrl,
                        estado       = r.estado,
                        latitud      = r.latitud,
                        longitud     = r.longitud,
                        sincronizado = true
                    )
                }
                dao.insertarTodos(entities)
                SyncResult.Exito(entities.size)
            } else {
                SyncResult.Error("No se pudo obtener datos de Cloud")
            }
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Error de sincronización")
        }
    }

    /**
     * Sube TODOS los restaurantes de SQLite hacia Cloud (Firestore).
     * No depende del flag sincronizado — siempre sube todo.
     */
    suspend fun sincronizarHaciaCloud() = withContext(Dispatchers.IO) {
        if (!hayInternet()) return@withContext SyncResult.SinInternet
        try {
            val todos = dao.obtenerTodos()
            if (todos.isEmpty()) return@withContext SyncResult.Error("SQLite vacío, nada que subir")
            todos.forEach { entity ->
                cloud.guardar(entity.toDomain())
            }
            // Marcar todos como sincronizados
            todos.forEach { dao.insertar(it.copy(sincronizado = true)) }
            android.util.Log.d("SYNC", "Subidos ${todos.size} restaurantes a Firestore")
            SyncResult.Exito(todos.size)
        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Error subiendo a Cloud: ${e.message}")
            SyncResult.Error(e.message ?: "Error subiendo a Cloud")
        }
    }

    /**
     * Descarga restaurantes desde el API remoto y los guarda en SQLite.
     * Llamar después de un login exitoso en modo API_REMOTA para tener
     * datos disponibles cuando se use modo LOCAL_SQLITE sin internet.
     */
    suspend fun sincronizarDesdeApi(token: String) = withContext(Dispatchers.IO) {
        if (!hayInternet()) return@withContext SyncResult.SinInternet
        try {
            val retrofit = com.example.cletaeats_mobile.data.remote.RetrofitClient
                .create<com.example.cletaeats_mobile.data.remote.IRestauranteApi>()
            val resp = retrofit.obtenerRestaurantes("Bearer $token")
            if (resp.isSuccessful) {
                val entities = resp.body()!!.map { r ->
                    RestauranteEntity(
                        id           = r.id,
                        nombre       = r.nombre,
                        categorias   = r.categorias.joinToString(",") { it.nombre },
                        direccion    = r.direccion,
                        imagenUrl    = r.imagenUrl,
                        estado       = r.estado,
                        latitud      = r.latitud,
                        longitud     = r.longitud,
                        sincronizado = false   // ← CAMBIAR a false para que sincronizarHaciaCloud() los tome
                    )
                }
                dao.insertarTodos(entities)
                android.util.Log.d("SYNC", "Guardados ${entities.size} restaurantes en SQLite")
                SyncResult.Exito(entities.size)
            } else {
                SyncResult.Error("Error al sincronizar (${resp.code()})")
            }
        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Error sincronizando desde API: ${e.message}")
            SyncResult.Error(e.message ?: "Error de sincronización")
        }
    }

    private fun RestauranteEntity.toDomain() = Restaurante(
        id         = id, nombre = nombre,
        categorias = categorias.split(",").map { it.trim() },
        direccion  = direccion, imagenUrl = imagenUrl,
        estado     = estado, latitud = latitud, longitud = longitud
    )

    /**
     * Sincroniza combos desde el API remoto hacia SQLite.
     * Llama por cada restaurante ya guardado localmente.
     */
    suspend fun sincronizarCombosDesdeApi(token: String) = withContext(Dispatchers.IO) {
        if (!hayInternet()) return@withContext SyncResult.SinInternet
        try {
            val comboApi = RetrofitClient.create<IComboApi>()
            val restaurantes = dao.obtenerTodos()
            var total = 0
            restaurantes.forEach { rest ->
                val resp = comboApi.obtenerCombos("Bearer $token", rest.id)
                if (resp.isSuccessful) {
                    val entities = resp.body()!!.map { c ->
                        ComboEntity(
                            id = c.id,
                            restauranteId = c.restauranteId,
                            numeroCombo = c.numeroCombo,
                            nombre = c.nombre,
                            descripcion = c.descripcion,
                            precio = c.precio,
                            imagenUrl = c.imagenUrl
                        )
                    }
                    daoCombo.insertarTodos(entities)
                    total += entities.size
                }
            }
            android.util.Log.d("SYNC", "Combos sincronizados: $total")
            SyncResult.Exito(total)
        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Error sincronizando combos: ${e.message}")
            SyncResult.Error(e.message ?: "Error de sincronización de combos")
        }
    }
}


sealed class SyncResult {
    data class Exito(val cantidad: Int)  : SyncResult()
    data class Error(val msg: String)    : SyncResult()
    object SinInternet                   : SyncResult()
}