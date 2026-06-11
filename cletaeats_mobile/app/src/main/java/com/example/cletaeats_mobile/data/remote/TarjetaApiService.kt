package com.example.cletaeats_mobile.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

data class TarjetaResponse(
    val id:           Int,
    @SerializedName("cliente_id")       val clienteId:        Int,
    val numero:       String,
    val alias:        String = "",
    @SerializedName("fecha_vencimiento") val fechaVencimiento: String = "",
    val cvv:          String = "",
    @SerializedName("es_principal")     val esPrincipal:      Int = 0
)

data class AgregarTarjetaRequest(
    val numero:       String,
    val alias:        String = "",
    @SerializedName("fecha_vencimiento") val fechaVencimiento: String = "",
    val cvv:          String = "",
    @SerializedName("es_principal")      val esPrincipal:      Int    = 0
)


interface ITarjetaApi {
    @GET("cliente/tarjetas")
    suspend fun listarTarjetas(
        @Header("Authorization") token: String
    ): Response<List<TarjetaResponse>>

    @POST("cliente/tarjetas")
    suspend fun agregarTarjeta(
        @Header("Authorization") token: String,
        @Body body: AgregarTarjetaRequest
    ): Response<Map<String, Any>>

    @PUT("cliente/tarjetas/{id}")
    suspend fun actualizarTarjeta(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: AgregarTarjetaRequest
    ): Response<Map<String, Any>>

    @DELETE("cliente/tarjetas/{id}")
    suspend fun eliminarTarjeta(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
}