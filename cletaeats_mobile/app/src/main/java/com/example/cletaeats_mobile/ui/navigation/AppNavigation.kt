package com.example.cletaeats_mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.cletaeats_mobile.AppContainer
import com.example.cletaeats_mobile.ui.auth.LoginScreen
import com.example.cletaeats_mobile.ui.auth.RegisterScreen
import com.example.cletaeats_mobile.ui.cliente.CarritoScreen
import com.example.cletaeats_mobile.ui.cliente.CombosScreen
import com.example.cletaeats_mobile.ui.cliente.FacturaScreen
import com.example.cletaeats_mobile.ui.cliente.MisPedidosScreen
import com.example.cletaeats_mobile.ui.cliente.RestaurantesScreen
import com.example.cletaeats_mobile.ui.repartidor.PedidosRepartidorScreen
import com.example.cletaeats_mobile.ui.cliente.MapaRestaurantesScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect

import com.example.cletaeats_mobile.ui.auth.SeleccionModoScreen
import com.example.cletaeats_mobile.data.local.DataMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
@Composable
fun AppNavigation(
    navController:    NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // ── Auth ─────────────────────────────────────────────────
        composable(AppRoutes.LOGIN) {
            backStackEntry ->
            val viewModel = remember(backStackEntry) { AppContainer.authViewModel() }
            LoginScreen(
                viewModel = viewModel,
                onLoginOk = { rol ->
                    navController.navigate(AppRoutes.SELECCION_MODO) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onIrRegistro = { navController.navigate(AppRoutes.REGISTER) }
            )
        }

        composable(AppRoutes.SELECCION_MODO) {
            val session = AppContainer.getSessionManager()
            val nombre  = session.getNombre()

            SeleccionModoScreen(
                nombreUsuario = nombre,
                onModoSeleccionado = { modo ->
                    session.saveDataMode(modo)

                    // Si el modo es CLOUD, sincronizar en background
                    if (modo == DataMode.CLOUD) {
                        CoroutineScope(Dispatchers.IO).launch {
                            AppContainer.syncManager.sincronizarDesdeCloud()
                        }
                    }

                    val rol = session.getRol()
                    navController.navigate(
                        if (rol == "CLIENTE") AppRoutes.RESTAURANTES else AppRoutes.PEDIDOS_REPARTIDOR
                    ) { popUpTo(AppRoutes.SELECCION_MODO) { inclusive = true } }
                }
            )
        }

        composable(AppRoutes.REGISTER) {
            backStackEntry ->
            val viewModel = remember(backStackEntry) { AppContainer.authViewModel() }
            RegisterScreen(
                viewModel = viewModel,
                onRegistroOk = { _ ->
                    navController.navigate(AppRoutes.SELECCION_MODO) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }

        // ── Cliente ──────────────────────────────────────────────
        composable(AppRoutes.RESTAURANTES) { backStackEntry ->
            val viewModel = remember(backStackEntry) { AppContainer.restauranteViewModel() }
            RestaurantesScreen(
                viewModel = viewModel,
                onRestauranteClick = { restauranteId ->
                    navController.navigate(AppRoutes.combosRuta(restauranteId))
                },
                onMisPedidos = { navController.navigate(AppRoutes.MIS_PEDIDOS) },
                onVerMapa    = { navController.navigate(AppRoutes.MAPA_RESTAURANTES) },
                onLogout = {
                    AppContainer.logout()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route     = AppRoutes.COMBOS,
            arguments = listOf(navArgument("restauranteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val restauranteId = backStackEntry.arguments?.getInt("restauranteId") ?: return@composable
            val comboViewModel = remember(backStackEntry) { AppContainer.comboViewModel() }
            CombosScreen(
                restauranteId    = restauranteId,
                comboViewModel   = comboViewModel,
                carritoViewModel = AppContainer.carritoViewModel,
                onVerCarrito     = { navController.navigate(AppRoutes.CARRITO) },
                onVolver         = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.CARRITO) {
            CarritoScreen(
                carritoViewModel = AppContainer.carritoViewModel,
                tarjetaViewModel = AppContainer.tarjetaViewModel,
                onPedidoCreado = {
                    navController.navigate(AppRoutes.FACTURA) {
                        popUpTo(AppRoutes.RESTAURANTES)
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.FACTURA) {
            FacturaScreen(
                carritoViewModel = AppContainer.carritoViewModel,
                onVolver = {
                    AppContainer.carritoViewModel.limpiar()
                    navController.navigate(AppRoutes.RESTAURANTES) {
                        popUpTo(AppRoutes.RESTAURANTES) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.MIS_PEDIDOS) {
            backStackEntry ->
            val viewModel = remember(backStackEntry) { AppContainer.pedidosClienteViewModel() }
            MisPedidosScreen(
                viewModel = viewModel,
                onVolver  = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.MAPA_RESTAURANTES) { backStackEntry ->
            val viewModel = remember(backStackEntry) { AppContainer.restauranteViewModel() }
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                if (uiState.restaurantes.isEmpty()) {
                    viewModel.cargarRestaurantes()
                }
            }

            MapaRestaurantesScreen(
                restaurantes       = uiState.restaurantesFiltrados,
                onRestauranteClick = { restauranteId ->
                    navController.navigate(AppRoutes.combosRuta(restauranteId))
                },
                onVolver = { navController.popBackStack() }
            )
        }

        // ── Repartidor ───────────────────────────────────────────
        composable(AppRoutes.PEDIDOS_REPARTIDOR) {
            backStackEntry ->
            val viewModel = remember(backStackEntry) { AppContainer.pedidosRepartidorViewModel() }
            PedidosRepartidorScreen(
                viewModel = viewModel,
                onLogout  = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}