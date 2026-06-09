package com.example.cletaeats_mobile

import android.content.Context
import com.example.cletaeats_mobile.data.local.SessionManager

import com.example.cletaeats_mobile.data.repository.AuthRepositoryImpl
import com.example.cletaeats_mobile.data.repository.ComboRepositoryImpl
import com.example.cletaeats_mobile.data.repository.PedidoRepositoryImpl
import com.example.cletaeats_mobile.data.repository.RestauranteRepositoryImpl
import com.example.cletaeats_mobile.data.repository.TarjetaRepositoryImpl
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
import com.example.cletaeats_mobile.data.remote.ITarjetaApi
import com.example.cletaeats_mobile.viewmodel.PedidosClienteViewModel
import com.example.cletaeats_mobile.viewmodel.TarjetaViewModel

import com.example.cletaeats_mobile.data.local.DataMode
import com.example.cletaeats_mobile.data.local.db.CletaEatsDatabase
import com.example.cletaeats_mobile.data.repository.RestauranteLocalRepositoryImpl
import com.example.cletaeats_mobile.data.repository.RestauranteCloudRepositoryImpl
import com.example.cletaeats_mobile.data.sync.SyncManager
import com.example.cletaeats_mobile.domain.interfaces.IRestauranteRepository

import com.example.cletaeats_mobile.data.remote.IPerfilApi
import com.example.cletaeats_mobile.data.repository.ComboCloudRepositoryImpl
import com.example.cletaeats_mobile.data.repository.ComboLocalRepositoryImpl
import com.example.cletaeats_mobile.data.repository.PerfilRepositoryImpl
import com.example.cletaeats_mobile.domain.interfaces.IComboRepository
import com.example.cletaeats_mobile.viewmodel.PerfilViewModel

import com.example.cletaeats_mobile.data.notifications.NotificationHelper
import com.example.cletaeats_mobile.data.notifications.PedidoNotificador

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
    private lateinit var appContext: android.content.Context
    // ── CarritoViewModel SINGLETON ───────────────────────────────
    // Singleton porque CarritoScreen y CombosScreen comparten
    // el mismo estado del carrito durante la misma sesión.
    private var _carritoViewModel: CarritoViewModel? = null
    private val perfilRepo by lazy {
        PerfilRepositoryImpl(RetrofitClient.create<IPerfilApi>(), sessionManager)
    }
    val carritoViewModel: CarritoViewModel
        get() {
            if (_carritoViewModel == null) _carritoViewModel = CarritoViewModel(pedidoRepository)
            return _carritoViewModel!!
        }
    val tarjetaViewModel: TarjetaViewModel by lazy { TarjetaViewModel(tarjetaRepo) }
    val syncManager: SyncManager by lazy { SyncManager(appContext) }
    // Helper de notificaciones (singleton: un solo canal para toda la app)
    private val notificationHelper by lazy { NotificationHelper(appContext) }
    // ── Repositorios (lazy) ──────────────────────────────────────
    private fun buildRestauranteRepository(): IRestauranteRepository =
        when (sessionManager.getDataMode()) {
            DataMode.API_REMOTA   -> RestauranteRepositoryImpl(
                RetrofitClient.create<IRestauranteApi>(), sessionManager)
            DataMode.LOCAL_SQLITE -> RestauranteLocalRepositoryImpl(
                CletaEatsDatabase.getInstance(appContext))
            DataMode.CLOUD        -> RestauranteCloudRepositoryImpl()
        }
    private fun buildComboRepository(): IComboRepository =
        when (sessionManager.getDataMode()) {
            DataMode.API_REMOTA   -> ComboRepositoryImpl(RetrofitClient.create<IComboApi>(), sessionManager)
            DataMode.LOCAL_SQLITE -> ComboLocalRepositoryImpl(
                CletaEatsDatabase.getInstance(
                    appContext
                ).comboDao()
            )
            DataMode.CLOUD        -> ComboCloudRepositoryImpl()
        }
    fun comboViewModel() = ComboViewModel(buildComboRepository())
    private val pedidoRepository by lazy {
        PedidoRepositoryImpl(RetrofitClient.create<IPedidoApi>(), sessionManager)
    }
    private val tarjetaRepo by lazy {
        TarjetaRepositoryImpl(RetrofitClient.create<ITarjetaApi>(), sessionManager) }
    private val authRepository by lazy {
        AuthRepositoryImpl(RetrofitClient.create<IAuthApi>(), RetrofitClient.create<ITarjetaApi>(), sessionManager, syncManager)
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        sessionManager = SessionManager(context.applicationContext)
    }
    fun getSessionManager() = sessionManager
    // ── ViewModels factories (nueva instancia cada vez) ──────────
    fun authViewModel()            = AuthViewModel(authRepository)
    fun tarjetaViewModel()         = TarjetaViewModel(tarjetaRepo)
    fun restauranteViewModel() = RestauranteViewModel(
        buildRestauranteRepository(),
        sessionManager.getDataMode()
    )

    fun pedidosClienteViewModel() = PedidosClienteViewModel(
        pedidoRepository,
        PedidoNotificador(notificationHelper, rol = "CLIENTE")
    )
    fun pedidosRepartidorViewModel() = PedidosRepartidorViewModel(
        pedidoRepository,
        PedidoNotificador(notificationHelper, rol = "REPARTIDOR")
    )

    fun perfilViewModel() = PerfilViewModel(perfilRepo, sessionManager)
    fun logout() {
        sessionManager.clearSession()
        // Resetear carritoViewModel para la próxima sesión
        _carritoViewModel = null
    }
}