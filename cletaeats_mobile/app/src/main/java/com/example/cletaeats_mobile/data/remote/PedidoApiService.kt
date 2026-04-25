package com.example.cletaeats_mobile.data.remote

import com.example.cletaeats_mobile.domain.model.FacturaData
import com.example.cletaeats_mobile.domain.model.ItemCarrito
import com.example.cletaeats_mobile.domain.model.ItemFactura
import com.example.cletaeats_mobile.domain.model.Pedido
import org.json.JSONArray
import org.json.JSONObject

class PedidoApiService {

    // ── POST /pedidos ─────────────────────────────────────────────
    suspend fun crearPedido(
        restauranteId: Int,
        items:         List<ItemCarrito>,
        distanciaKm:   Double,
        token:         String
    ): Pair<Int, FacturaData?> {
        val itemsArr = JSONArray().apply {
            items.forEach { item ->
                put(JSONObject()
                    .put("combo_id",      item.combo.id)
                    .put("cantidad",      item.cantidad)
                    .put("configuracion", item.configuracion))
            }
        }
        val body = JSONObject()
            .put("restaurante_id", restauranteId)
            .put("items",         itemsArr)
            .put("distancia_km",  distanciaKm)
            .toString()

        val (status, json) = ApiClient.request("POST", "/pedidos", body, token)
        return Pair(status, if (status == 201) parseFactura(json) else null)
    }

    // ── GET /pedidos/cliente ──────────────────────────────────────
    suspend fun obtenerPedidosCliente(token: String): Pair<Int, List<Pedido>> {
        val (status, json) = ApiClient.request("GET", "/pedidos/cliente", token = token)
        return Pair(status, if (status == 200) parsePedidos(json) else emptyList())
    }

    // ── GET /pedidos/repartidor ───────────────────────────────────
    suspend fun obtenerPedidosRepartidor(token: String): Pair<Int, List<Pedido>> {
        val (status, json) = ApiClient.request("GET", "/pedidos/repartidor", token = token)
        return Pair(status, if (status == 200) parsePedidos(json) else emptyList())
    }

    // ── PUT /pedidos/{id}/entregar ────────────────────────────────
    suspend fun marcarEntregado(pedidoId: Int, token: String): Int =
        ApiClient.request("PUT", "/pedidos/$pedidoId/entregar", token = token).first

    // ── GET /pedidos/{id}/factura ─────────────────────────────────
    suspend fun obtenerFactura(pedidoId: Int, token: String): Pair<Int, FacturaData?> {
        val (status, json) = ApiClient.request("GET", "/pedidos/$pedidoId/factura", token = token)
        return Pair(status, if (status == 200) parseFactura(json) else null)
    }

    fun parseError(json: String): String = try {
        JSONObject(json).getString("error")
    } catch (_: Exception) { "Error desconocido" }

    // ─── Parsers ──────────────────────────────────────────────────
    private fun parseFactura(json: String): FacturaData? = try {
        val o    = JSONObject(json)
        val arr  = o.getJSONArray("items")
        val items = (0 until arr.length()).map { i ->
            val item = arr.getJSONObject(i)
            ItemFactura(
                comboNombre    = item.getString("combo_nombre"),
                numeroCombo    = item.getInt("numero_combo"),
                cantidad       = item.getInt("cantidad"),
                precioUnitario = item.getDouble("precio_unitario"),
                subtotalItem   = item.getDouble("subtotal_item")
            )
        }
        FacturaData(
            pedidoId          = o.getInt("pedido_id"),
            estado            = o.getInt("estado"),
            restauranteNombre = o.getString("restaurante_nombre"),
            clienteNombre     = o.getString("cliente_nombre"),
            repartidorNombre  = o.getString("repartidor_nombre"),
            items             = items,
            subtotal          = o.getDouble("subtotal"),
            distanciaKm       = o.getDouble("distancia_km"),
            costoTransporte   = o.getDouble("costo_transporte"),
            iva               = o.getDouble("iva"),
            total             = o.getDouble("total"),
            fechaCreacion     = o.getString("fecha_creacion")
        )
    } catch (_: Exception) { null }

    private fun parsePedidos(json: String): List<Pedido> = try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Pedido(
                id                = o.getInt("id"),
                estado            = o.getInt("estado"),
                estadoTexto       = o.optString("estado_texto", ""),
                restauranteNombre = o.optString("restaurante_nombre", ""),
                tipoComida        = o.optString("tipo_comida", ""),
                clienteNombre     = o.optString("cliente_nombre", ""),
                distanciaKm       = o.optDouble("distancia_km", 0.0),
                fechaCreacion     = o.optString("fecha_creacion", ""),
                fechaEntrega      = o.optString("fecha_entrega", ""),
                itemsCount        = o.optInt("items_count", 0)
            )
        }
    } catch (_: Exception) { emptyList() }
}