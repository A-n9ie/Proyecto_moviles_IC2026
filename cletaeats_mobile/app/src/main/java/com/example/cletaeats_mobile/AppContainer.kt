package com.example.cletaeats_mobile

import android.content.Context
import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.data.remote.AuthApiService
import com.example.cletaeats_mobile.data.remote.RestauranteApiService
import com.example.cletaeats_mobile.data.repository.AuthRepositoryImpl
import com.example.cletaeats_mobile.data.repository.RestauranteRepositoryImpl
import com.example.cletaeats_mobile.viewmodel.AuthViewModel
import com.example.cletaeats_mobile.viewmodel.RestauranteViewModel

/**
 * Contenedor de dependencias manual (MVC sin framework DI).
 *
 * PATRÓN MVC aplicado:
 *   Model       = data classes + repos (data/repository/, domain/model/)
 *   View        = Composables (ui/)
 *   Controller  = ViewModels (viewmodel/)
 *
 * FUTURO: Reemplazable 1:1 con Hilt sin modificar ningún ViewModel ni Screen.
 */
object AppContainer {

    private lateinit var sessionManager: SessionManager

    fun init(context: Context) {
        sessionManager = SessionManager(context.applicationContext)
    }

    // ── Repositorios (lazy: se crean al primer uso) ────────────────
    private val authRepository by lazy {
        AuthRepositoryImpl(AuthApiService(), sessionManager)
    }

    private val restauranteRepository by lazy {
        RestauranteRepositoryImpl(RestauranteApiService(), sessionManager)
    }

    // ── Factories de ViewModel ─────────────────────────────────────
    // Factory (no singleton) porque cada ViewModel tiene su propio ciclo
    fun authViewModel()        = AuthViewModel(authRepository)
    fun restauranteViewModel() = RestauranteViewModel(restauranteRepository)

    fun getSessionManager()    = sessionManager
}