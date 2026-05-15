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

// ── Response ──────────────────────────────────────────────────────
data class ItemFacturaResponse(
    @SerializedName("combo_nombre")    val comboNombre: String,
    @SerializedName("numero_combo")    val numeroCombo: Int,
    val cantidad: Int,
    @SerializedName("precio_unitario") val precioUnitario: Double,
    @SerializedName("subtotal_item")   val subtotalItem: Double
)

data class FacturaResponse(
    @SerializedName("pedido_id")          val pedidoId: Int,
    val estado: Int,
    @SerializedName("restaurante_nombre") val restauranteNombre: String,
    @SerializedName("cliente_nombre")     val clienteNombre: String,
    @SerializedName("repartidor_nombre")  val repartidorNombre: String,
    val items: List<ItemFacturaResponse>,
    val subtotal: Double,
    @SerializedName("distancia_km")       val distanciaKm: Double,
    @SerializedName("costo_transporte")   val costoTransporte: Double,
    val iva: Double,
    val total: Double,
    @SerializedName("fecha_creacion")     val fechaCreacion: String
)

data class PedidoListResponse(
    val id: Int,
    val estado: Int,
    @SerializedName("estado_texto")       val estadoTexto: String,
    @SerializedName("restaurante_nombre") val restauranteNombre: String,
    @SerializedName("tipo_comida")        val tipoComida: String,
    @SerializedName("cliente_nombre")     val clienteNombre: String = "",
    @SerializedName("distancia_km")       val distanciaKm: Double,
    @SerializedName("fecha_creacion")     val fechaCreacion: String,
    @SerializedName("fecha_entrega")      val fechaEntrega: String = "",
    @SerializedName("items_count")        val itemsCount: Int
)

interface IPedidoApi {
    @POST("pedidos")
    suspend fun crearPedido(
        @Header("Authorization") token: String,
        @Body body: CrearPedidoRequest
    ): Response<FacturaResponse>

    @GET("pedidos/cliente")
    suspend fun obtenerPedidosCliente(
        @Header("Authorization") token: String
    ): Response<List<PedidoListResponse>>

    @GET("pedidos/repartidor")
    suspend fun obtenerPedidosRepartidor(
        @Header("Authorization") token: String
    ): Response<List<PedidoListResponse>>

    @PUT("pedidos/{id}/entregar")
    suspend fun marcarEntregado(
        @Header("Authorization") token: String,
        @Path("id") pedidoId: Int
    ): Response<Unit>

    @GET("pedidos/{id}/factura")
    suspend fun obtenerFactura(
        @Header("Authorization") token: String,
        @Path("id") pedidoId: Int
    ): Response<FacturaResponse>
}