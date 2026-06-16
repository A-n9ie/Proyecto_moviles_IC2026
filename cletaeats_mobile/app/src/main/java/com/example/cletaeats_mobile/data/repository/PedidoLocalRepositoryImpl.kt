package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.data.local.db.CletaEatsDatabase
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IPedidoRepository
import com.example.cletaeats_mobile.domain.model.FacturaData
import com.example.cletaeats_mobile.domain.model.ItemCarrito
import com.example.cletaeats_mobile.domain.model.Pedido
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PedidoLocalRepositoryImpl(
    private val db: CletaEatsDatabase,
    private val remoteRepo: PedidoRepositoryImpl
) : IPedidoRepository {

    private val dao = db.pedidoDao()

    override suspend fun crearPedido(restauranteId: Int, items: List<ItemCarrito>, distanciaKm: Double): Result<FacturaData> =
        remoteRepo.crearPedido(restauranteId, items, distanciaKm)

    override suspend fun obtenerPedidosCliente(): Result<List<Pedido>> =
        withContext(Dispatchers.IO) {
            try {
                val pedidos = dao.obtenerTodos().map { it.toDomain() }
                Result.Success(pedidos)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error al leer pedidos locales")
            }
        }

    override suspend fun obtenerPedidosRepartidor(): Result<List<Pedido>> =
        withContext(Dispatchers.IO) {
            try {
                val pedidos = dao.obtenerTodos().filter { it.estado != 3 }.map { it.toDomain() }
                Result.Success(pedidos)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error al leer pedidos locales")
            }
        }

    override suspend fun obtenerHistorialRepartidor(): Result<List<Pedido>> =
        withContext(Dispatchers.IO) {
            try {
                val pedidos = dao.obtenerTodos().filter { it.estado == 3 }.map { it.toDomain() }
                Result.Success(pedidos)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error al leer pedidos locales")
            }
        }

    override suspend fun marcarPreparando(pedidoId: Int): Result<Unit> =
        remoteRepo.marcarPreparando(pedidoId)

    override suspend fun marcarEnCamino(pedidoId: Int): Result<Unit> =
        remoteRepo.marcarEnCamino(pedidoId)

    override suspend fun marcarEntregado(pedidoId: Int): Result<Unit> =
        remoteRepo.marcarEntregado(pedidoId)

    override suspend fun obtenerFactura(pedidoId: Int): Result<FacturaData> =
        remoteRepo.obtenerFactura(pedidoId)

    override suspend fun calificarRepartidor(pedidoId: Int, rating: Int): Result<Unit> =
        remoteRepo.calificarRepartidor(pedidoId, rating)

    override suspend fun cancelarPedido(pedidoId: Int): Result<Unit> =
        remoteRepo.cancelarPedido(pedidoId)

    override suspend fun crearQueja(pedidoId: Int, motivo: String, descripcion: String): Result<Int> =
        remoteRepo.crearQueja(pedidoId, motivo, descripcion)

    private fun com.example.cletaeats_mobile.data.local.db.PedidoEntity.toDomain() =
        Pedido(
            id = id,
            estado = estado,
            estadoTexto = estadoTexto,
            restauranteNombre = restauranteNombre,
            tipoComida = tipoComida,
            clienteNombre = clienteNombre,
            distanciaKm = distanciaKm,
            fechaCreacion = fechaCreacion,
            fechaEntrega = fechaEntrega,
            itemsCount = itemsCount,
            restauranteLatitud = restauranteLatitud,
            restauranteLongitud = restauranteLongitud
        )
}
