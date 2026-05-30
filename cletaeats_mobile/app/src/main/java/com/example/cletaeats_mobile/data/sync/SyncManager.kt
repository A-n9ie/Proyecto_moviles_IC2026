package com.example.cletaeats_mobile.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.cletaeats_mobile.data.local.db.CletaEatsDatabase
import com.example.cletaeats_mobile.data.local.db.RestauranteEntity
import com.example.cletaeats_mobile.data.repository.RestauranteCloudRepositoryImpl
import com.example.cletaeats_mobile.domain.model.Restaurante
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncManager(private val context: Context) {

    private val cloud = RestauranteCloudRepositoryImpl()
    private val dao   = CletaEatsDatabase.getInstance(context).restauranteDao()

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
     * Sube restaurantes locales no sincronizados hacia Cloud.
     */
    suspend fun sincronizarHaciaCloud() = withContext(Dispatchers.IO) {
        if (!hayInternet()) return@withContext SyncResult.SinInternet
        try {
            val noSincronizados = dao.obtenerTodos().filter { !it.sincronizado }
            noSincronizados.forEach { entity ->
                cloud.guardar(entity.toDomain())
                dao.insertar(entity.copy(sincronizado = true))
            }
            SyncResult.Exito(noSincronizados.size)
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Error subiendo a Cloud")
        }
    }

    private fun RestauranteEntity.toDomain() = Restaurante(
        id         = id, nombre = nombre,
        categorias = categorias.split(",").map { it.trim() },
        direccion  = direccion, imagenUrl = imagenUrl,
        estado     = estado, latitud = latitud, longitud = longitud
    )
}

sealed class SyncResult {
    data class Exito(val cantidad: Int)  : SyncResult()
    data class Error(val msg: String)    : SyncResult()
    object SinInternet                   : SyncResult()
}