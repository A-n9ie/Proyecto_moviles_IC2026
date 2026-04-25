package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.data.remote.PedidoApiService
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IPedidoRepository
import com.example.cletaeats_mobile.domain.model.FacturaData
import com.example.cletaeats_mobile.domain.model.ItemCarrito
import com.example.cletaeats_mobile.domain.model.Pedido

class PedidoRepositoryImpl(
    private val api:     PedidoApiService,
    private val session: SessionManager
) : IPedidoRepository {

    override suspend fun crearPedido(
        restauranteId: Int,
        items:         List<ItemCarrito>,
        distanciaKm:   Double
    ): Result<FacturaData> {
        return try {
            val (status, factura) = api.crearPedido(
                restauranteId, items, distanciaKm, session.getToken()
            )
            when {
                status == 201 && factura != null -> Result.Success(factura)
                status == 400 -> Result.Error("Error en el pedido. Revisá los datos.")
                status == 401 -> Result.Error("Sesión expirada. Volvé a iniciar sesión.")
                status == -1  -> Result.Error("Sin conexión al servidor")
                else          -> Result.Error("Error al crear el pedido ($status)")
            }
        } catch (e: Exception) {
            Result.Error("Error inesperado: ${e.message}")
        }
    }

    override suspend fun obtenerPedidosCliente(): Result<List<Pedido>> {
        return try {
            val (status, pedidos) = api.obtenerPedidosCliente(session.getToken())
            when (status) {
                200  -> Result.Success(pedidos)
                401  -> Result.Error("Sesión expirada")
                -1   -> Result.Error("Sin conexión al servidor")
                else -> Result.Error("Error al cargar pedidos ($status)")
            }
        } catch (e: Exception) {
            Result.Error("Error inesperado: ${e.message}")
        }
    }

    override suspend fun obtenerPedidosRepartidor(): Result<List<Pedido>> {
        return try {
            val (status, pedidos) = api.obtenerPedidosRepartidor(session.getToken())
            when (status) {
                200  -> Result.Success(pedidos)
                401  -> Result.Error("Sesión expirada")
                -1   -> Result.Error("Sin conexión al servidor")
                else -> Result.Error("Error al cargar pedidos ($status)")
            }
        } catch (e: Exception) {
            Result.Error("Error inesperado: ${e.message}")
        }
    }

    override suspend fun marcarEntregado(pedidoId: Int): Result<Unit> {
        return try {
            when (val status = api.marcarEntregado(pedidoId, session.getToken())) {
                200  -> Result.Success(Unit)
                400  -> Result.Error("No se puede marcar como entregado")
                401  -> Result.Error("Sesión expirada")
                -1   -> Result.Error("Sin conexión al servidor")
                else -> Result.Error("Error al actualizar ($status)")
            }
        } catch (e: Exception) {
            Result.Error("Error inesperado: ${e.message}")
        }
    }

    override suspend fun obtenerFactura(pedidoId: Int): Result<FacturaData> {
        return try {
            val (status, factura) = api.obtenerFactura(pedidoId, session.getToken())
            when {
                status == 200 && factura != null -> Result.Success(factura)
                status == 404 -> Result.Error("Pedido no encontrado")
                status == -1  -> Result.Error("Sin conexión")
                else          -> Result.Error("Error al cargar factura ($status)")
            }
        } catch (e: Exception) {
            Result.Error("Error inesperado: ${e.message}")
        }
    }
}