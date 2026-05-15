package com.example.cletaeats_mobile

import android.content.Context
import com.example.cletaeats_mobile.data.local.SessionManager

import com.example.cletaeats_mobile.data.repository.AuthRepositoryImpl
import com.example.cletaeats_mobile.data.repository.ComboRepositoryImpl
import com.example.cletaeats_mobile.data.repository.PedidoRepositoryImpl
import com.example.cletaeats_mobile.data.repository.RestauranteRepositoryImpl
import com.example.cletaeats_mobile.viewmodel.AuthViewModel
import com.example.cletaeats_mobile.viewmodel.CarritoViewModel
import com.example.cletaeats_mobile.viewmodel.ComboViewModel
import com.example.cletaeats_mobile.viewmodel.PedidosRepartidorViewModel
import com.example.cletaeats_mobile.viewmodel.RestauranteViewModel

import com.example.cletaeats_mobile.data.remote.RetrofitClient
import com.example.cletaeats_mobile.data.remote.IAuthApi
import com.example.cletaeats_mobile.data.remote.IRestauranteApi
import com.example.cletaeats_mobile.data.remote.IComboApi
import com.example.cletaeats_mobile.data.remote.IPedidoApi
import com.example.cletaeats_mobile.viewmodel.PedidosClienteViewModel

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

    // ── CarritoViewModel SINGLETON ───────────────────────────────
    // Singleton porque CarritoScreen y CombosScreen comparten
    // el mismo estado del carrito durante la misma sesión.
    private var _carritoViewModel: CarritoViewModel? = null
    val carritoViewModel: CarritoViewModel
        get() {
            if (_carritoViewModel == null) _carritoViewModel = CarritoViewModel(pedidoRepository)
            return _carritoViewModel!!
        }
    // ── Repositorios (lazy) ──────────────────────────────────────
    private val authRepository by lazy {
    AuthRepositoryImpl(RetrofitClient.create<IAuthApi>(), sessionManager)
    }
    private val restauranteRepository by lazy {
        RestauranteRepositoryImpl(RetrofitClient.create<IRestauranteApi>(), sessionManager)
    }
    private val comboRepository by lazy {
        ComboRepositoryImpl(RetrofitClient.create<IComboApi>(), sessionManager)
    }
    private val pedidoRepository by lazy {
        PedidoRepositoryImpl(RetrofitClient.create<IPedidoApi>(), sessionManager)
    }

    fun init(context: Context) {
        sessionManager = SessionManager(context.applicationContext)
    }
    fun getSessionManager() = sessionManager
    // ── ViewModels factories (nueva instancia cada vez) ──────────
    fun authViewModel()            = AuthViewModel(authRepository)
    fun restauranteViewModel()     = RestauranteViewModel(restauranteRepository)
    fun comboViewModel()           = ComboViewModel(comboRepository)
    fun pedidosClienteViewModel() = PedidosClienteViewModel(pedidoRepository)
    fun pedidosRepartidorViewModel() = PedidosRepartidorViewModel(pedidoRepository)
    fun logout() {
        sessionManager.clearSession()
        // Resetear carritoViewModel para la próxima sesión
        _carritoViewModel = null
    }
}