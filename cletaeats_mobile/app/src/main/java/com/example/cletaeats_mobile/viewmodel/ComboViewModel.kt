package com.example.cletaeats_mobile.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IComboRepository
import com.example.cletaeats_mobile.domain.model.Combo
import com.example.cletaeats_mobile.domain.model.Restaurante
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ComboUiState(
    val isLoading:   Boolean      = false,
    val restaurante: Restaurante? = null,
    val combos:      List<Combo>  = emptyList(),
    val errorMsg:    String?      = null
)

class ComboViewModel(private val repo: IComboRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ComboUiState())
    val uiState: StateFlow<ComboUiState> = _uiState.asStateFlow()

    fun cargarCombos(restauranteId: Int) {
        val state = _uiState.value
        if (state.combos.isNotEmpty() &&
            state.combos.first().restauranteId == restauranteId) {
            android.util.Log.d("ComboViewModel", "cargarCombos: cache hit, no recarga ($restauranteId)")
            return
        }

        android.util.Log.d("ComboViewModel", "cargarCombos: iniciando carga para restauranteId=$restauranteId")
        _uiState.value = ComboUiState(isLoading = true)
        viewModelScope.launch {
            android.util.Log.d("ComboViewModel", "cargarCombos: dentro de coroutine, llamando al repo")
            when (val result = repo.obtenerCombosPorRestaurante(restauranteId)) {
                is Result.Success -> {
                    android.util.Log.d("ComboViewModel", "cargarCombos: éxito, combos=${result.data.combos.size}")
                    _uiState.value = ComboUiState(
                        restaurante = result.data.restaurante,
                        combos      = result.data.combos
                    )
                }
                is Result.Error -> {
                    android.util.Log.d("ComboViewModel", "cargarCombos: error -> ${result.message}")
                    _uiState.value = ComboUiState(errorMsg = result.message)
                }
            }
        }
    }
}