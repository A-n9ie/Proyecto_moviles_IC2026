package com.example.cletaeats_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IRestauranteRepository
import com.example.cletaeats_mobile.domain.model.Restaurante
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RestauranteUiState(
    val isLoading:    Boolean          = false,
    val restaurantes: List<Restaurante> = emptyList(),
    val errorMsg:     String?          = null
)

class RestauranteViewModel(private val repo: IRestauranteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RestauranteUiState())
    val uiState: StateFlow<RestauranteUiState> = _uiState.asStateFlow()

    fun cargarRestaurantes() {
        _uiState.value = RestauranteUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = repo.obtenerRestaurantes()) {
                is Result.Success -> _uiState.value = RestauranteUiState(
                    restaurantes = result.data
                )
                is Result.Error -> _uiState.value = RestauranteUiState(
                    errorMsg = result.message
                )
            }
        }
    }
}