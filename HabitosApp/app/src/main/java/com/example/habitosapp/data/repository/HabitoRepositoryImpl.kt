package com.example.habitosapp.data.repository

import android.os.Handler
import android.os.Looper
import com.example.habitosapp.data.local.SessionManager
import com.example.habitosapp.data.remote.HabitoApiService
import com.example.habitosapp.domain.Result
import com.example.habitosapp.domain.interfaces.IHabitoRepository
import com.example.habitosapp.domain.model.Habito

class HabitoRepositoryImpl(
    private val api: HabitoApiService,
    private val session: SessionManager
) : IHabitoRepository {

    private val main = Handler(Looper.getMainLooper())

    override fun obtenerHabitos(callback: (Result<List<Habito>>) -> Unit) {
        Thread {
            val result = try {
                val (status, habitos) = api.obtenerHabitos(session.getToken())
                when (status) {
                    200  -> Result.Success(habitos)
                    401  -> Result.Error("Sesión expirada. Volvé a iniciar sesión.")
                    -1   -> Result.Error("Sin conexión al servidor")
                    else -> Result.Error("Error al obtener hábitos ($status)")
                }
            } catch (e: Exception) {
                Result.Error("Error inesperado: ${e.message}")
            }
            main.post { callback(result) }
        }.start()
    }

    override fun crearHabito(
        nombre: String,
        descripcion: String,
        callback: (Result<Habito>) -> Unit
    ) {
        Thread {
            val result = try {
                val (status, habito) = api.crearHabito(nombre, descripcion, session.getToken())
                when {
                    status == 201 && habito != null -> Result.Success(habito)
                    status == 401 -> Result.Error("Sesión expirada")
                    status == -1  -> Result.Error("Sin conexión al servidor")
                    else          -> Result.Error("Error al crear hábito ($status)")
                }
            } catch (e: Exception) {
                Result.Error("Error inesperado: ${e.message}")
            }
            main.post { callback(result) }
        }.start()
    }

    override fun actualizarHabito(
        habito: Habito,
        callback: (Result<Habito>) -> Unit
    ) {
        Thread {
            val result = try {
                val (status, updated) = api.actualizarHabito(habito, session.getToken())
                when {
                    status == 200 && updated != null -> Result.Success(updated)
                    status == 404 -> Result.Error("Hábito no encontrado")
                    status == 401 -> Result.Error("Sesión expirada")
                    status == -1  -> Result.Error("Sin conexión al servidor")
                    else          -> Result.Error("Error al actualizar ($status)")
                }
            } catch (e: Exception) {
                Result.Error("Error inesperado: ${e.message}")
            }
            main.post { callback(result) }
        }.start()
    }

    override fun eliminarHabito(id: Int, callback: (Result<Unit>) -> Unit) {
        Thread {
            val result = try {
                when (val status = api.eliminarHabito(id, session.getToken())) {
                    200  -> Result.Success(Unit)
                    404  -> Result.Error("Hábito no encontrado")
                    401  -> Result.Error("Sesión expirada")
                    -1   -> Result.Error("Sin conexión al servidor")
                    else -> Result.Error("Error al eliminar ($status)")
                }
            } catch (e: Exception) {
                Result.Error("Error inesperado: ${e.message}")
            }
            main.post { callback(result) }
        }.start()
    }
}