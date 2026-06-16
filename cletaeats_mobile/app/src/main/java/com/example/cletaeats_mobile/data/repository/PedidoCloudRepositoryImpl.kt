package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IPedidoRepository
import com.example.cletaeats_mobile.domain.model.FacturaData
import com.example.cletaeats_mobile.domain.model.ItemCarrito
import com.example.cletaeats_mobile.domain.model.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repositorio de pedidos usando Firestore.
 * Operaciones de escritura (crear, avanzar estado) siguen usando el API HTTP
 * porque las reglas de negocio (asignación de repartidor, billing, etc.)
 * viven en el backend. Solo la LECTURA viene de Firestore.
 */
class PedidoCloudRepositoryImpl(
    private val apiRepo: PedidoRepositoryImpl   // delegamos escritura al repo HTTP
) : IPedidoRepository {

    private val db = FirebaseFirestore.getInstance()

    override suspend fun obtenerPedidosCliente(): Result<List<Pedido>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = db.collection("pedidos").get().await()
                val pedidos = snapshot.documents.mapNotNull { doc ->
                    try {
                        Pedido(
                            id                  = doc.getLong("id")?.toInt()       ?: 0,
                            estado              = doc.getLong("estado")?.toInt()   ?: 0,
                            estadoTexto         = doc.getString("estadoTexto")     ?: "",
                            restauranteNombre   = doc.getString("restauranteNombre") ?: "",
                            tipoComida          = doc.getString("tipoComida")      ?: "",
                            clienteNombre       = doc.getString("clienteNombre")   ?: "",
                            distanciaKm         = doc.getDouble("distanciaKm")     ?: 0.0,
                            fechaCreacion       = doc.getString("fechaCreacion")   ?: "",
                            fechaEntrega        = doc.getString("fechaEntrega")    ?: "",
                            itemsCount          = doc.getLong("itemsCount")?.toInt() ?: 0,
                            restauranteLatitud  = doc.getDouble("restauranteLatitud"),
                            restauranteLongitud = doc.getDouble("restauranteLongitud")
                        )
                    } catch (e: Exception) { null }
                }
                Result.Success(pedidos)
            } catch (e: Exception) {
                Result.Error("Error al leer pedidos de Cloud: ${e.message}")
            }
        }

    // Las operaciones de escritura se delegan al repo HTTP (tienen lógica de backend)
    override suspend fun crearPedido(restauranteId: Int, items: List<ItemCarrito>, distanciaKm: Double) =
        apiRepo.crearPedido(restauranteId, items, distanciaKm)

    override suspend fun obtenerPedidosRepartidor() = apiRepo.obtenerPedidosRepartidor()
    override suspend fun obtenerHistorialRepartidor() = apiRepo.obtenerHistorialRepartidor()
    override suspend fun marcarPreparando(pedidoId: Int) = apiRepo.marcarPreparando(pedidoId)
    override suspend fun marcarEnCamino(pedidoId: Int) = apiRepo.marcarEnCamino(pedidoId)
    override suspend fun marcarEntregado(pedidoId: Int) = apiRepo.marcarEntregado(pedidoId)
    override suspend fun obtenerFactura(pedidoId: Int) = apiRepo.obtenerFactura(pedidoId)
    override suspend fun calificarRepartidor(pedidoId: Int, rating: Int) = apiRepo.calificarRepartidor(pedidoId, rating)
    override suspend fun cancelarPedido(pedidoId: Int) = apiRepo.cancelarPedido(pedidoId)
    override suspend fun crearQueja(pedidoId: Int, motivo: String, descripcion: String) = apiRepo.crearQueja(pedidoId, motivo, descripcion)
}