package com.example.cletaeats_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IPedidoRepository
import com.example.cletaeats_mobile.domain.model.Pedido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PedidosRepartidorUiState(
    val isLoading:  Boolean      = false,
    val pedidos:    List<Pedido> = emptyList(),
    val errorMsg:   String?      = null,
    val mensajeOk:  String?      = null
)

class PedidosRepartidorViewModel(private val repo: IPedidoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PedidosRepartidorUiState())
    val uiState: StateFlow<PedidosRepartidorUiState> = _uiState.asStateFlow()

    fun cargarPedidos() {
        _uiState.value = PedidosRepartidorUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = repo.obtenerPedidosRepartidor()) {
                is Result.Success -> _uiState.value = PedidosRepartidorUiState(
                    pedidos = result.data
                )
                is Result.Error -> _uiState.value = PedidosRepartidorUiState(
                    errorMsg = result.message
                )
            }
        }
    }

    fun marcarEntregado(pedidoId: Int) {
        viewModelScope.launch {
            when (val result = repo.marcarEntregado(pedidoId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        mensajeOk = "¡Pedido #$pedidoId entregado!"
                    )
                    cargarPedidos()   // Refrescar lista
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    errorMsg = result.message
                )
            }
        }
    }

    fun clearMensajes() {
        _uiState.value = _uiState.value.copy(mensajeOk = null, errorMsg = null)
    }
}