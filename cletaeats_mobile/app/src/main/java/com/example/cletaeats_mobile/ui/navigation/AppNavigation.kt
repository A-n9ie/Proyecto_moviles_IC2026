package com.example.cletaeats_mobile.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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

import com.example.cletaeats_mobile.ui.cliente.RastreoRepartidorScreen

import com.example.cletaeats_mobile.ui.cliente.PerfilScreen
import com.example.cletaeats_mobile.ui.repartidor.MapaSeguimientoScreen

import com.example.cletaeats_mobile.ui.repartidor.HistorialRepartidorScreen
import com.example.cletaeats_mobile.ui.repartidor.PerfilRepartidorScreen

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
                    val destino = if (rol == "CLIENTE") AppRoutes.RESTAURANTES else AppRoutes.PEDIDOS_REPARTIDOR
                    navController.navigate(destino) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
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
                    val destino = if (rol == "CLIENTE") AppRoutes.RESTAURANTES else AppRoutes.PEDIDOS_REPARTIDOR
                    navController.navigate(destino) {
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
                onRestauranteClick = { restaurante ->
                    navController.navigate(
                        AppRoutes.combosRuta(restaurante.id, restaurante.latitud, restaurante.longitud, restaurante.nombre)
                    )
                },
                onMisPedidos = { navController.navigate(AppRoutes.MIS_PEDIDOS) },
                onVerMapa    = { navController.navigate(AppRoutes.MAPA_RESTAURANTES) },
                onVerPerfil  = { navController.navigate(AppRoutes.PERFIL) },
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
            arguments = listOf(
                navArgument("restauranteId") { type = NavType.IntType },
                navArgument("lat")           { type = NavType.StringType },  // Double como String
                navArgument("lng")           { type = NavType.StringType },
                navArgument("nombre")        { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val restauranteId = backStackEntry.arguments?.getInt("restauranteId") ?: return@composable
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0
            val comboViewModel = remember(backStackEntry) { AppContainer.comboViewModel() }
            val nombre = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("nombre") ?: "", "UTF-8"
            )

            CombosScreen(
                restauranteId    = restauranteId,
                restauranteLat   = lat,
                restauranteLng   = lng,
                restauranteNombre = nombre,
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

        composable(AppRoutes.MIS_PEDIDOS) { backStackEntry ->
            val viewModel = AppContainer.pedidosClienteViewModel
            MisPedidosScreen(
                viewModel  = viewModel,
                onVolver   = { navController.popBackStack() },
                onRastrear = { pedidoId -> navController.navigate(AppRoutes.rastreoRuta(pedidoId)) },
                carritoVm       = AppContainer.carritoViewModel,
                onIrARestaurante = { _, _ ->
                    navController.navigate(AppRoutes.CARRITO)
                }
            )
        }

        composable(
            route = AppRoutes.RASTREO_REPARTIDOR,
            arguments = listOf(navArgument("pedidoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val pedidoId = backStackEntry.arguments?.getInt("pedidoId") ?: return@composable
            RastreoRepartidorScreen(
                pedidoId = pedidoId,
                onVolver = { navController.popBackStack() }
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
                    val restaurante = uiState.restaurantes.find { it.id == restauranteId }
                    navController.navigate(
                        AppRoutes.combosRuta(
                            restauranteId,
                            restaurante?.latitud,
                            restaurante?.longitud,
                            restaurante?.nombre ?: ""
                        )
                    )
                },
                onVolver = { navController.popBackStack() }
            )
        }

        // ── Repartidor ───────────────────────────────────────────
        composable(AppRoutes.PERFIL_REPARTIDOR) { backStackEntry ->
            val viewModel = remember(backStackEntry) { AppContainer.perfilRepartidorViewModel() }
            PerfilRepartidorScreen(
                viewModel = viewModel,
                onVolver  = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.PEDIDOS_REPARTIDOR) {
            backStackEntry ->
            val viewModel = remember(backStackEntry) { AppContainer.pedidosRepartidorViewModel() }
            PedidosRepartidorScreen(
                viewModel = viewModel,
                onLogout  = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onVerMapa      = { pedidoId -> navController.navigate(AppRoutes.mapaSeguimientoRuta(pedidoId)) },
                onVerPerfil    = { navController.navigate(AppRoutes.PERFIL_REPARTIDOR) },
                onVerHistorial = { navController.navigate(AppRoutes.HISTORIAL_REPARTIDOR) }
            )
        }

        composable(AppRoutes.HISTORIAL_REPARTIDOR) { backStackEntry ->
            val viewModel = remember(backStackEntry) { AppContainer.historialRepartidorViewModel() }
            HistorialRepartidorScreen(
                viewModel = viewModel,
                onBack    = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.MAPA_SEGUIMIENTO) { backStackEntry ->
            val pedidoId = backStackEntry.arguments?.getString("pedidoId")?.toIntOrNull() ?: return@composable

            // Buscar el pedido en el viewmodel del repartidor
            val repVM = remember(backStackEntry) { AppContainer.pedidosRepartidorViewModel() }
            val state by repVM.uiState.collectAsState()

            // Esta instancia es nueva, así que hay que cargar los pedidos
            LaunchedEffect(Unit) {
                repVM.cargarPedidos()
            }

            val pedido = state.pedidos.find { it.id == pedidoId }

            if (pedido == null) {
                // Mientras cargan los pedidos, mostrar spinner en vez de pantalla vacía
                Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = com.example.cletaeats.ui.theme.CletaNaranja
                    )
                }
                return@composable
            }

            MapaSeguimientoScreen(
                pedido      = pedido,
                onVolver    = { navController.popBackStack() },
                onEntregado = {
                    repVM.marcarEntregado(pedidoId)
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.PERFIL) { backStackEntry ->
            val perfilVM  = remember(backStackEntry) { AppContainer.perfilViewModel() }
            PerfilScreen(
                perfilViewModel  = perfilVM,
                tarjetaViewModel = AppContainer.tarjetaViewModel,
                onVolver         = { navController.popBackStack() }
            )
        }
    }
}