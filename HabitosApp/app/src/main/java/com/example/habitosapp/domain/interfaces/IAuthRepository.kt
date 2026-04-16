package com.example.habitosapp.domain.interfaces

import com.example.habitosapp.domain.Result
import com.example.habitosapp.domain.model.Usuario

interface IAuthRepository {
    fun login(
        nombreUsuario: String,
        password: String,
        callback: (Result<Usuario>) -> Unit
    )
    fun registro(
        nombreUsuario: String,
        email: String,
        password: String,
        confirmarPassword: String,
        callback: (Result<Usuario>) -> Unit
    )
    fun logout(callback: (Result<Unit>) -> Unit)
}