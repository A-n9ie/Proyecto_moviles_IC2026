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

data class PedidosClienteUiState(
    val isLoading: Boolean      = false,
    val pedidos:   List<Pedido> = emptyList(),
    val errorMsg:  String?      = null
)

class PedidosClienteViewModel(private val repo: IPedidoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PedidosClienteUiState())
    val uiState: StateFlow<PedidosClienteUiState> = _uiState.asStateFlow()

    fun cargarPedidos() {
        _uiState.value = PedidosClienteUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = repo.obtenerPedidosCliente()) {
                is Result.Success -> _uiState.value = PedidosClienteUiState(pedidos = result.data)
                is Result.Error   -> _uiState.value = PedidosClienteUiState(errorMsg = result.message)
            }
        }
    }
}