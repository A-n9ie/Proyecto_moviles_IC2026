package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.data.remote.AgregarTarjetaRequest
import com.example.cletaeats_mobile.data.remote.ITarjetaApi
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.ITarjetaRepository
import com.example.cletaeats_mobile.domain.model.Tarjeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TarjetaRepositoryImpl(
    private val api:     ITarjetaApi,
    private val session: SessionManager
) : ITarjetaRepository {

    override suspend fun listarTarjetas(): Result<List<Tarjeta>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.listarTarjetas("Bearer ${session.getToken()}")
                if (resp.isSuccessful) {
                    Result.Success(resp.body()!!.map {
                        Tarjeta(
                            id = it.id, clienteId = it.clienteId,
                            numero = it.numero, alias = it.alias,
                            fechaVencimiento = it.fechaVencimiento,
                            cvv = it.cvv,
                            esPrincipal = it.esPrincipal
                        )
                    })

                } else {
                    Result.Error("Error al cargar tarjetas (${resp.code()})")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
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
                    Result.Success(id)
                } else {
                    Result.Error("No se pudo agregar la tarjeta (${resp.code()})")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
            }
        }

    override suspend fun actualizarTarjeta(id: Int, alias: String, fechaVencimiento: String, cvv: String, esPrincipal: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.actualizarTarjeta(
                    "Bearer ${session.getToken()}", id,
                    AgregarTarjetaRequest(
                        numero = "",  // no se modifica
                        alias = alias,
                        fechaVencimiento = fechaVencimiento,
                        cvv = cvv,
                        esPrincipal = if (esPrincipal) 1 else 0
                    )
                )
                if (resp.isSuccessful) Result.Success(Unit)
                else Result.Error("No se pudo actualizar (${resp.code()})")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
            }
        }

    override suspend fun eliminarTarjeta(id: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.eliminarTarjeta("Bearer ${session.getToken()}", id)
                if (resp.isSuccessful) Result.Success(Unit)
                else Result.Error("No se pudo eliminar la tarjeta (${resp.code()})")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
            }
        }
}