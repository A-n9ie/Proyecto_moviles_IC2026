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
import com.example.cletaeats_mobile.data.remote.IRepartidorPerfilApi
import com.example.cletaeats_mobile.data.remote.RetrofitClient.retrofit
import com.example.cletaeats_mobile.data.repository.PedidoCloudRepositoryImpl

import com.example.cletaeats_mobile.viewmodel.HistorialRepartidorViewModel
import com.example.cletaeats_mobile.viewmodel.PerfilRepartidorViewModel

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
            if (_carritoViewModel == null) _carritoViewModel = CarritoViewModel(buildPedidoRepository())
            return _carritoViewModel!!
        }

    val tarjetaViewModel: TarjetaViewModel by lazy { TarjetaViewModel(tarjetaRepo) }
    val repartidorPerfilApi: IRepartidorPerfilApi by lazy {
        retrofit.create(IRepartidorPerfilApi::class.java)
    }
    val syncManager: SyncManager by lazy { SyncManager(appContext, sessionManager) }
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
    private val pedidoHttpRepository by lazy {
        PedidoRepositoryImpl(RetrofitClient.create<IPedidoApi>(), sessionManager)
    }

    // Factory que retorna el repo correcto según el modo activo
    private fun buildPedidoRepository() =
        when (sessionManager.getDataMode()) {
            DataMode.CLOUD -> PedidoCloudRepositoryImpl(pedidoHttpRepository)
            else           -> pedidoHttpRepository   // LOCAL y API_REMOTA usan HTTP
            // Nota: LOCAL podría leer de SQLite, pero los pedidos locales se sincronizan
            // al inicio de sesión, así que el estado es reciente.
        }
    private val tarjetaRepo by lazy {
        TarjetaRepositoryImpl(RetrofitClient.create<ITarjetaApi>(), sessionManager) }
    private val authRepository by lazy {
        AuthRepositoryImpl(RetrofitClient.create<IAuthApi>(), RetrofitClient.create<ITarjetaApi>(), sessionManager, syncManager)
    }
    private val _perfilViewModel by lazy { PerfilViewModel(perfilRepo, sessionManager) }

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

    val pedidosClienteViewModel: PedidosClienteViewModel by lazy {
        PedidosClienteViewModel(
            buildPedidoRepository(),
            PedidoNotificador(notificationHelper, rol = "CLIENTE")
        )
    }

    fun pedidosRepartidorViewModel() = PedidosRepartidorViewModel(
        buildPedidoRepository(),
        PedidoNotificador(notificationHelper, rol = "REPARTIDOR")
    )

    fun historialRepartidorViewModel() = HistorialRepartidorViewModel(buildPedidoRepository())
    fun perfilViewModel() = _perfilViewModel

    // Factory del ViewModel
    fun perfilRepartidorViewModel() = PerfilRepartidorViewModel(
        api = repartidorPerfilApi,
        session = getSessionManager()
    )
    fun logout() {
        sessionManager.clearSession()
        // Resetear carritoViewModel para la próxima sesión
        _carritoViewModel = null
    }
}