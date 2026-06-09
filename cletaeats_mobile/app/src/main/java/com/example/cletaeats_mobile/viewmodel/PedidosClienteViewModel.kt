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

data class PedidosClienteUiState(
    val isLoading: Boolean      = false,
    val pedidos:   List<Pedido> = emptyList(),
    val errorMsg:  String?      = null,
    val filtroEstado: Int?        = null
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
                is Result.Success -> {
                    notificador?.procesar(result.data)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pedidos = result.data,
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
            cargarPedidos() // refresca la lista
        }
    }

    fun cancelarPedido(pedidoId: Int) {
        viewModelScope.launch {
            repo.cancelarPedido(pedidoId)
            cargarPedidos()
        }
    }
}