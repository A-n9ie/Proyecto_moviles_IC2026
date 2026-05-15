package com.example.cletaeats_mobile.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

data class RestauranteResponse(
    val id: Int,
    val nombre: String,
    @SerializedName("tipo_comida") val tipoComida: String,
    val direccion: String,
    @SerializedName("imagen_url") val imagenUrl: String = "",
    val estado: Int = 1
)

interface IRestauranteApi {
    @GET("restaurantes")
    suspend fun obtenerRestaurantes(
        @Header("Authorization") token: String
    ): Response<List<RestauranteResponse>>
}