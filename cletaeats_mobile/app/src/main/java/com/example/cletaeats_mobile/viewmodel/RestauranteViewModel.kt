package com.example.cletaeats_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IRestauranteRepository
import com.example.cletaeats_mobile.domain.model.Categoria
import com.example.cletaeats_mobile.domain.model.Restaurante
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RestauranteUiState(
    val isLoading:            Boolean           = false,
    val restaurantes:         List<Restaurante> = emptyList(),
    val restaurantesFiltrados: List<Restaurante> = emptyList(), // ← nuevo
    val categorias:           List<Categoria>   = emptyList(),  // ← nuevo
    val categoriasSeleccionadas: Set<String>    = emptySet(),   // ← nuevo
    val errorMsg:             String?           = null
)

class RestauranteViewModel(private val repo: IRestauranteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RestauranteUiState())
    val uiState: StateFlow<RestauranteUiState> = _uiState.asStateFlow()

    fun cargarRestaurantes() {
        _uiState.value = RestauranteUiState(isLoading = true)
        viewModelScope.launch {
            // Cargar categorías y restaurantes en paralelo
            val catResult  = repo.obtenerCategorias()
            val restResult = repo.obtenerRestaurantes()

            val categorias   = (catResult  as? Result.Success)?.data ?: emptyList()
            val restaurantes = (restResult as? Result.Success)?.data

            if (restaurantes == null) {
                _uiState.value = RestauranteUiState(
                    errorMsg   = (restResult as Result.Error).message,
                    categorias = categorias
                )
            } else {
                _uiState.value = RestauranteUiState(
                    restaurantes          = restaurantes,
                    restaurantesFiltrados = restaurantes,
                    categorias            = categorias
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