package com.example.cletaeats_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeats_mobile.domain.interfaces.IPedidoRepository
import com.example.cletaeats_mobile.domain.model.Pedido
import com.example.cletaeats_mobile.domain.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HistorialUiState(
    val isLoading:      Boolean = false,
    val entregas:       List<Pedido> = emptyList(),
    val errorMsg:       String? = null,
    // Métricas calculadas
    val totalEntregas:  Int = 0,
    val kmTotales:      Double = 0.0,
    val ganancias:      Double = 0.0
)

class HistorialRepartidorViewModel(
    private val repo: IPedidoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState

    // Costo por km hábil del enunciado: 1000 colones/km
    private val COSTO_KM = 1000.0

    fun cargarHistorial() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val result = repo.obtenerHistorialRepartidor()) {
                is Result.Success -> {
                    val entregas = result.data
                    val km = entregas.sumOf { it.distanciaKm }
                    _uiState.value = HistorialUiState(
                        isLoading     = false,
                        entregas      = entregas,
                        totalEntregas = entregas.size,
                        kmTotales     = km,
                        ganancias     = km * COSTO_KM
                    )
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMsg  = result.message
                )
            }
        }
    }
}