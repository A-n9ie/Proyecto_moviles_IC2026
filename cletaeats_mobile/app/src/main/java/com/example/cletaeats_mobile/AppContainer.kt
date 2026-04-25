package com.example.cletaeats_mobile

import android.content.Context
import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.data.remote.AuthApiService
import com.example.cletaeats_mobile.data.remote.ComboApiService
import com.example.cletaeats_mobile.data.remote.PedidoApiService
import com.example.cletaeats_mobile.data.remote.RestauranteApiService
import com.example.cletaeats_mobile.data.repository.AuthRepositoryImpl
import com.example.cletaeats_mobile.data.repository.ComboRepositoryImpl
import com.example.cletaeats_mobile.data.repository.PedidoRepositoryImpl
import com.example.cletaeats_mobile.data.repository.RestauranteRepositoryImpl
import com.example.cletaeats_mobile.viewmodel.AuthViewModel
import com.example.cletaeats_mobile.viewmodel.CarritoViewModel
import com.example.cletaeats_mobile.viewmodel.ComboViewModel
import com.example.cletaeats_mobile.viewmodel.PedidosRepartidorViewModel
import com.example.cletaeats_mobile.viewmodel.RestauranteViewModel

/**
 * DI manual. PATRÓN MVC:
 *   Model      = data classes + repositorios
 *   View       = Composables (ui/)
 *   Controller = ViewModels (viewmodel/)
 *
 * carritoViewModel es singleton (val, no fun) porque debe
 * persistir el estado del carrito entre CombosScreen y CarritoScreen.
 */
object AppContainer {

    private lateinit var sessionManager: SessionManager

    fun init(context: Context) {
        sessionManager = SessionManager(context.applicationContext)
    }

    // ── Repositorios (lazy) ──────────────────────────────────────
    private val authRepository by lazy {
        AuthRepositoryImpl(AuthApiService(), sessionManager)
    }
    private val restauranteRepository by lazy {
        RestauranteRepositoryImpl(RestauranteApiService(), sessionManager)
    }
    private val comboRepository by lazy {
        ComboRepositoryImpl(ComboApiService(), sessionManager)
    }
    private val pedidoRepository by lazy {
        PedidoRepositoryImpl(PedidoApiService(), sessionManager)
    }

    // ── ViewModels factories (nueva instancia cada vez) ──────────
    fun authViewModel()            = AuthViewModel(authRepository)
    fun restauranteViewModel()     = RestauranteViewModel(restauranteRepository)
    fun comboViewModel()           = ComboViewModel(comboRepository)
    fun pedidosRepartidorViewModel() = PedidosRepartidorViewModel(pedidoRepository)

    // ── CarritoViewModel SINGLETON ───────────────────────────────
    // Singleton porque CarritoScreen y CombosScreen comparten
    // el mismo estado del carrito durante la misma sesión.
    val carritoViewModel: CarritoViewModel by lazy {
        CarritoViewModel(pedidoRepository)
    }

    fun getSessionManager() = sessionManager
}