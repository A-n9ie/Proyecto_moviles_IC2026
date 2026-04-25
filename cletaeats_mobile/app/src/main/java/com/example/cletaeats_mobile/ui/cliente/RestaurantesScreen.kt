package com.example.cletaeats_mobile.ui.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cletaeats_mobile.AppContainer
import com.example.cletaeats_mobile.domain.model.Restaurante
import com.example.cletaeats_mobile.ui.components.CletaTopBar
import com.example.cletaeats.ui.theme.*
import androidx.compose.material.icons.outlined.Spa
import com.example.cletaeats_mobile.viewmodel.AuthViewModel
import com.example.cletaeats_mobile.viewmodel.RestauranteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantesScreen(
    viewModel: RestauranteViewModel,
    onLogout:  () -> Unit
) {
    val uiState  by viewModel.uiState.collectAsState()
    val session  = AppContainer.getSessionManager()
    val authVM   = remember { AppContainer.authViewModel() }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    // Cargar restaurantes al entrar a la pantalla
    LaunchedEffect(Unit) { viewModel.cargarRestaurantes() }

    // ── NavDrawer ─────────────────────────────────────────────────
    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            DrawerContent(
                nombre    = session.getNombre(),
                email     = session.getEmail(),
                onCerrarSesion = {
                    scope.launch {
                        drawerState.close()
                        authVM.logout()
                        onLogout()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CletaTopBar(
                    titulo      = "Restaurantes",
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            containerColor = CletaGrisOscuro
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    // ── Cargando ──────────────────────────────────
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color    = CletaNaranja
                        )
                    }

                    // ── Error ─────────────────────────────────────
                    uiState.errorMsg != null -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.WifiOff,
                                contentDescription = null,
                                tint     = CletaNaranja,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                uiState.errorMsg!!,
                                color     = CletaTextoSecundario,
                                fontSize  = 15.sp
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick  = { viewModel.cargarRestaurantes() },
                                colors   = ButtonDefaults.buttonColors(containerColor = CletaNaranja)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Reintentar")
                            }
                        }
                    }

                    // ── Lista de restaurantes ─────────────────────
                    else -> {
                        LazyColumn(
                            contentPadding  = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    "Hola, ${session.getNombre()} 👋",
                                    color      = CletaBlanco,
                                    fontSize   = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "¿Qué vas a comer hoy?",
                                    color    = CletaTextoSecundario,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(8.dp))
                            }

                            items(uiState.restaurantes) { restaurante ->
                                RestauranteCard(restaurante = restaurante)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Card individual de restaurante ────────────────────────────────
@Composable
private fun RestauranteCard(restaurante: Restaurante) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: navegar a combos del restaurante */ },
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CletaGrisMedio),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Imagen placeholder (el profe pidió imágenes) ──────
            // Cuando se tengan URLs reales, reemplazar este Box
            // con AsyncImage de Coil: AsyncImage(model=url, ...)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CletaNaranja.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = tipoComidaIcono(restaurante.tipComida),
                    contentDescription = restaurante.tipComida,
                    tint               = CletaNaranja,
                    modifier           = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // ── Info ──────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = restaurante.nombre,
                    color      = CletaBlanco,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        tint     = CletaNaranja,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text     = restaurante.tipComida.replaceFirstChar { it.uppercase() },
                        color    = CletaTextoSecundario,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint     = CletaTextoSecundario,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text     = restaurante.direccion,
                        color    = CletaTextoSecundario,
                        fontSize = 12.sp
                    )
                }
            }

            // ── Botón de acción (ImageButton con ícono) ───────────
            IconButton(
                onClick = { /* TODO: ir a combos */ },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CletaNaranja)
                    .size(40.dp)
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Ver menú de ${restaurante.nombre}",
                    tint     = CletaBlanco,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── Contenido del NavDrawer ───────────────────────────────────────
@Composable
private fun DrawerContent(
    nombre:        String,
    email:         String,
    onCerrarSesion: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = CletaGrisMedio
    ) {
        // Encabezado del drawer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CletaNaranja)
                .padding(vertical = 32.dp, horizontal = 20.dp)
        ) {
            Column {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint     = CletaBlanco,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(nombre, color = CletaBlanco, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(email,  color = CletaBlanco.copy(alpha = 0.8f), fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CletaBlanco.copy(alpha = 0.2f)
                ) {
                    Text(
                        "Cliente",
                        color    = CletaBlanco,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Ítem: Mis pedidos (placeholder para fase siguiente)
        NavigationDrawerItem(
            icon     = { Icon(Icons.Default.Receipt, contentDescription = null, tint = CletaNaranja) },
            label    = { Text("Mis pedidos", color = CletaBlanco) },
            selected = false,
            onClick  = { /* TODO: navegación a historial de pedidos */ },
            colors   = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = CletaGrisMedio
            )
        )

        // Ítem: Cerrar sesión
        Spacer(Modifier.weight(1f))
        Divider(color = CletaGrisClaro)
        NavigationDrawerItem(
            icon     = {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = CletaError)
            },
            label    = { Text("Cerrar sesión", color = CletaError) },
            selected = false,
            onClick  = onCerrarSesion,
            colors   = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = CletaGrisMedio
            )
        )
        Spacer(Modifier.height(16.dp))
    }
}

// ── Helper: ícono según tipo de comida ───────────────────────────
@Composable
private fun tipoComidaIcono(tipo: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (tipo.lowercase()) {
        "rápida", "rapida" -> Icons.Default.Fastfood
        "china"            -> Icons.Default.RiceBowl
        "saludable"        -> Icons.Outlined.Spa
        "italiana"         -> Icons.Default.LocalPizza
        "mexicana"         -> Icons.Default.TakeoutDining
        "mariscos"         -> Icons.Default.SetMeal
        "japonesa"         -> Icons.Default.RiceBowl
        else               -> Icons.Default.Restaurant
    }
}