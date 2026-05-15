package com.example.cletaeats_mobile.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class ComboResponse(
    val id: Int,
    @SerializedName("numero_combo") val numeroCombo: Int,
    val nombre: String,
    val descripcion: String = "",
    val precio: Double,
    @SerializedName("imagen_url") val imagenUrl: String = ""
)

data class RestauranteConCombosResponse(
    val restaurante: RestauranteResponse,
    val combos: List<ComboResponse>
)

interface IComboApi {
    @GET("combos")
    suspend fun obtenerCombos(
        @Header("Authorization") token: String,
        @Query("restaurante") restauranteId: Int
    ): Response<RestauranteConCombosResponse>
}