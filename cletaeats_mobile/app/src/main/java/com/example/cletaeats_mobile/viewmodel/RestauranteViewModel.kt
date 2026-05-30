package com.example.cletaeats_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IRestauranteRepository
import com.example.cletaeats_mobile.domain.model.Categoria
import com.example.cletaeats_mobile.domain.model.Restaurante
import com.example.cletaeats_mobile.data.local.DataMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
data class RestauranteUiState(
    val isLoading:            Boolean           = false,
    val restaurantes:         List<Restaurante> = emptyList(),
    val restaurantesFiltrados: List<Restaurante> = emptyList(),
    val categorias:           List<Categoria>   = emptyList(),
    val categoriasSeleccionadas: Set<String>    = emptySet(),
    val modoActivo:           DataMode          = DataMode.API_REMOTA,
    val errorMsg:             String?           = null
)

class RestauranteViewModel(
    private val repo: IRestauranteRepository,
    private val modo: DataMode = DataMode.API_REMOTA
    ) : ViewModel() {
    private val _uiState = MutableStateFlow(RestauranteUiState(modoActivo = modo))
    val uiState: StateFlow<RestauranteUiState> = _uiState.asStateFlow()

    fun cargarRestaurantes() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMsg = null
        )
        viewModelScope.launch {
            // Cargar categorías y restaurantes en paralelo
            val catResult  = repo.obtenerCategorias()
            val restResult = repo.obtenerRestaurantes()

            val categorias   = (catResult  as? Result.Success)?.data ?: emptyList()
            val restaurantes = (restResult as? Result.Success)?.data

            if (restaurantes == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMsg = (restResult as Result.Error).message,
                    categorias = categorias
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    restaurantes = restaurantes,
                    restaurantesFiltrados = restaurantes,
                    categorias = categorias
                )
            }
        }
    }

    fun toggleCategoria(nombreCategoria: String) {
        val seleccionadas = _uiState.value.categoriasSeleccionadas.toMutableSet()
        if (nombreCategoria in seleccionadas) {
            seleccionadas.remove(nombreCategoria)
        } else {
            seleccionadas.add(nombreCategoria)
        }
        _aplicarFiltro(seleccionadas)
    }

    fun limpiarFiltros() {
        _aplicarFiltro(emptySet())
    }

    private fun _aplicarFiltro(seleccionadas: Set<String>) {
        val todos = _uiState.value.restaurantes
        val filtrados = if (seleccionadas.isEmpty()) {
            todos
        } else {
            todos.filter { r ->
                r.categorias.any { it in seleccionadas }
            }
        }
        _uiState.value = _uiState.value.copy(
            categoriasSeleccionadas  = seleccionadas,
            restaurantesFiltrados    = filtrados
        )
    }
}