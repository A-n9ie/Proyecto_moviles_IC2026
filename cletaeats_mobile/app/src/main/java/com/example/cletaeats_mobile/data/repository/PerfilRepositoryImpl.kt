package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.data.remote.ActualizarPerfilRequest
import com.example.cletaeats_mobile.data.remote.IPerfilApi
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IPerfilRepository
import com.example.cletaeats_mobile.domain.model.PerfilData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PerfilRepositoryImpl(
    private val api:     IPerfilApi,
    private val session: SessionManager
) : IPerfilRepository {

    override suspend fun obtenerPerfil(): Result<PerfilData> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.obtenerPerfil("Bearer ${session.getToken()}")
                if (resp.isSuccessful && resp.body() != null) {
                    val body = resp.body()!!
                    Result.Success(
                        PerfilData(
                            nombre    = body.nombre,
                            telefono  = body.telefono,
                            direccion = body.direccion,
                            cedula    = body.cedula
                        )
                    )
                } else {
                    Result.Error("No se pudo cargar el perfil (${resp.code()})")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error de red")
            }
        }

    override suspend fun actualizarPerfil(
        nombre: String, telefono: String, direccion: String
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.actualizarPerfil(
                    "Bearer ${session.getToken()}",
                    ActualizarPerfilRequest(nombre, telefono, direccion)
                )
                if (resp.isSuccessful) Result.Success(Unit)
                else Result.Error("No se pudo actualizar el perfil (${resp.code()})")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error de red")
            }
        }
}
