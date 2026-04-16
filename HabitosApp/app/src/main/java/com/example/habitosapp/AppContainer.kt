package com.example.habitosapp

import android.content.Context
import com.example.habitosapp.data.local.SessionManager
import com.example.habitosapp.data.remote.AuthApiService
import com.example.habitosapp.data.remote.HabitoApiService
import com.example.habitosapp.data.repository.AuthRepositoryImpl
import com.example.habitosapp.data.repository.HabitoRepositoryImpl
import com.example.habitosapp.domain.interfaces.IAuthRepository
import com.example.habitosapp.domain.interfaces.IHabitoRepository
import com.example.habitosapp.viewmodel.AuthViewModel
import com.example.habitosapp.viewmodel.HabitosViewModel

/**
 * Contenedor de dependencias manual (DI sin framework).
 *
*/
object AppContainer {

    private lateinit var sessionManager: SessionManager

    // ─── Inicialización ─────────────────────────────────────────────
    fun init(context: Context) {
        sessionManager = SessionManager(context.applicationContext)
    }

    // ─── Repositorios (lazy: se crean solo cuando se necesitan) ─────
    private val authRepository: IAuthRepository by lazy {
        AuthRepositoryImpl(AuthApiService(), sessionManager)
    }

    private val habitoRepository: IHabitoRepository by lazy {
        HabitoRepositoryImpl(HabitoApiService(), sessionManager)
    }

    // ─── Factories de ViewModel ─────────────────────────────────────
    // Se usan factories y no singletons porque cada Activity puede
    // tener su propio ciclo de vida.
    fun authViewModel()    = AuthViewModel(authRepository)
    fun habitosViewModel() = HabitosViewModel(habitoRepository)

    // ─── Acceso directo a SessionManager ────────────────────────────
    fun getSessionManager() = sessionManager
}