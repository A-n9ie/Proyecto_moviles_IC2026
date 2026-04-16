package com.example.habitosapp.viewmodel

import com.example.habitosapp.domain.Result
import com.example.habitosapp.domain.interfaces.IAuthRepository
import com.example.habitosapp.domain.model.Usuario

/**
 * ViewModel manual de autenticación.
 *
 * FUTURO: Heredar de AndroidViewModel y reemplazar callbacks
 *         por LiveData<Result<Usuario>> sin cambiar los repositorios.
 */
class AuthViewModel(private val repo: IAuthRepository) {

    fun login(
        nombreUsuario: String,
        password: String,
        onSuccess: (Usuario) -> Unit,
        onError: (String) -> Unit
    ) {
        repo.login(nombreUsuario, password) { result ->
            when (result) {
                is Result.Success -> onSuccess(result.data)
                is Result.Error   -> onError(result.message)
            }
        }
    }

    fun registro(
        nombreUsuario: String,
        email: String,
        password: String,
        confirmarPassword: String,
        onSuccess: (Usuario) -> Unit,
        onError: (String) -> Unit
    ) {
        repo.registro(nombreUsuario, email, password, confirmarPassword) { result ->
            when (result) {
                is Result.Success -> onSuccess(result.data)
                is Result.Error   -> onError(result.message)
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        repo.logout { onComplete() }
    }
}