package com.example.cletaeats_mobile.ui.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cletaeats_mobile.AppContainer
import com.example.cletaeats_mobile.domain.model.Restaurante
import com.example.cletaeats_mobile.ui.components.CletaTopBar
import com.example.cletaeats.ui.theme.*
import com.example.cletaeats_mobile.domain.model.Categoria
import com.example.cletaeats_mobile.viewmodel.RestauranteViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.Coil
import coil.compose.AsyncImage
import com.example.cletaeats_mobile.ui.utils.resolveImageUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantesScreen(
    viewModel:          RestauranteViewModel,
    onRestauranteClick: (Restaurante) -> Unit,
    onLogout:           () -> Unit,
    onVerPerfil:        () -> Unit,
    onMisPedidos:       () -> Unit = {},
    onVerMapa:          () -> Unit = {}
){
    val uiState  by viewModel.uiState.collectAsState()

    val perfilVM = remember { AppContainer.perfilViewModel() }
    val perfilState by perfilVM.uiState.collectAsState()

    val session  = AppContainer.getSessionManager()
    val authVM   = remember { AppContainer.authViewModel() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope       = rememberCoroutineScope()
    val context     = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.cargarRestaurantes()
        if (perfilState.imagenUrl.isBlank() && perfilState.nombre.isBlank()) {
            perfilVM.cargarPerfil()
        }
    }

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            DrawerContent(
                nombre = session.getNombre(),
                email = session.getEmail(),
                imagenUrl = resolveImageUrl(perfilState.imagenUrl) ?: "",

                onVerPerfil = {
                    scope.launch {
                        drawerState.close()
                        onVerPerfil()
                    }
                },

                onMisPedidos = {
                    scope.launch {
                        drawerState.close()
                        onMisPedidos()
                    }
                },

                onVerMapa = {
                    scope.launch {
                        drawerState.close()
                        onVerMapa()
                    }
                },

                onCerrarSesion = {
                    scope.launch {
                        drawerState.close()
                        Coil.imageLoader(context).memoryCache?.clear()
                        Coil.imageLoader(context).diskCache?.clear()
                        authVM.logout()
                        AppContainer.logout() // RESETEAR ViewModels en el AppContainer
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
                    onMenuClick = { scope.launch { drawerState.open() } },
                    modoActivo  = session.getDataMode()
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
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color    = CletaNaranja
                        )
                    }

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

                    else -> {
                        LazyColumn(
                            contentPadding  = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    "Hola, ${session.getNombre()} 👋",
                                    color = CletaBlanco, fontSize = 20.sp, fontWeight = FontWeight.Bold
                                )
                                Text("¿Qué vas a comer hoy?", color = CletaTextoSecundario, fontSize = 14.sp)
                                Spacer(Modifier.height(12.dp))

                                FiltroCategoriasRow(
                                    categorias           = uiState.categorias,
                                    seleccionadas        = uiState.categoriasSeleccionadas,
                                    onToggleCategoria    = { viewModel.toggleCategoria(it) },
                                    onLimpiar            = { viewModel.limpiarFiltros() }
                                )
                                Spacer(Modifier.height(8.dp))

                                // Contador de resultados
                                if (uiState.categoriasSeleccionadas.isNotEmpty()) {
                                    Text(
                                        "${uiState.restaurantesFiltrados.size} restaurante(s) encontrado(s)",
                                        color    = CletaTextoSecundario,
                                        fontSize = 12.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                }
                            }

                            items(uiState.restaurantesFiltrados) { restaurante ->
                                RestauranteCard(
                                    restaurante  = restaurante,
                                    onClickCard = { onRestauranteClick(restaurante) }
                                )
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
private fun RestauranteCard(restaurante: Restaurante, onClickCard: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickCard() },
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
            val primeraCat = restaurante.categorias.firstOrNull() ?: ""
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CletaNaranja.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (restaurante.imagenUrl.isNotBlank()) {
                    AsyncImage(
                        model = restaurante.imagenUrl,
                        contentDescription = restaurante.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = categoriaIcono(primeraCat),
                        contentDescription = primeraCat,
                        tint = CletaNaranja,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

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
                        text = restaurante.categorias.joinToString(" · ") { it.replaceFirstChar { c -> c.uppercase() } },
                        color = CletaTextoSecundario,
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

            IconButton(
                onClick = onClickCard,
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

// ── NavDrawer ─────────────────────────────────────────────────────
@Composable
private fun DrawerContent(
    nombre: String,
    email: String,
    imagenUrl: String = "",
    onVerPerfil: () -> Unit,
    onMisPedidos: () -> Unit,
    onVerMapa: () -> Unit,
    onCerrarSesion: () -> Unit
){
    ModalDrawerSheet(
        drawerContainerColor = CletaGrisMedio
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CletaNaranja)
                .padding(vertical = 32.dp, horizontal = 20.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CletaBlanco.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imagenUrl.isNotBlank()) {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(LocalContext.current)
                                .data(imagenUrl)
                                .diskCachePolicy(coil.request.CachePolicy.DISABLED)
                                .memoryCachePolicy(coil.request.CachePolicy.DISABLED)
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = CletaBlanco,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
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

        NavigationDrawerItem(
            icon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = CletaNaranja
                )
            },
            label = {
                Text(
                    "Mi perfil",
                    color = CletaBlanco
                )
            },
            selected = false,
            onClick = onVerPerfil,
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = CletaGrisMedio
            )
        )

        NavigationDrawerItem(
            icon     = { Icon(Icons.Default.Receipt, contentDescription = null, tint = CletaNaranja) },
            label    = { Text("Mis pedidos", color = CletaBlanco) },
            selected = false,
            onClick  = onMisPedidos,
            colors   = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = CletaGrisMedio
            )
        )

        NavigationDrawerItem(
            icon     = { Icon(Icons.Default.Map, contentDescription = null, tint = CletaNaranja) },
            label    = { Text("Ver mapa", color = CletaBlanco) },
            selected = false,
            onClick  = onVerMapa,
            colors   = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = CletaGrisMedio
            )
        )

        Spacer(Modifier.weight(1f))
        Divider(color = CletaGrisClaro)

        NavigationDrawerItem(
            icon     = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            label    = { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error) },
            selected = false,
            onClick  = onCerrarSesion,
            colors   = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = CletaGrisMedio
            )
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FiltroCategoriasRow(
    categorias:        List<Categoria>,
    seleccionadas:     Set<String>,
    onToggleCategoria: (String) -> Unit,
    onLimpiar:         () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding        = PaddingValues(horizontal = 0.dp)
    ) {
        // Chip "Todos"
        item {
            FilterChip(
                selected = seleccionadas.isEmpty(),
                onClick  = onLimpiar,
                label    = { Text("Todos") },
                leadingIcon = {
                    Icon(
                        Icons.Default.GridView,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor     = CletaNaranja,
                    selectedLabelColor         = CletaBlanco,
                    selectedLeadingIconColor   = CletaBlanco,
                    containerColor             = CletaGrisMedio,
                    labelColor                 = CletaTextoSecundario,
                    iconColor                  = CletaTextoSecundario
                )
            )
        }
        items(categorias) { cat ->
            val estaSeleccionada = cat.nombre in seleccionadas
            FilterChip(
                selected = estaSeleccionada,
                onClick  = { onToggleCategoria(cat.nombre) },
                label    = { Text(cat.nombre.replaceFirstChar { it.uppercase() }) },
                leadingIcon = {
                    Icon(
                        imageVector = if (estaSeleccionada) Icons.Default.Check
                                      else categoriaIcono(cat.nombre),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor     = CletaNaranja,
                    selectedLabelColor         = CletaBlanco,
                    selectedLeadingIconColor   = CletaBlanco,
                    containerColor             = CletaGrisMedio,
                    labelColor                 = CletaTextoSecundario,
                    iconColor                  = CletaNaranja
                )
            )
        }
    }
}

// ── Helper: ícono según tipo de comida ───────────────────────────
fun categoriaIcono(tipo: String): ImageVector {
    return when (tipo.lowercase().trim()) {
        "rápida", "rapida", "comida rápida" -> Icons.Default.Fastfood
        "china", "asiática", "asiatica"     -> Icons.Default.Restaurant
        "saludable", "vegana", "vegan"      -> Icons.Default.Eco
        "italiana", "pizza"                 -> Icons.Default.LocalPizza
        "mexicana", "tacos"                 -> Icons.Default.LunchDining
        "mariscos", "pescados"              -> Icons.Default.RestaurantMenu
        "japonesa", "sushi", "ramen"        -> Icons.Default.RamenDining
        "postres", "dulces", "helados"      -> Icons.Default.Cake
        "bebidas", "jugos", "café"          -> Icons.Default.LocalCafe
        "desayunos", "desayuno"             -> Icons.Default.FreeBreakfast
        "carnes", "parrilla", "bbq"         -> Icons.Default.OutdoorGrill
        else                                -> Icons.Default.Restaurant
    }
}
