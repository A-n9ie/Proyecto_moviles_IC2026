package com.example.cletaeats_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * PATRÓN MVC: este ViewModel actúa como CONTROLLER.
 * No conoce Composables, ni repositorios concretos (solo IAuthRepository).
 *
 * Usa viewModelScope para lanzar coroutines que se cancelan
 * automáticamente al destruir el ViewModel.
 */

data class AuthUiState(
    val isLoading:   Boolean = false,
    val errorMsg:    String? = null,
    val isLoggedIn:  Boolean = false,
    val rol:         String  = "",
    val nombre:      String  = ""
)

class AuthViewModel(private val repo: IAuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = repo.login(email, password)) {
                is Result.Success -> _uiState.value = AuthUiState(
                    isLoggedIn = true,
                    rol        = result.data.rol,
                    nombre     = result.data.nombre
                )
                is Result.Error -> _uiState.value = AuthUiState(
                    errorMsg = result.message
                )
            }
        }
    }

    fun registroCliente(
        email: String, password: String, confirmarPassword: String,
        cedula: String, nombre: String, direccion: String,
        telefono: String, tarjeta: String
    ) {
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = repo.registroCliente(
                email, password, confirmarPassword,
                cedula, nombre, direccion, telefono, tarjeta
            )) {
                is Result.Success -> _uiState.value = AuthUiState(
                    isLoggedIn = true,
                    rol        = result.data.rol,
                    nombre     = result.data.nombre
                )
                is Result.Error -> _uiState.value = AuthUiState(errorMsg = result.message)
            }
        }
    }

    fun registroRepartidor(
        email: String, password: String, confirmarPassword: String,
        cedula: String, nombre: String, correoContacto: String,
        direccion: String, telefono: String, tarjeta: String
    ) {
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = repo.registroRepartidor(
                email, password, confirmarPassword,
                cedula, nombre, correoContacto, direccion, telefono, tarjeta
            )) {
                is Result.Success -> _uiState.value = AuthUiState(
                    isLoggedIn = true,
                    rol        = result.data.rol,
                    nombre     = result.data.nombre
                )
                is Result.Error -> _uiState.value = AuthUiState(errorMsg = result.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
            _uiState.value = AuthUiState()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMsg = null)
    }
}