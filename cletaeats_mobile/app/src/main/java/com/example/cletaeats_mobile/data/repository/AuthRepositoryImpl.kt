package com.example.cletaeats_mobile.data.repository

import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.data.remote.AuthApiService
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IAuthRepository
import com.example.cletaeats_mobile.domain.model.Usuario

class AuthRepositoryImpl(
    private val api:     AuthApiService,
    private val session: SessionManager
) : IAuthRepository {

    override suspend fun login(email: String, password: String): Result<Usuario> {
        return try {
            val (status, resp) = api.login(email, password)
            when {
                status == 200 && resp != null -> {
                    session.saveSession(
                        resp.token, resp.idUsuario, resp.email,
                        resp.rol, resp.nombre, resp.idPerfil
                    )
                    Result.Success(
                        Usuario(resp.idUsuario, resp.email, resp.rol, resp.nombre, resp.idPerfil, resp.token)
                    )
                }
                status == -1  -> Result.Error("Sin conexión al servidor")
                status == 401 -> Result.Error("Correo o contraseña incorrectos")
                status == 403 -> Result.Error("Este rol solo puede acceder desde la web")
                else          -> Result.Error("Error del servidor ($status)")
            }
        } catch (e: Exception) {
            Result.Error("Error inesperado: ${e.message}")
        }
    }

    override suspend fun registroCliente(
        email: String, password: String, confirmarPassword: String,
        cedula: String, nombre: String, direccion: String,
        telefono: String, tarjeta: String
    ): Result<Usuario> {
        return try {
            val (status, resp) = api.registroCliente(
                email, password, confirmarPassword,
                cedula, nombre, direccion, telefono, tarjeta
            )
            when {
                status == 201 && resp != null -> {
                    session.saveSession(
                        resp.token, resp.idUsuario, resp.email,
                        resp.rol, resp.nombre, resp.idPerfil
                    )
                    Result.Success(
                        Usuario(resp.idUsuario, resp.email, resp.rol, resp.nombre, resp.idPerfil, resp.token)
                    )
                }
                status == -1  -> Result.Error("Sin conexión al servidor")
                status == 400 -> Result.Error("Datos inválidos. Revisá los campos.")
                else          -> Result.Error("No se pudo registrar ($status)")
            }
        } catch (e: Exception) {
            Result.Error("Error inesperado: ${e.message}")
        }
    }

    override suspend fun registroRepartidor(
        email: String, password: String, confirmarPassword: String,
        cedula: String, nombre: String, correoContacto: String,
        direccion: String, telefono: String, tarjeta: String
    ): Result<Usuario> {
        return try {
            val (status, resp) = api.registroRepartidor(
                email, password, confirmarPassword,
                cedula, nombre, correoContacto, direccion, telefono, tarjeta
            )
            when {
                status == 201 && resp != null -> {
                    session.saveSession(
                        resp.token, resp.idUsuario, resp.email,
                        resp.rol, resp.nombre, resp.idPerfil
                    )
                    Result.Success(
                        Usuario(resp.idUsuario, resp.email, resp.rol, resp.nombre, resp.idPerfil, resp.token)
                    )
                }
                status == -1  -> Result.Error("Sin conexión al servidor")
                status == 400 -> Result.Error("Datos inválidos. Revisá los campos.")
                else          -> Result.Error("No se pudo registrar ($status)")
            }
        } catch (e: Exception) {
            Result.Error("Error inesperado: ${e.message}")
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            api.logout(session.getToken())
            session.clearSession()
            Result.Success(Unit)
        } catch (_: Exception) {
            session.clearSession()
            Result.Success(Unit)
        }
    }
}