package com.example.cletaeats_mobile.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cletaeats_mobile.AppContainer
import com.example.cletaeats_mobile.ui.auth.LoginScreen
import com.example.cletaeats_mobile.ui.auth.RegisterScreen
import com.example.cletaeats_mobile.ui.cliente.RestaurantesScreen
import com.example.cletaeats_mobile.ui.repartidor.PedidosRepartidorScreen

/**
 * Grafo de navegación de la app.
 * startDestination se decide en MainActivity según la sesión activa.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {

        // ── Auth ────────────────────────────────────────────────
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                viewModel  = AppContainer.authViewModel(),
                onLoginOk  = { rol ->
                    val destino = when (rol) {
                        "CLIENTE"    -> AppRoutes.RESTAURANTES
                        "REPARTIDOR" -> AppRoutes.PEDIDOS_REPARTIDOR
                        else         -> AppRoutes.LOGIN
                    }
                    navController.navigate(destino) {
                        // Limpia el back stack para que Back no regrese al Login
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onIrRegistro = {
                    navController.navigate(AppRoutes.REGISTER)
                }
            )
        }

        composable(AppRoutes.REGISTER) {
            RegisterScreen(
                viewModel    = AppContainer.authViewModel(),
                onRegistroOk = { rol ->
                    val destino = when (rol) {
                        "CLIENTE"    -> AppRoutes.RESTAURANTES
                        "REPARTIDOR" -> AppRoutes.PEDIDOS_REPARTIDOR
                        else         -> AppRoutes.LOGIN
                    }
                    navController.navigate(destino) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }

        // ── Cliente ─────────────────────────────────────────────
        composable(AppRoutes.RESTAURANTES) {
            RestaurantesScreen(
                viewModel = AppContainer.restauranteViewModel(),
                onLogout  = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Repartidor ──────────────────────────────────────────
        composable(AppRoutes.PEDIDOS_REPARTIDOR) {
            PedidosRepartidorScreen(
                onLogout = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}