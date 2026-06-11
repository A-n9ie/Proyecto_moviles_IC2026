package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.data.remote.ActualizarPerfilRequest
import com.example.cletaeats_mobile.data.remote.IPerfilApi
import com.example.cletaeats_mobile.data.remote.ImagenPerfilRequest
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IPerfilRepository
import com.example.cletaeats_mobile.domain.model.PerfilData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

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
                            cedula    = body.cedula,
                            imagenUrl = body.imagen_url   // ← mapear el campo
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

    override suspend fun subirFotoPerfil(
        context: android.content.Context,
        uri:     android.net.Uri
    ): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val stream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext Result.Error("No se pudo leer la imagen")
                val bytes = stream.readBytes()
                stream.close()

                val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", "foto.jpg", requestBody)

                val uploadResp = api.subirFoto("Bearer ${session.getToken()}", part)
                if (!uploadResp.isSuccessful || uploadResp.body() == null) {
                    return@withContext Result.Error("Error al subir imagen (${uploadResp.code()})")
                }

                val url = uploadResp.body()!!["url"] as? String
                    ?: return@withContext Result.Error("Respuesta inválida del servidor")

                // Actualizar la URL en el perfil
                api.actualizarImagenPerfil(
                    "Bearer ${session.getToken()}",
                    ImagenPerfilRequest(url)
                )

                Result.Success(url)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error desconocido")
            }
        }
}