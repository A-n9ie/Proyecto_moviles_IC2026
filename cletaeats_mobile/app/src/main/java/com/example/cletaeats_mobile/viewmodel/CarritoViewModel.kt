package com.example.cletaeats_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IPedidoRepository
import com.example.cletaeats_mobile.domain.model.Combo
import com.example.cletaeats_mobile.domain.model.FacturaData
import com.example.cletaeats_mobile.domain.model.ItemCarrito
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CarritoUiState(
    val restauranteId:     Int               = 0,
    val restauranteNombre: String            = "",
    val items:             List<ItemCarrito> = emptyList(),
    val isLoading:         Boolean           = false,
    val errorMsg:          String?           = null,
    val factura:           FacturaData?      = null,
    val pedidoCreado:      Boolean           = false
) {
    val subtotal:    Double get() = items.sumOf { it.subtotal }
    val totalItems:  Int    get() = items.sumOf { it.cantidad }
    val estaVacio:   Boolean get() = items.isEmpty()
}

/**
 * Singleton en AppContainer — persiste entre CombosScreen y CarritoScreen.
 * PATRÓN MVC: actúa como Controller del carrito de compras.
 */
class CarritoViewModel(private val repo: IPedidoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CarritoUiState())
    val uiState: StateFlow<CarritoUiState> = _uiState.asStateFlow()

    // ─── Gestión del restaurante activo ───────────────────────────
    fun iniciarCarrito(restauranteId: Int, restauranteNombre: String) {
        if (_uiState.value.restauranteId != restauranteId) {
            // Restaurante diferente → vaciar carrito
            _uiState.value = CarritoUiState(
                restauranteId     = restauranteId,
                restauranteNombre = restauranteNombre
            )
        } else {
            _uiState.value = _uiState.value.copy(restauranteNombre = restauranteNombre)
        }
    }

    // ─── Agregar / quitar combos ──────────────────────────────────
    fun agregarCombo(combo: Combo) {
        val items = _uiState.value.items.toMutableList()
        val index = items.indexOfFirst { it.combo.id == combo.id }
        if (index >= 0) {
            items[index] = items[index].copy(cantidad = items[index].cantidad + 1)
        } else {
            items.add(ItemCarrito(combo = combo, cantidad = 1))
        }
        _uiState.value = _uiState.value.copy(items = items)
    }

    fun reducirCombo(comboId: Int) {
        val items = _uiState.value.items.toMutableList()
        val index = items.indexOfFirst { it.combo.id == comboId }
        if (index >= 0) {
            if (items[index].cantidad > 1) {
                items[index] = items[index].copy(cantidad = items[index].cantidad - 1)
            } else {
                items.removeAt(index)
            }
            _uiState.value = _uiState.value.copy(items = items)
        }
    }

    fun eliminarCombo(comboId: Int) {
        val items = _uiState.value.items.filter { it.combo.id != comboId }
        _uiState.value = _uiState.value.copy(items = items)
    }

    fun getCantidad(comboId: Int): Int =
        _uiState.value.items.find { it.combo.id == comboId }?.cantidad ?: 0

    // ─── Confirmar pedido ─────────────────────────────────────────
    fun confirmarPedido(distanciaKm: Double) {
        val state = _uiState.value
        if (state.estaVacio || state.restauranteId == 0) return

        _uiState.value = state.copy(isLoading = true, errorMsg = null)
        viewModelScope.launch {
            when (val result = repo.crearPedido(
                restauranteId = state.restauranteId,
                items         = state.items,
                distanciaKm   = distanciaKm
            )) {
                is Result.Success -> _uiState.value = state.copy(
                    isLoading    = false,
                    factura      = result.data,
                    pedidoCreado = true
                )
                is Result.Error -> _uiState.value = state.copy(
                    isLoading = false,
                    errorMsg  = result.message
                )
            }
        }
    }

    fun limpiar() {
        _uiState.value = CarritoUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMsg = null)
    }
}