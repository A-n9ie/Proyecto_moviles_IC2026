package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.data.local.db.CletaEatsDatabase
import com.example.cletaeats_mobile.data.local.db.TarjetaEntity
import com.example.cletaeats_mobile.data.remote.AgregarTarjetaRequest
import com.example.cletaeats_mobile.data.remote.ITarjetaApi
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.ITarjetaRepository
import com.example.cletaeats_mobile.domain.model.Tarjeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TarjetaRepositoryImpl(
    private val api:     ITarjetaApi,
    private val session: SessionManager,
    private val db:      CletaEatsDatabase
) : ITarjetaRepository {

    override suspend fun listarTarjetas(): Result<List<Tarjeta>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.listarTarjetas("Bearer ${session.getToken()}")
                if (resp.isSuccessful) {
                    val body = resp.body()!!
                    // Guardar en Room
                    val entities = body.map {
                        TarjetaEntity(
                            id               = it.id,
                            numero           = it.numero,
                            alias            = it.alias,
                            fechaVencimiento = it.fechaVencimiento,
                            cvv              = it.cvv,
                            esPrincipal      = it.esPrincipal
                        )
                    }
                    db.tarjetaDao().insertarTodos(entities)
                    Result.Success(body.map {
                        Tarjeta(
                            id               = it.id,
                            clienteId        = it.clienteId,
                            numero           = it.numero,
                            alias            = it.alias,
                            fechaVencimiento = it.fechaVencimiento,
                            cvv              = it.cvv,
                            esPrincipal      = it.esPrincipal
                        )
                    })
                } else {
                    // Si falla la API, leer de Room
                    val local = db.tarjetaDao().obtenerTodas()
                    if (local.isNotEmpty()) {
                        Result.Success(local.map {
                            Tarjeta(
                                id               = it.id,
                                numero           = it.numero,
                                alias            = it.alias,
                                fechaVencimiento = it.fechaVencimiento,
                                cvv              = it.cvv,
                                esPrincipal      = it.esPrincipal
                            )
                        })
                    } else {
                        Result.Error("Error al cargar tarjetas (${resp.code()})")
                    }
                }
            } catch (e: Exception) {
                // Sin internet, leer de Room
                val local = db.tarjetaDao().obtenerTodas()
                if (local.isNotEmpty()) {
                    Result.Success(local.map {
                        Tarjeta(
                            id               = it.id,
                            numero           = it.numero,
                            alias            = it.alias,
                            fechaVencimiento = it.fechaVencimiento,
                            cvv              = it.cvv,
                            esPrincipal      = it.esPrincipal
                        )
                    })
                } else {
                    Result.Error(e.message ?: "Error desconocido")
                }
            }
        }

    override suspend fun agregarTarjeta(
        numero: String, alias: String,
        fechaVencimiento: String, cvv: String,
        esPrincipal: Boolean
    ): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.agregarTarjeta(
                    "Bearer ${session.getToken()}",
                    AgregarTarjetaRequest(numero, alias, fechaVencimiento, cvv, if (esPrincipal) 1 else 0)
                )
                if (resp.isSuccessful) {
                    val id = (resp.body()?.get("id") as? Double)?.toInt() ?: 0
                    // Guardar en Room
                    db.tarjetaDao().insertar(
                        TarjetaEntity(id, numero, alias, fechaVencimiento, cvv, if (esPrincipal) 1 else 0)
                    )
                    Result.Success(id)
                } else {
                    Result.Error("No se pudo agregar la tarjeta (${resp.code()})")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
            }
        }

    override suspend fun actualizarTarjeta(
        id: Int, alias: String,
        fechaVencimiento: String, cvv: String,
        esPrincipal: Boolean
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            // Actualizar Room PRIMERO, sin importar si hay internet
            val existente = db.tarjetaDao().obtenerTodas().firstOrNull { it.id == id }
            db.tarjetaDao().actualizar(
                TarjetaEntity(
                    id               = id,
                    numero           = existente?.numero ?: "",
                    alias            = alias,
                    fechaVencimiento = fechaVencimiento,
                    cvv              = cvv,
                    esPrincipal      = if (esPrincipal) 1 else 0
                )
            )
            // Luego intentar sincronizar con la API
            try {
                val resp = api.actualizarTarjeta(
                    "Bearer ${session.getToken()}", id,
                    AgregarTarjetaRequest(
                        numero           = "",
                        alias            = alias,
                        fechaVencimiento = fechaVencimiento,
                        cvv              = cvv,
                        esPrincipal      = if (esPrincipal) 1 else 0
                    )
                )
                if (resp.isSuccessful) Result.Success(Unit)
                else Result.Error("Guardado localmente, sin sincronizar (${resp.code()})")
            } catch (e: Exception) {
                // Sin internet pero Room ya fue actualizado
                Result.Success(Unit)
            }
        }

    override suspend fun eliminarTarjeta(id: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            // Borrar de Room PRIMERO
            db.tarjetaDao().eliminar(id)
            // Luego intentar en la API
            try {
                val resp = api.eliminarTarjeta("Bearer ${session.getToken()}", id)
                if (resp.isSuccessful) Result.Success(Unit)
                else Result.Success(Unit) // ya fue borrado localmente
            } catch (e: Exception) {
                Result.Success(Unit) // ya fue borrado localmente
            }
        }
}
