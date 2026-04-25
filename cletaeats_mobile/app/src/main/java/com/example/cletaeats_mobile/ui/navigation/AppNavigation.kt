package com.example.cletaeats_mobile.ui.navigation

import androidx.compose.runtime.Composable
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
            LoginScreen(
                viewModel  = AppContainer.authViewModel(),
                onLoginOk  = { rol ->
                    navController.navigate(
                        if (rol == "CLIENTE") AppRoutes.RESTAURANTES else AppRoutes.PEDIDOS_REPARTIDOR
                    ) { popUpTo(AppRoutes.LOGIN) { inclusive = true } }
                },
                onIrRegistro = { navController.navigate(AppRoutes.REGISTER) }
            )
        }

        composable(AppRoutes.REGISTER) {
            RegisterScreen(
                viewModel    = AppContainer.authViewModel(),
                onRegistroOk = { rol ->
                    navController.navigate(
                        if (rol == "CLIENTE") AppRoutes.RESTAURANTES else AppRoutes.PEDIDOS_REPARTIDOR
                    ) { popUpTo(AppRoutes.LOGIN) { inclusive = true } }
                },
                onVolver = { navController.popBackStack() }
            )
        }

        // ── Cliente ──────────────────────────────────────────────
        composable(AppRoutes.RESTAURANTES) {
            RestaurantesScreen(
                viewModel          = AppContainer.restauranteViewModel(),
                onRestauranteClick = { restauranteId ->
                    navController.navigate(AppRoutes.combosRuta(restauranteId))
                },
                onLogout = {
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
            CombosScreen(
                restauranteId    = restauranteId,
                comboViewModel   = AppContainer.comboViewModel(),
                carritoViewModel = AppContainer.carritoViewModel,
                onVerCarrito     = { navController.navigate(AppRoutes.CARRITO) },
                onVolver         = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.CARRITO) {
            CarritoScreen(
                carritoViewModel = AppContainer.carritoViewModel,
                onPedidoCreado   = {
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

        // ── Repartidor ───────────────────────────────────────────
        composable(AppRoutes.PEDIDOS_REPARTIDOR) {
            PedidosRepartidorScreen(
                viewModel = AppContainer.pedidosRepartidorViewModel(),
                onLogout  = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}