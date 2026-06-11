package com.example.cletaeats_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.ITarjetaRepository
import com.example.cletaeats_mobile.domain.model.Tarjeta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TarjetaUiState(
    val isLoading:         Boolean       = false,
    val tarjetas:          List<Tarjeta> = emptyList(),
    val tarjetaSeleccionada: Tarjeta?   = null,  // la que se usará para pagar
    val errorMsg:          String?       = null,
    val exito:             Boolean       = false
){
    val limiteAlcanzado: Boolean
        get() = tarjetas.size >= 5
}

class TarjetaViewModel(private val repo: ITarjetaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TarjetaUiState())
    val uiState: StateFlow<TarjetaUiState> = _uiState.asStateFlow()

    fun cargarTarjetas() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val result = repo.listarTarjetas()) {
                is Result.Success -> {
                    val tarjetas = result.data
                    // Pre-seleccionar la principal
                    val principal = tarjetas.firstOrNull { it.esPrincipal == 1 } ?: tarjetas.firstOrNull()
                    _uiState.value = TarjetaUiState(tarjetas = tarjetas, tarjetaSeleccionada = principal)
                }
                is Result.Error -> _uiState.value = TarjetaUiState(errorMsg = result.message)
            }
        }
    }

    fun seleccionarTarjeta(tarjeta: Tarjeta) {
        _uiState.value = _uiState.value.copy(tarjetaSeleccionada = tarjeta)
    }

    fun agregarTarjeta(numero: String, alias: String, fechaVencimiento: String = "", cvv: String = "", esPrincipal: Boolean
    ) {
        if (numero.isBlank()) return
        if (_uiState.value.tarjetas.size >= 5) {
            _uiState.value = _uiState.value.copy(errorMsg = "Límite de 5 tarjetas alcanzado. Eliminá una para agregar otra.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMsg = null)
        viewModelScope.launch {
            when (val result = repo.agregarTarjeta(numero, alias, fechaVencimiento, cvv, esPrincipal)) {
                is Result.Success -> cargarTarjetas()
                is Result.Error   -> _uiState.value = _uiState.value.copy(
                    isLoading = false, errorMsg = result.message
                )
            }
        }
    }

    fun eliminarTarjeta(id: Int) {
        viewModelScope.launch {
            repo.eliminarTarjeta(id)
            cargarTarjetas()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMsg = null)
    }
}