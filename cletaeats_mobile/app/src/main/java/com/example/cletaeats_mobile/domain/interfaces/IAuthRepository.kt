package com.example.cletaeats_mobile.domain.interfaces

import com.example.cletaeats_mobile.data.local.DataMode
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.model.Usuario

interface IAuthRepository {
    suspend fun login(email: String, password: String, modo: DataMode): Result<Usuario>

    suspend fun registroCliente(
        email: String, password: String, confirmarPassword: String,
        cedula: String, nombre: String, direccion: String,
        telefono: String, tarjeta: String
    ): Result<Usuario>

    suspend fun registroRepartidor(
        email: String, password: String, confirmarPassword: String,
        cedula: String, nombre: String, correoContacto: String,
        direccion: String, telefono: String, tarjeta: String
    ): Result<Usuario>
    suspend fun logout(): Result<Unit>
}