package com.example.cletaeats_mobile.ui.repartidor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cletaeats_mobile.AppContainer
import com.example.cletaeats_mobile.domain.model.Pedido
import com.example.cletaeats.ui.theme.*
import com.example.cletaeats_mobile.viewmodel.PedidosRepartidorViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.cletaeats_mobile.data.remote.RepartidorLocationService
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosRepartidorScreen(
    viewModel: PedidosRepartidorViewModel,
    onLogout:  () -> Unit,
    onVerMapa: (Int) -> Unit,
    onVerPerfil: () -> Unit,
    onVerHistorial: () -> Unit
) {
    val uiState     by viewModel.uiState.collectAsState()
    val session     = AppContainer.getSessionManager()
    val authVM      = remember { AppContainer.authViewModel() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    val pedidosFiltrados by viewModel.pedidosFiltrados.collectAsState()

    val context = LocalContext.current
    val locationService = remember { RepartidorLocationService(context) }

    // Mensajes de éxito/error como Snackbar
    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(uiState.mensajeOk, uiState.errorMsg) {
        uiState.mensajeOk?.let { snackbarHost.showSnackbar(it); viewModel.clearMensajes() }
        uiState.errorMsg?.let { snackbarHost.showSnackbar(it); viewModel.clearMensajes() }
    }

    LaunchedEffect(uiState.pedidos) {
        while (isActive && uiState.pedidos.isNotEmpty()) {
            uiState.pedidos
                .filter { it.estado == 2 }  // solo los EN_CAMINO
                .forEach { pedido -> locationService.publicarUbicacion(pedido.id) }
            delay(10_000L)
        }
    }

    // Cargar pedidos al entrar
    LaunchedEffect(Unit) { viewModel.cargarPedidos() }

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            DrawerRepartidor(
                nombre         = session.getNombre(),
                email          = session.getEmail(),
                onPerfil       = {
                    scope.launch { drawerState.close() }
                    onVerPerfil()
                },
                onHistorial    = {
                    scope.launch { drawerState.close() }
                    onVerHistorial()
                },
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
            snackbarHost = { SnackbarHost(snackbarHost) },
            topBar = {
                TopAppBar(
                    title = { Text("Mis pedidos", color = CletaBlanco, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = CletaBlanco)
                        }
                    },
                    actions = {
                        // ImageButton para refrescar
                        IconButton(onClick = { viewModel.cargarPedidos() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = CletaBlanco)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CletaGrisMedio)
                )
            },
            containerColor = CletaGrisOscuro
        ) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when {
                    // ── Cargando ───────────────────────────────────
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color    = CletaNaranja
                        )
                    }

                    // ── Sin pedidos ────────────────────────────────
                    uiState.pedidos.isEmpty() && !uiState.isLoading -> {
                        Column(
                            modifier            = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Inbox, contentDescription = null,
                                tint = CletaTextoSecundario, modifier = Modifier.size(80.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Sin pedidos asignados", color = CletaBlanco, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Cuando te asignen un pedido aparecerá aquí.", color = CletaTextoSecundario, fontSize = 14.sp)
                        }
                    }

                    // ── Lista de pedidos ───────────────────────────
                    else -> {
                        LazyColumn(
                            contentPadding      = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // ── Chips de filtro ──────────────────────────────────
                            item {
                                val filtros = listOf(
                                    null to "Todos",
                                    0    to "Creado",
                                    1    to "Preparando",
                                    2    to "En camino",
                                    3    to "Entregado"
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    filtros.forEach { (estado, label) ->
                                        FilterChip(
                                            selected = uiState.filtroEstado == estado,
                                            onClick  = { viewModel.setFiltro(estado) },
                                            label    = { Text(label, fontSize = 12.sp) },
                                            colors   = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = CletaNaranja,
                                                selectedLabelColor     = CletaBlanco,
                                                containerColor         = CletaGrisMedio,
                                                labelColor             = CletaTextoSecundario
                                            )
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                            item {
                                Text(
                                    "${pedidosFiltrados.size} pedido(s) activo(s)",
                                    color = CletaBlanco, fontWeight = FontWeight.Bold, fontSize = 16.sp
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                            items(pedidosFiltrados) { pedido ->
                                PedidoRepartidorCard(
                                    pedido            = pedido,
                                    onAceptar         = { viewModel.marcarPreparando(pedido.id) },
                                    onSalirEntregar   = { viewModel.marcarEnCamino(pedido.id) },
                                    onMarcarEntregado = {
                                        viewModel.marcarEntregado(pedido.id)
                                        locationService.limpiarUbicacion(pedido.id)
                                    },
                                    onVerMapa = { onVerMapa(pedido.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Card de pedido para el repartidor ────────────────────────────
@Composable
private fun PedidoRepartidorCard(
    pedido:            Pedido,
    onAceptar:         () -> Unit,
    onSalirEntregar:   () -> Unit,
    onMarcarEntregado: () -> Unit,
    onVerMapa:         () -> Unit
) {
    val estadoColor = when (pedido.estado) {
        0 -> CletaTextoSecundario   // CREADO
        1 -> CletaNaranjaClaro      // EN_PREPARACION
        2 -> CletaExito             // EN_CAMINO
        else -> CletaTextoSecundario
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CletaGrisMedio),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Encabezado de la card ────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "Pedido #${pedido.id}",
                    color      = CletaBlanco,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
                // Badge de estado (chip con color dinámico)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = estadoColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        pedido.estadoTexto.replace("_", " "),
                        color     = estadoColor,
                        fontSize  = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier  = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Divider(color = CletaGrisClaro)
            Spacer(Modifier.height(10.dp))

            // ── Info del pedido ──────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = CletaNaranja, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(pedido.restauranteNombre, color = CletaBlanco, fontSize = 14.sp)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = CletaTextoSecundario, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(pedido.clienteNombre, color = CletaTextoSecundario, fontSize = 13.sp)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Route, contentDescription = null, tint = CletaTextoSecundario, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("${pedido.distanciaKm} km · ${pedido.itemsCount} combo(s)", color = CletaTextoSecundario, fontSize = 13.sp)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = CletaTextoSecundario, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(pedido.fechaCreacion, color = CletaTextoSecundario, fontSize = 12.sp)
            }

            Spacer(Modifier.height(14.dp))

            // Botón de acción según el estado actual del pedido.
            // El flujo es estricto: CREADO → EN_PREPARACION → EN_CAMINO → ENTREGADO
            when (pedido.estado) {
                0 -> {
                    // CREADO: el repartidor acepta el pedido
                    Button(
                        onClick  = onAceptar,
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = CletaNaranja)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Aceptar pedido", fontWeight = FontWeight.SemiBold)
                    }
                }
                1 -> {
                    // EN_PREPARACION: el repartidor sale a entregar
                    Button(
                        onClick  = onSalirEntregar,
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = CletaNaranjaClaro)
                    ) {
                        Icon(Icons.Default.DeliveryDining, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Salir a entregar", fontWeight = FontWeight.SemiBold)
                    }
                }
                2 -> {
                    // EN_CAMINO: ver ruta + marcar entregado
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick  = onVerMapa,
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(10.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = CletaNaranja),
                            border   = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Ver ruta", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick  = onMarcarEntregado,
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(10.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = CletaExito)
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Entregar", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ── Drawer del repartidor ─────────────────────────────────────────
@Composable
private fun DrawerRepartidor(
    nombre:        String,
    email:         String,
    onPerfil:      () -> Unit,
    onHistorial:   () -> Unit,
    onCerrarSesion: () -> Unit
) {
    ModalDrawerSheet(drawerContainerColor = CletaGrisMedio) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CletaNaranjaOscuro)
                .padding(vertical = 32.dp, horizontal = 20.dp)
        ) {
            Column {
                Icon(
                    Icons.Default.DeliveryDining,
                    contentDescription = null,
                    tint     = CletaBlanco,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(nombre, color = CletaBlanco, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(email,  color = CletaBlanco.copy(alpha = 0.8f), fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CletaBlanco.copy(alpha = 0.2f)
                ) {
                    Text(
                        "Repartidor",
                        color     = CletaBlanco,
                        fontSize  = 11.sp,
                        modifier  = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        NavigationDrawerItem(
            icon     = {
                Icon(Icons.Default.Person, contentDescription = null, tint = CletaNaranja)
            },
            label    = { Text("Mi Perfil", color = CletaBlanco) },
            selected = false,
            onClick  = onPerfil,
            colors   = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = CletaGrisMedio
            )
        )

        NavigationDrawerItem(
            icon     = {
                Icon(Icons.Default.History, contentDescription = null, tint = CletaNaranja)
            },
            label    = { Text("Historial de entregas", color = CletaBlanco) },
            selected = false,
            onClick  = onHistorial,
            colors   = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = CletaGrisMedio
            )
        )

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