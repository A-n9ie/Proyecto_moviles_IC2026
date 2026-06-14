package com.example.cletaeats_mobile.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

// ── Request ───────────────────────────────────────────────────────
data class ItemPedidoRequest(
    @SerializedName("combo_id")      val comboId: Int,
    val cantidad: Int,
    val configuracion: String = "{}"
)

data class CrearPedidoRequest(
    @SerializedName("restaurante_id") val restauranteId: Int,
    val items: List<ItemPedidoRequest>,
    @SerializedName("distancia_km")  val distanciaKm: Double
)

data class CrearPedidoResponse(
    @SerializedName("mensaje")  val mensaje: String,
    @SerializedName("factura")  val factura: FacturaResponse
)

// ── Response ──────────────────────────────────────────────────────
data class ItemFacturaResponse(
    @SerializedName("combo_nombre")    val comboNombre: String,
    @SerializedName("numero_combo")    val numeroCombo: Int,
    val cantidad: Int,
    @SerializedName("precio_unitario") val precioUnitario: Double,
    @SerializedName("subtotal_item") val subtotalItem: Double? = null
)

data class FacturaResponse(
    @SerializedName("pedido_id")          val pedidoId: Int,
    val estado: Int,
    @SerializedName("restaurante") val restauranteNombre: String,
    @SerializedName("cliente") val clienteNombre: String,
    @SerializedName("repartidor") val repartidorNombre: String,
    val items: List<ItemFacturaResponse>,
    val subtotal: Double,
    @SerializedName("distancia_km")       val distanciaKm: Double,
    @SerializedName("costo_envio") val costoTransporte: Double,
    val iva: Double,
    val total: Double,
    @SerializedName("fecha_creacion")     val fechaCreacion: String?
)

data class PedidoListResponse(
    val id: Int,
    val estado: Int,
    @SerializedName("estado_texto")       val estadoTexto: String,
    @SerializedName("restaurante_nombre") val restauranteNombre: String,
    @SerializedName("tipo_comida")        val tipoComida: String = "",
    @SerializedName("cliente_nombre")     val clienteNombre: String = "",
    @SerializedName("distancia_km")       val distanciaKm: Double,
    @SerializedName("fecha_creacion")     val fechaCreacion: String,
    @SerializedName("fecha_entrega")      val fechaEntrega: String? = null,
    @SerializedName("items_count")        val itemsCount: Int,
    @SerializedName("restaurante_latitud")  val restauranteLatitud:  Double? = null,
    @SerializedName("restaurante_longitud") val restauranteLongitud: Double? = null,
    @SerializedName("calificado") val calificado: Boolean = false,
)

data class RatingRequest(val rating: Int)

data class CrearQuejaRequest(
    @SerializedName("pedido_id")   val pedidoId: Int,
    val motivo: String,
    val descripcion: String = ""
)

data class CrearQuejaResponse(
    val mensaje: String,
    @SerializedName("queja_id") val quejaId: Int
)

interface IPedidoApi {
    @POST("cliente/pedidos")
    suspend fun crearPedido(
        @Header("Authorization") token: String,
        @Body body: CrearPedidoRequest
    ): Response<CrearPedidoResponse>

    @GET("cliente/pedidos")
    suspend fun obtenerPedidosCliente(
        @Header("Authorization") token: String
    ): Response<List<PedidoListResponse>>

    @GET("repartidor/pedidos")
    suspend fun obtenerPedidosRepartidor(
        @Header("Authorization") token: String
    ): Response<List<PedidoListResponse>>

    @GET("repartidor/historial")
    suspend fun obtenerHistorialRepartidor(
        @Header("Authorization") token: String
    ): Response<List<PedidoListResponse>>

    @PUT("repartidor/pedidos/{id}/preparar")
    suspend fun marcarPreparando(
        @Header("Authorization") token: String,
        @Path("id") pedidoId: Int
    ): Response<Unit>

    @PUT("repartidor/pedidos/{id}/en-camino")
    suspend fun marcarEnCamino(
        @Header("Authorization") token: String,
        @Path("id") pedidoId: Int
    ): Response<Unit>

    @PUT("repartidor/pedidos/{id}/entregar")
    suspend fun marcarEntregado(
        @Header("Authorization") token: String,
        @Path("id") pedidoId: Int
    ): Response<Unit>

    @GET("cliente/pedidos/{id}/factura")
    suspend fun obtenerFactura(
        @Header("Authorization") token: String,
        @Path("id") pedidoId: Int
    ): Response<FacturaResponse>

    @POST("cliente/pedidos/{id}/rating")
    suspend fun calificarRepartidor(
        @Header("Authorization") token: String,
        @Path("id") pedidoId: Int,
        @Body body: RatingRequest
    ): Response<Unit>

    @PUT("cliente/pedidos/{id}/cancelar")
    suspend fun cancelarPedido(
        @Header("Authorization") token: String,
        @Path("id") pedidoId: Int
    ): Response<Unit>

    @POST("cliente/quejas")
    suspend fun crearQueja(
        @Header("Authorization") token: String,
        @Body body: CrearQuejaRequest
    ): Response<CrearQuejaResponse>

}