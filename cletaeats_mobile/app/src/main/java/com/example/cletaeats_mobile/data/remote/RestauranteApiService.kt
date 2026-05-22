package com.example.cletaeats_mobile.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class CategoriaResponse(
    val id:     Int,
    val nombre: String
)

data class RestauranteResponse(
    val id:         Int,
    val nombre:     String,
    val categorias: List<CategoriaResponse> = emptyList(), // ← antes era tipo_comida: String
    val direccion:  String,
    @SerializedName("imagen_url") val imagenUrl: String = "",
    val estado:     Int = 1
)

interface IRestauranteApi {
    @GET("restaurantes")
    suspend fun obtenerRestaurantes(
        @Header("Authorization") token: String,
        @Query("categoria") categoria: String? = null  // ← filtro opcional
    ): Response<List<RestauranteResponse>>

    @GET("categorias")
    suspend fun obtenerCategorias(
        @Header("Authorization") token: String
    ): Response<List<CategoriaResponse>>
}