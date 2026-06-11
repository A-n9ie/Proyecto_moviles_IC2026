package com.example.cletaeats_mobile.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

data class RepartidorPerfilResponse(
    val id:        Int,
    val nombre:    String,
    val telefono:  String,
    val direccion: String,
    val correo:    String,
    val cedula:    String,
    val tarjeta:   String,
    val rating:    Double,
    @SerializedName("km_recorridos_diarios") val kmRecorridos: Double = 0.0,
    val amonestaciones: Int = 0
)

data class PerfilRepartidorRequest(
    val nombre:    String,
    val telefono:  String,
    val direccion: String
)

data class TarjetaRepartidorRequest(
    val tarjeta: String
)

interface IRepartidorPerfilApi {
    @GET("repartidor/perfil")
    suspend fun obtenerPerfil(
        @Header("Authorization") token: String
    ): Response<RepartidorPerfilResponse>

    @PUT("repartidor/perfil")
    suspend fun actualizarPerfil(
        @Header("Authorization") token: String,
        @Body body: PerfilRepartidorRequest
    ): Response<Map<String, String>>

    @PUT("repartidor/perfil/tarjeta")
    suspend fun actualizarTarjeta(
        @Header("Authorization") token: String,
        @Body body: TarjetaRepartidorRequest
    ): Response<Map<String, String>>
}