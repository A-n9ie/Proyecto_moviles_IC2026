package com.example.cletaeats_mobile.data.remote

import retrofit2.Response
import retrofit2.http.*

data class PerfilResponse(
    val id:        Int    = 0,
    val nombre:    String = "",
    val telefono:  String = "",
    val direccion: String = "",
    val cedula:    String = ""
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
}