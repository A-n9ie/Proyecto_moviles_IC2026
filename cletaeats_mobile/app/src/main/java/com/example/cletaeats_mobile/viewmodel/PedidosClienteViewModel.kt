package com.example.cletaeats_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IPedidoRepository
import com.example.cletaeats_mobile.domain.model.Pedido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.cletaeats_mobile.data.notifications.PedidoNotificador
import com.example.cletaeats_mobile.viewmodel.CarritoViewModel

data class PedidosClienteUiState(
    val isLoading: Boolean      = false,
    val pedidos:   List<Pedido> = emptyList(),
    val errorMsg:  String?      = null,
    val filtroEstado: Int?        = null,
    val quejaMsg:  String?      = null   // feedback al enviar una queja (éxito o error)
)

class PedidosClienteViewModel(
    private val repo: IPedidoRepository,
    private val notificador: PedidoNotificador? = null) : ViewModel() {

    private val _uiState = MutableStateFlow(PedidosClienteUiState())
    val uiState: StateFlow<PedidosClienteUiState> = _uiState.asStateFlow()

    fun cargarPedidos() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val result = repo.obtenerPedidosCliente()) {
                // DESPUÉS
                is Result.Success -> {
                    notificador?.procesar(result.data)
                    val calificadosIds = _uiState.value.pedidos
                        .filter { it.calificado }
                        .map { it.id }
                        .toSet()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pedidos = result.data.map {
                            if (it.id in calificadosIds) it.copy(calificado = true) else it
                        },
                        errorMsg = null
                    )
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMsg = result.message
                )
            }
        }
    }

    val pedidosFiltrados: StateFlow<List<Pedido>> = uiState
        .map { state ->
            if (state.filtroEstado == null) state.pedidos
            else state.pedidos.filter { it.estado == state.filtroEstado }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFiltro(estado: Int?) {
        _uiState.value = _uiState.value.copy(filtroEstado = estado)
    }


    fun calificarRepartidor(pedidoId: Int, rating: Int) {
        viewModelScope.launch {
            repo.calificarRepartidor(pedidoId, rating)
            // Marca localmente ANTES de refrescar
            _uiState.value = _uiState.value.copy(
                pedidos = _uiState.value.pedidos.map {
                    if (it.id == pedidoId) it.copy(calificado = true) else it
                }
            )
        }
    }

    fun cancelarPedido(pedidoId: Int) {
        viewModelScope.launch {
            repo.cancelarPedido(pedidoId)
            cargarPedidos()
        }
    }

    fun crearQueja(pedidoId: Int, motivo: String, descripcion: String) {
        viewModelScope.launch {
            when (repo.crearQueja(pedidoId, motivo, descripcion)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    quejaMsg = "Queja enviada. ¡Gracias por tu reporte!"
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    quejaMsg = "No se pudo enviar la queja. Intentá de nuevo."
                )
            }
        }
    }

    fun limpiarQuejaMsg() {
        _uiState.value = _uiState.value.copy(quejaMsg = null)
    }

    fun volverAPedir(
        pedidoId: Int,
        carritoViewModel: CarritoViewModel,
        onListo: (restauranteId: Int, restauranteNombre: String) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = repo.obtenerFactura(pedidoId)) {
                is Result.Success -> {
                    val factura = result.data
                    val pedido = _uiState.value.pedidos.find { it.id == pedidoId } ?: return@launch
                    carritoViewModel.confirmarCambioRestaurante(pedido.restauranteId, factura.restauranteNombre)
                    factura.items.forEach { item ->
                        val combo = com.example.cletaeats_mobile.domain.model.Combo(
                            id          = 0,
                            restauranteId = pedido.restauranteId,
                            numeroCombo = item.numeroCombo,
                            nombre      = item.comboNombre,
                            precio      = item.precioUnitario
                        )
                        repeat(item.cantidad) { carritoViewModel.agregarCombo(combo) }
                    }
                    onListo(pedido.restauranteId, factura.restauranteNombre)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    quejaMsg = "No se pudo cargar el pedido anterior."
                )
            }
        }
    }

}