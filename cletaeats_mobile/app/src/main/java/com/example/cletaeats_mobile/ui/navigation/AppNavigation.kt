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
                onLoginOk  = { rol ->
                    navController.navigate(
                        if (rol == "CLIENTE") AppRoutes.RESTAURANTES else AppRoutes.PEDIDOS_REPARTIDOR
                    ) { popUpTo(AppRoutes.LOGIN) { inclusive = true } }
                },
                onIrRegistro = { navController.navigate(AppRoutes.REGISTER) }
            )
        }

        composable(AppRoutes.REGISTER) {
            backStackEntry ->
            val viewModel = remember(backStackEntry) { AppContainer.authViewModel() }
            RegisterScreen(
                viewModel = viewModel,
                onRegistroOk = { rol ->
                    navController.navigate(
                        if (rol == "CLIENTE") AppRoutes.RESTAURANTES else AppRoutes.PEDIDOS_REPARTIDOR
                    ) { popUpTo(AppRoutes.LOGIN) { inclusive = true } }
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
                onMisPedidos       = { navController.navigate(AppRoutes.MIS_PEDIDOS) },
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