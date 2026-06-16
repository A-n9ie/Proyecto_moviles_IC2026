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
import com.example.cletaeats_mobile.data.remote.FirebaseAuthHelper
import com.example.cletaeats_mobile.data.sync.SyncManager

class AuthRepositoryImpl(
    private val api:     IAuthApi,
    private val tarjetaApi: ITarjetaApi,
    private val session: SessionManager,
    private val sync:    SyncManager? = null
) : IAuthRepository {

    override suspend fun login(email: String, password: String, modo: DataMode): Result<Usuario> =
        withContext(Dispatchers.IO) {

            // ── MODO LOCAL: autenticar con SQLite ───────────────────
            if (modo == DataMode.LOCAL_SQLITE) {
                // 1. Buscar en SQLite el usuario que inició sesión antes
                val db = com.example.cletaeats_mobile.data.local.db.CletaEatsDatabase
                    .getInstance(session.getApplicationContext())   // ← ver Paso 9 para agregar esto a SessionManager
                val localUser = db.usuarioLocalDao().buscarPorEmail(email)

                return@withContext if (localUser != null) {
                    session.saveDataMode(modo)
                    session.saveSession(
                        localUser.token, localUser.idUsuario, localUser.email,
                        localUser.rol, localUser.nombre, localUser.idPerfil
                    )
                    Result.Success(
                        Usuario(
                            idUsuario = localUser.idUsuario,
                            email     = localUser.email,
                            rol       = localUser.rol,
                            nombre    = localUser.nombre,
                            idPerfil  = localUser.idPerfil,
                            token     = localUser.token
                        )
                    )
                } else {
                    // Fallback: intentar con offlinePrefs (compatibilidad hacia atrás)
                    val savedEmail = session.getOfflineEmail()
                    val savedToken = session.getOfflineToken()
                    if (savedToken.isNotEmpty() && savedEmail == email) {
                        session.saveDataMode(modo)
                        session.saveSession(
                            savedToken, session.getOfflineIdUsuario(), savedEmail,
                            session.getOfflineRol(), session.getOfflineNombre(), session.getOfflineIdPerfil()
                        )
                        Result.Success(
                            Usuario(
                                idUsuario = session.getOfflineIdUsuario(), email = savedEmail,
                                rol       = session.getOfflineRol(), nombre = session.getOfflineNombre(),
                                idPerfil  = session.getOfflineIdPerfil(), token = savedToken
                            )
                        )
                    } else {
                        Result.Error("No hay sesión guardada para este correo. Iniciá sesión en línea primero.")
                    }
                }
            }

            // ── MODO CLOUD: primero autenticar con Firebase Auth ────
            if (modo == DataMode.CLOUD) {
                // 1. Autenticar con Firebase Auth (email/password)
                val firebaseResult = FirebaseAuthHelper.signIn(email, password)
                if (firebaseResult.isFailure) {
                    val msg = firebaseResult.exceptionOrNull()?.message ?: "Error de autenticación Cloud"
                    // Mapear errores comunes de Firebase a mensajes legibles
                    val friendlyMsg = when {
                        msg.contains("INVALID_LOGIN_CREDENTIALS") ||
                                msg.contains("invalid-credential")        -> "Correo o contraseña incorrectos"
                        msg.contains("user-not-found")            -> "No existe cuenta con este correo en Cloud"
                        msg.contains("wrong-password")            -> "Contraseña incorrecta"
                        msg.contains("too-many-requests")         -> "Demasiados intentos. Intentá más tarde"
                        msg.contains("network")                   -> "Sin conexión a internet"
                        else -> "Error de autenticación: $msg"
                    }
                    return@withContext Result.Error(friendlyMsg)
                }
                // Firebase Auth OK — ahora también validar con el backend para obtener el JWT y el rol
                // (Firebase no conoce los roles ni genera el JWT de CletaEats)
            }

            // ── MODOS API_REMOTA y CLOUD: llamada HTTP al backend ───
            try {
                val resp = api.login(LoginRequest(email, password))
                when (resp.code()) {
                    200 -> {
                        val body = resp.body()!!
                        session.saveDataMode(modo)
                        session.saveSession(body.token, body.idUsuario, body.email,
                            body.rol, body.nombre, body.idPerfil)

                        when (modo) {
                            DataMode.API_REMOTA -> {
                                // Guardar usuario en SQLite para uso offline posterior
                                sync?.guardarUsuarioLocal(body.idUsuario, body.email,
                                    body.nombre, body.rol, body.idPerfil, body.token)
                                sync?.sincronizarDesdeApi(body.token, body.rol)
                            }
                            DataMode.CLOUD -> {
                                // 1. Guardar usuario en SQLite
                                sync?.guardarUsuarioLocal(body.idUsuario, body.email,
                                    body.nombre, body.rol, body.idPerfil, body.token)
                                // 2. Bajar datos del API a SQLite
                                sync?.sincronizarDesdeApi(body.token, body.rol)
                                // 3. Subir de SQLite a Firestore
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

                        // Guardar usuario en SQLite local siempre
                        sync?.guardarUsuarioLocal(body.idUsuario, body.email,
                            body.nombre, body.rol, body.idPerfil, body.token)

                        // Si el modo actual es CLOUD, crear también en Firebase Auth
                        if (session.getDataMode() == DataMode.CLOUD) {
                            val fbResult = FirebaseAuthHelper.createUser(email, password)
                            if (fbResult.isFailure) {
                                // No es fatal: el usuario ya quedó en el backend.
                                // Solo loguear el warning.
                                android.util.Log.w("AUTH",
                                    "Registro en Firebase falló: ${fbResult.exceptionOrNull()?.message}")
                            }
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

                    session.saveSession(
                        body.token,
                        body.idUsuario,
                        body.email,
                        body.rol,
                        body.nombre,
                        body.idPerfil
                    )

                    sync?.guardarUsuarioLocal(
                        body.idUsuario,
                        body.email,
                        body.nombre,
                        body.rol,
                        body.idPerfil,
                        body.token
                    )

                    if (session.getDataMode() == DataMode.CLOUD) {
                        FirebaseAuthHelper.createUser(email, password)
                            .onFailure {
                                android.util.Log.w(
                                    "AUTH",
                                    "Firebase repartidor registro: ${it.message}"
                                )
                            }
                    }

                    Result.Success(
                        Usuario(
                            body.idUsuario,
                            body.email,
                            body.rol,
                            body.nombre,
                            body.idPerfil,
                            body.token
                        )
                    )
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
            FirebaseAuthHelper.signOut()
            Result.Success(Unit)
        } catch (_: Exception) {
            session.clearSession()
            FirebaseAuthHelper.signOut()
            Result.Success(Unit)
        }
    }
}