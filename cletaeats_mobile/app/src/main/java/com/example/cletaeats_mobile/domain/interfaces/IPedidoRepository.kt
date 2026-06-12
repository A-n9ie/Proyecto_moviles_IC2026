package com.example.cletaeats_mobile.domain.interfaces

import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.model.FacturaData
import com.example.cletaeats_mobile.domain.model.ItemCarrito
import com.example.cletaeats_mobile.domain.model.Pedido

interface IPedidoRepository {
    suspend fun crearPedido(
        restauranteId: Int,
        items:         List<ItemCarrito>,
        distanciaKm:   Double
    ): Result<FacturaData>

    suspend fun obtenerPedidosCliente(): Result<List<Pedido>>

    suspend fun obtenerPedidosRepartidor(): Result<List<Pedido>>

    suspend fun obtenerHistorialRepartidor(): Result<List<Pedido>>

    suspend fun marcarPreparando(pedidoId: Int): Result<Unit>

    suspend fun marcarEnCamino(pedidoId: Int): Result<Unit>

    suspend fun marcarEntregado(pedidoId: Int): Result<Unit>

    suspend fun obtenerFactura(pedidoId: Int): Result<FacturaData>

    suspend fun calificarRepartidor(pedidoId: Int, rating: Int): Result<Unit>

    suspend fun cancelarPedido(pedidoId: Int): Result<Unit>

    suspend fun crearQueja(pedidoId: Int, motivo: String, descripcion: String): Result<Int>

}