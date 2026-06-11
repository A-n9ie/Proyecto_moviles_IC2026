package com.example.cletaeats_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.data.remote.IRepartidorPerfilApi
import com.example.cletaeats_mobile.data.remote.PerfilRepartidorRequest
import com.example.cletaeats_mobile.data.remote.TarjetaRepartidorRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PerfilRepartidorUiState(
    val isLoading:  Boolean = false,
    val nombre:     String  = "",
    val telefono:   String  = "",
    val direccion:  String  = "",
    val correo:     String  = "",
    val cedula:     String  = "",
    val tarjeta:    String  = "",
    val rating:     Double  = 0.0,
    val guardadoOk: Boolean = false,
    val errorMsg:   String? = null
)

class PerfilRepartidorViewModel(
    private val api:     IRepartidorPerfilApi,  // interfaz nueva, ver 6c
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilRepartidorUiState())
    val uiState: StateFlow<PerfilRepartidorUiState> = _uiState.asStateFlow()

    fun cargarPerfil() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMsg = null)
        viewModelScope.launch {
            try {
                val resp = api.obtenerPerfil("Bearer ${session.getToken()}")
                if (resp.isSuccessful && resp.body() != null) {
                    val d = resp.body()!!
                    _uiState.value = PerfilRepartidorUiState(
                        nombre    = d.nombre,
                        telefono  = d.telefono,
                        direccion = d.direccion,
                        correo    = d.correo,
                        cedula    = d.cedula,
                        tarjeta   = d.tarjeta,
                        rating    = d.rating
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMsg = "Error al cargar perfil (${resp.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMsg = e.message)
            }
        }
    }

    fun actualizarPerfil(nombre: String, telefono: String, direccion: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, guardadoOk = false, errorMsg = null)
        viewModelScope.launch {
            try {
                val resp = api.actualizarPerfil(
                    "Bearer ${session.getToken()}",
                    PerfilRepartidorRequest(nombre, telefono, direccion)
                )
                if (resp.isSuccessful) {
                    session.saveSession(
                        token     = session.getToken(),
                        idUsuario = session.getIdUsuario(),
                        email     = session.getEmail(),
                        rol       = session.getRol(),
                        nombre    = nombre,
                        idPerfil  = session.getIdPerfil()
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, guardadoOk = true,
                        nombre = nombre, telefono = telefono, direccion = direccion
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMsg = "No se pudo guardar (${resp.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMsg = e.message)
            }
        }
    }

    fun actualizarTarjeta(tarjeta: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, guardadoOk = false, errorMsg = null)
        viewModelScope.launch {
            try {
                val resp = api.actualizarTarjeta(
                    "Bearer ${session.getToken()}",
                    TarjetaRepartidorRequest(tarjeta)
                )
                if (resp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isLoading = false, guardadoOk = true, tarjeta = tarjeta)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMsg = "No se pudo guardar la tarjeta (${resp.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMsg = e.message)
            }
        }
    }
}