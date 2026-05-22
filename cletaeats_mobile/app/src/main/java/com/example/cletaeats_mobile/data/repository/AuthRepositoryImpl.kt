package com.example.cletaeats_mobile.data.repository

import android.annotation.TargetApi
import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.data.remote.AgregarTarjetaRequest
import com.example.cletaeats_mobile.data.remote.IAuthApi
import com.example.cletaeats_mobile.data.remote.ITarjetaApi
import com.example.cletaeats_mobile.data.remote.LoginRequest
import com.example.cletaeats_mobile.data.remote.RegistroClienteRequest
import com.example.cletaeats_mobile.data.remote.RegistroRepartidorRequest
import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.interfaces.IAuthRepository
import com.example.cletaeats_mobile.domain.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val api:     IAuthApi,
    private val tarjetaApi: ITarjetaApi,
    private val session: SessionManager
) : IAuthRepository {

    override suspend fun login(email: String, password: String): Result<Usuario> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.login(LoginRequest(email, password))
                when (resp.code()) {
                    200 -> {
                        val body = resp.body()!!
                        session.saveSession(body.token, body.idUsuario, body.email,
                                            body.rol, body.nombre, body.idPerfil)
                        Result.Success(Usuario(body.idUsuario, body.email, body.rol,
                                               body.nombre, body.idPerfil, body.token))
                    }
                    401 -> Result.Error("Correo o contraseña incorrectos")
                    403 -> Result.Error("Este rol solo puede acceder desde la web")
                    else -> Result.Error("Error del servidor (${resp.code()})")
                }
            } catch (e: Exception) {
                Result.Error("Sin conexión al servidor")
            }
        }

    override suspend fun registroCliente(
        email: String, password: String, confirmarPassword: String,
        cedula: String, nombre: String, direccion: String,
        telefono: String,
        tarjeta: String
    ): Result<Usuario> =
        withContext(Dispatchers.IO) {
        try {
            val resp = api.registroCliente(
                RegistroClienteRequest(
                    email, password, confirmarPassword,
                    cedula, nombre, direccion, telefono
                )
            )
            when (resp.code()) {
                201 -> {
                    val body = resp.body()!!
                    session.saveSession(body.token, body.idUsuario, body.email,
                                        body.rol, body.nombre, body.idPerfil)
                    if (tarjeta.isNotEmpty()) {
                        try {
                            tarjetaApi.agregarTarjeta(
                                "Bearer ${body.token}",
                                AgregarTarjetaRequest(tarjeta, "Principal", 1)
                            )
                        } catch (_: Exception) { /* no bloquear el registro si falla */ }
                    }
                    Result.Success(Usuario(body.idUsuario, body.email, body.rol,
                                           body.nombre, body.idPerfil, body.token))
                }
                400 -> {
                    val errorMsg = resp.errorBody()?.string()
                    Result.Error(errorMsg ?: "Datos inválidos")
                }
                else -> Result.Error("No se pudo registrar (${resp.code()})")
            }
        } catch (e: Exception) {
            Result.Error("Sin conexión al servidor")
        }
    }

    override suspend fun registroRepartidor(
        email: String, password: String, confirmarPassword: String,
        cedula: String, nombre: String, correoContacto: String,
        direccion: String, telefono: String, tarjeta: String
    ): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val resp = api.registroRepartidor(
                RegistroRepartidorRequest(email, password, confirmarPassword,
                                          cedula, nombre, correoContacto,
                                          direccion, telefono, tarjeta)
            )
            when (resp.code()) {
                201 -> {
                    val body = resp.body()!!
                    session.saveSession(body.token, body.idUsuario, body.email,
                                        body.rol, body.nombre, body.idPerfil)
                    Result.Success(Usuario(body.idUsuario, body.email, body.rol,
                                           body.nombre, body.idPerfil, body.token))
                }
                400 -> Result.Error("Datos inválidos. Revisá los campos.")
                else -> Result.Error("No se pudo registrar (${resp.code()})")
            }
        } catch (e: Exception) {
            Result.Error("Sin conexión al servidor")
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            api.logout("Bearer ${session.getToken()}")
            session.clearSession()
            Result.Success(Unit)
        } catch (_: Exception) {
            session.clearSession()
            Result.Success(Unit)
        }
    }
}