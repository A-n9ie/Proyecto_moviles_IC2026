package com.example.cletaeats_mobile.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

data class PerfilResponse(
    val id:        Int    = 0,
    val nombre:    String = "",
    val telefono:  String = "",
    val direccion: String = "",
    val cedula:    String = "",
    val imagen_url: String = ""
)

data class ImagenPerfilRequest(
    val imagen_url: String
)

data class ActualizarPerfilRequest(
    val nombre:    String,
    val telefono:  String,
    val direccion: String
)

interface IPerfilApi {
    @GET("cliente/perfil")
    suspend fun obtenerPerfil(
        @Header("Authorization") token: String
    ): Response<PerfilResponse>

    @PUT("cliente/perfil")
    suspend fun actualizarPerfil(
        @Header("Authorization") token: String,
        @Body body: ActualizarPerfilRequest
    ): Response<Map<String, String>>

    @Multipart
    @POST("cliente/upload-imagen")
    suspend fun subirFoto(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<Map<String, Any>>

    @PUT("cliente/perfil/imagen")
    suspend fun actualizarImagenPerfil(
        @Header("Authorization") token: String,
        @Body body: ImagenPerfilRequest
    ): Response<Map<String, String>>
}