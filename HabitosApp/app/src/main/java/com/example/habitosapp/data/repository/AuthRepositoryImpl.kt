package com.example.habitosapp.data.repository

import android.os.Handler
import android.os.Looper
import com.example.habitosapp.data.local.SessionManager
import com.example.habitosapp.data.remote.AuthApiService
import com.example.habitosapp.domain.Result
import com.example.habitosapp.domain.interfaces.IAuthRepository
import com.example.habitosapp.domain.model.Usuario

class AuthRepositoryImpl(
    private val api: AuthApiService,
    private val session: SessionManager
) : IAuthRepository {

    // Handler al Main Thread: todos los callbacks llegan aquí
    private val main = Handler(Looper.getMainLooper())

    override fun login(
        nombreUsuario: String,
        password: String,
        callback: (Result<Usuario>) -> Unit
    ) {
        Thread {
            val result = try {
                val (status, resp) = api.login(nombreUsuario, password)
                when {
                    status == 200 && resp != null -> {
                        session.saveSession(resp.token, resp.idUsuario, resp.nombreUsuario)
                        Result.Success(Usuario(resp.idUsuario, resp.nombreUsuario, resp.token))
                    }
                    status == -1  -> Result.Error("Sin conexión al servidor")
                    status == 401 -> Result.Error("Credenciales incorrectas")
                    else          -> Result.Error("Error del servidor ($status)")
                }
            } catch (e: Exception) {
                Result.Error("Error inesperado: ${e.message}")
            }
            main.post { callback(result) }
        }.start()
    }

    override fun registro(
        nombreUsuario: String,
        email: String,
        password: String,
        confirmarPassword: String,
        callback: (Result<Usuario>) -> Unit
    ) {
        Thread {
            val result = try {
                val (status, resp) = api.registro(nombreUsuario, email, password, confirmarPassword)
                when {
                    status == 201 && resp != null -> {
                        session.saveSession(resp.token, resp.idUsuario, resp.nombreUsuario)
                        Result.Success(Usuario(resp.idUsuario, resp.nombreUsuario, resp.token))
                    }
                    status == -1  -> Result.Error("Sin conexión al servidor")
                    status == 400 -> Result.Error("Datos inválidos. Revise los campos ingresados.")
                    else          -> Result.Error("No se pudo registrar ($status)")
                }
            } catch (e: Exception) {
                Result.Error("Error inesperado: ${e.message}")
            }
            main.post { callback(result) }
        }.start()
    }

    override fun logout(callback: (Result<Unit>) -> Unit) {
        Thread {
            try {
                api.logout(session.getToken())   // Intento de logout remoto
            } catch (_: Exception) {
                // Si el servidor no responde, el logout local se hace igual
            }
            session.clearSession()
            main.post { callback(Result.Success(Unit)) }
        }.start()
    }
}