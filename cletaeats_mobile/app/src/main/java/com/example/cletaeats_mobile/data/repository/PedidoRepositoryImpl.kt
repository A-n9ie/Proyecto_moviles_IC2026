package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.data.remote.CrearPedidoRequest
import com.example.cletaeats_mobile.data.remote.FacturaResponse
import com.example.cletaeats_mobile.data.remote.IPedidoApi
import com.example.cletaeats_mobile.data.remote.ItemPedidoRequest
import com.example.cletaeats_mobile.data.remote.PedidoListResponse
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IPedidoRepository
import com.example.cletaeats_mobile.domain.model.FacturaData
import com.example.cletaeats_mobile.domain.model.ItemCarrito
import com.example.cletaeats_mobile.domain.model.ItemFactura
import com.example.cletaeats_mobile.domain.model.Pedido
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.cletaeats_mobile.data.remote.RatingRequest

class PedidoRepositoryImpl(
    private val api:     IPedidoApi,
    private val session: SessionManager
) : IPedidoRepository {

    private val token get() = "Bearer ${session.getToken()}"

    override suspend fun crearPedido(
        restauranteId: Int, items: List<ItemCarrito>, distanciaKm: Double
    ): Result<FacturaData> = withContext(Dispatchers.IO) {
        try {
            val body = CrearPedidoRequest(
                restauranteId = restauranteId,
                items = items.map { ItemPedidoRequest(it.combo.id, it.cantidad, it.configuracion) },
                distanciaKm = distanciaKm
            )
            val resp = api.crearPedido(token, body)
            when (resp.code()) {
                201 -> Result.Success(resp.body()!!.factura.toFacturaData())
                400 -> {
                    val errorBody = resp.errorBody()?.string()
                    val msg = try {
                        org.json.JSONObject(errorBody ?: "").getString("error")
                    } catch (_: Exception) {
                        "Error en el pedido. Revisá los datos."
                    }
                    Result.Error(msg)
                }
                401 -> Result.Error("Sesión expirada.")
                else -> Result.Error("Error al crear el pedido (${resp.code()})")
            }
        }  catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e.message ?: "Error desconocido")
        }
    }

    override suspend fun obtenerPedidosCliente(): Result<List<Pedido>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.obtenerPedidosCliente(token)
                if (resp.code() == 200) Result.Success(resp.body()!!.map { it.toPedido() })
                else Result.Error("Error al cargar pedidos (${resp.code()})")
            } catch (e: Exception) { Result.Error("Sin conexión al servidor") }
        }

    override suspend fun obtenerPedidosRepartidor(): Result<List<Pedido>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.obtenerPedidosRepartidor(token)
                if (resp.code() == 200) Result.Success(resp.body()!!.map { it.toPedido() })
                else Result.Error("Error al cargar pedidos (${resp.code()})")
            } catch (e: Exception) { Result.Error("Sin conexión al servidor") }
        }

    override suspend fun marcarPreparando(pedidoId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.marcarPreparando(token, pedidoId)
                if (resp.isSuccessful) Result.Success(Unit)
                else Result.Error("No se pudo aceptar el pedido")
            } catch (e: Exception) { Result.Error("Sin conexión al servidor") }
        }

    override suspend fun marcarEnCamino(pedidoId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.marcarEnCamino(token, pedidoId)
                if (resp.isSuccessful) Result.Success(Unit)
                else Result.Error("No se pudo marcar en camino")
            } catch (e: Exception) { Result.Error("Sin conexión al servidor") }
        }

    override suspend fun marcarEntregado(pedidoId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.marcarEntregado(token, pedidoId)
                if (resp.isSuccessful) Result.Success(Unit)
                else Result.Error("No se pudo marcar como entregado")
            } catch (e: Exception) { Result.Error("Sin conexión al servidor") }
        }

    override suspend fun obtenerFactura(pedidoId: Int): Result<FacturaData> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.obtenerFactura(token, pedidoId)
                when (resp.code()) {
                    200  -> Result.Success(resp.body()!!.toFacturaData())
                    404  -> Result.Error("Pedido no encontrado")
                    else -> Result.Error("Error al cargar factura (${resp.code()})")
                }
            } catch (e: Exception) { Result.Error("Sin conexión al servidor") }
        }

    override suspend fun calificarRepartidor(pedidoId: Int, rating: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.calificarRepartidor(token, pedidoId, RatingRequest(rating))
                if (resp.code() == 200) Result.Success(Unit)
                else Result.Error("No se pudo enviar la calificación")
            } catch (e: Exception) { Result.Error("Sin conexión al servidor") }
        }

    override suspend fun cancelarPedido(pedidoId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.cancelarPedido(token, pedidoId)
                if (resp.code() == 200) Result.Success(Unit)
                else Result.Error("No se pudo cancelar el pedido")
            } catch (e: Exception) { Result.Error("Sin conexión al servidor") }
        }

}



// ── Extensiones de mapeo ──────────────────────────────────────────
private fun FacturaResponse.toFacturaData() =
    FacturaData(
        pedidoId          = pedidoId,
        estado            = estado,
        restauranteNombre = restauranteNombre,
        clienteNombre     = clienteNombre,
        repartidorNombre  = repartidorNombre,
        items = items.map {
            ItemFactura(it.comboNombre, it.numeroCombo, it.cantidad,
                it.precioUnitario, it.subtotalItem ?: 0.0)
        },
        subtotal        = subtotal,
        distanciaKm     = distanciaKm,
        costoTransporte = costoTransporte,
        iva             = iva,
        total           = total,
        fechaCreacion   = fechaCreacion ?: ""
    )

private fun PedidoListResponse.toPedido() =
    Pedido(
        id                = id,
        estado            = estado,
        estadoTexto       = estadoTexto,
        restauranteNombre = restauranteNombre,
        tipoComida        = tipoComida,
        clienteNombre     = clienteNombre,
        distanciaKm       = distanciaKm,
        fechaCreacion     = fechaCreacion,
        fechaEntrega      = fechaEntrega ?: "",
        itemsCount        = itemsCount,
        restauranteLatitud  = restauranteLatitud,
        restauranteLongitud = restauranteLongitud
    )