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

import com.example.cletaeats_mobile.data.local.DataMode
import com.example.cletaeats_mobile.data.sync.SyncManager

class AuthRepositoryImpl(
    private val api:     IAuthApi,
    private val tarjetaApi: ITarjetaApi,
    private val session: SessionManager,
    private val sync:    SyncManager? = null
) : IAuthRepository {

    override suspend fun login(email: String, password: String, modo: DataMode): Result<Usuario> =
        withContext(Dispatchers.IO) {

            // ── MODO LOCAL: autenticar con sesión previa guardada ─────
            if (modo == DataMode.LOCAL_SQLITE) {
                val savedEmail = session.getOfflineEmail()   // ← cambiar getEmail() por getOfflineEmail()
                val savedToken = session.getOfflineToken()   // ← cambiar getToken() por getOfflineToken()
                return@withContext if (savedToken.isNotEmpty() && savedEmail == email) {
                    session.saveDataMode(modo)
                    // Restaurar la sesión activa desde las credenciales offline
                    session.saveSession(
                        savedToken,
                        session.getOfflineIdUsuario(),
                        savedEmail,
                        session.getOfflineRol(),
                        session.getOfflineNombre(),
                        session.getOfflineIdPerfil()
                    )
                    Result.Success(
                        Usuario(
                            idUsuario       = session.getOfflineIdUsuario(),
                            email    = savedEmail,
                            rol      = session.getOfflineRol(),
                            nombre   = session.getOfflineNombre(),
                            idPerfil = session.getOfflineIdPerfil(),
                            token    = savedToken
                        )
                    )
                } else {
                    Result.Error("No hay sesión guardada para este correo. Iniciá sesión en línea primero.")
                }
            }

            // ── MODOS REMOTO / CLOUD: llamada HTTP normal ─────────────
            try {
                val resp = api.login(LoginRequest(email, password))
                when (resp.code()) {
                    200 -> {
                        val body = resp.body()!!
                        session.saveDataMode(modo)
                        session.saveSession(body.token, body.idUsuario, body.email,
                            body.rol, body.nombre, body.idPerfil)

                        // Sync según el modo seleccionado
                        when (modo) {
                            DataMode.API_REMOTA -> {
                                // Guardar en SQLite para uso offline posterior
                                sync?.sincronizarDesdeApi(body.token)
                            }
                            DataMode.CLOUD -> {
                                // 1. Bajar del API a SQLite
                                sync?.sincronizarDesdeApi(body.token)
                                // 2. Subir de SQLite a Firestore
                                sync?.sincronizarHaciaCloud()
                            }
                            else -> {}
                        }

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
        telefono: String, tarjeta: String,
        fechaVencimiento: String, cvv: String
    ): Result<Usuario> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.registroCliente(
                    RegistroClienteRequest(
                        email = email, password = password,
                        confirmarPassword = confirmarPassword,
                        cedula = cedula, nombre = nombre,
                        direccion = direccion, telefono = telefono,
                        numeroTarjeta = tarjeta,
                        fechaVencimiento = fechaVencimiento,
                        cvvTarjeta = cvv
                    )
                )
                when (resp.code()) {
                    201 -> {
                        val body = resp.body()!!
                        session.saveSession(body.token, body.idUsuario, body.email,
                            body.rol, body.nombre, body.idPerfil)
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