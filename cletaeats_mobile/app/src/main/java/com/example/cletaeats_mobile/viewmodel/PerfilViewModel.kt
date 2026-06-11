package com.example.cletaeats_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IPerfilRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PerfilUiState(
    val isLoading:  Boolean = false,
    val nombre:     String  = "",
    val telefono:   String  = "",
    val direccion:  String  = "",
    val email:      String  = "",
    val cedula:     String  = "",   // solo lectura, no editable
    val imagenUrl: String = "",
    val guardadoOk: Boolean = false,
    val errorMsg:   String? = null
)

class PerfilViewModel(
    private val repo:    IPerfilRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    /** Carga los datos actuales del perfil desde la API */
    fun cargarPerfil() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMsg = null)
        viewModelScope.launch {
            when (val result = repo.obtenerPerfil()) {
                is Result.Success -> _uiState.value = PerfilUiState(
                    nombre    = result.data.nombre,
                    telefono  = result.data.telefono,
                    direccion = result.data.direccion,
                    email     = session.getEmail(),
                    cedula    = result.data.cedula,
                    imagenUrl = result.data.imagenUrl
                )
                is Result.Error -> _uiState.value = PerfilUiState(
                    email    = session.getEmail(),
                    errorMsg = result.message
                )
            }
        }
    }

    /** Envía los cambios al servidor y actualiza la sesión local */
    fun actualizarPerfil(nombre: String, telefono: String, direccion: String) {
        if (nombre.isBlank() || telefono.isBlank() || direccion.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMsg = "Completá todos los campos")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMsg = null, guardadoOk = false)
        viewModelScope.launch {
            when (val result = repo.actualizarPerfil(nombre, telefono, direccion)) {
                is Result.Success -> {
                    // Actualizar el nombre en la sesión local para que el header lo refleje
                    session.saveSession(
                        token      = session.getToken(),
                        idUsuario  = session.getIdUsuario(),
                        email      = session.getEmail(),
                        rol        = session.getRol(),
                        nombre     = nombre,
                        idPerfil   = session.getIdPerfil()
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading  = false,
                        nombre     = nombre,
                        telefono   = telefono,
                        direccion  = direccion,
                        guardadoOk = true
                    )
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMsg  = result.message
                )
            }
        }
    }

    fun limpiarEstado() {
        _uiState.value = _uiState.value.copy(guardadoOk = false, errorMsg = null)
    }
}
