package com.example.cletaeats_mobile.ui.cliente

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cletaeats_mobile.AppContainer
import com.example.cletaeats_mobile.domain.model.Pedido
import com.example.cletaeats.ui.theme.*
import com.example.cletaeats_mobile.viewmodel.PedidosClienteViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.cletaeats_mobile.ui.components.ModoBadge
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.example.cletaeats_mobile.viewmodel.CarritoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPedidosScreen(
    viewModel: PedidosClienteViewModel,
    onVolver: () -> Unit,
    onRastrear: (Int) -> Unit,
    carritoVm: CarritoViewModel,
    onIrARestaurante: (Int, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pedidosFiltrados by viewModel.pedidosFiltrados.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mientras haya un diálogo abierto (ej. reportar problema), se pausa el
    // polling para no recomponer la pantalla y cerrar el diálogo por debajo.
    var dialogoAbierto by remember { mutableStateOf(false) }

    // Muestra el feedback de la queja (éxito o error) y lo limpia
    LaunchedEffect(uiState.quejaMsg) {
        uiState.quejaMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.limpiarQuejaMsg()
        }
    }

    // Polling: recarga pedidos cada 15s mientras la pantalla esté visible,
    // SALVO que haya un diálogo abierto (no interrumpir al usuario).
    LaunchedEffect(Unit) {
        while (isActive) {
            if (!dialogoAbierto) {
                viewModel.cargarPedidos()
            }
            delay(15_000L)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Mis pedidos", color = CletaBlanco, fontWeight = FontWeight.Bold)
                        ModoBadge(AppContainer.getSessionManager().getDataMode())
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = CletaBlanco)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarPedidos() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = CletaBlanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CletaGrisMedio)
            )
        },
        containerColor = CletaGrisOscuro
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center), color = CletaNaranja
                )
                uiState.errorMsg != null -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.WifiOff, null, tint = CletaNaranja,
                        modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(uiState.errorMsg!!, color = CletaTextoSecundario)
                }
                uiState.pedidos.isEmpty() -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.ReceiptLong, null, tint = CletaTextoSecundario,
                        modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Sin pedidos aún", color = CletaBlanco, fontSize = 20.sp,
                        fontWeight = FontWeight.Bold)
                    Text("Tus pedidos aparecerán aquí.", color = CletaTextoSecundario,
                        fontSize = 14.sp)
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Chips de filtro ──────────────────────────────────────
                    item {
                        val filtros = listOf(
                            null to "Todos",
                            0    to "Creado",
                            2    to "En camino",
                            3    to "Entregado",
                            4    to "Cancelado"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filtros.forEach { (estado, label) ->
                                FilterChip(
                                    selected  = uiState.filtroEstado == estado,
                                    onClick   = { viewModel.setFiltro(estado) },
                                    label     = { Text(label, fontSize = 12.sp) },
                                    colors    = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor    = CletaNaranja,
                                        selectedLabelColor        = CletaBlanco,
                                        containerColor            = CletaGrisMedio,
                                        labelColor                = CletaTextoSecundario
                                    )
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    // ── Conteo ───────────────────────────────────────────────
                    item {
                        Text(
                            "${pedidosFiltrados.size} pedido(s)",
                            color = CletaBlanco, fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    // ── Lista filtrada ────────────────────────────────────────
                    items(pedidosFiltrados) { pedido ->
                        PedidoClienteCard(
                            pedido     = pedido,
                            onRastrear = { onRastrear(pedido.id) },
                            onCalificar = { rating -> viewModel.calificarRepartidor(pedido.id, rating) },
                            onCancelar  = { viewModel.cancelarPedido(pedido.id) },
                            onReportar  = { motivo, desc -> viewModel.crearQueja(pedido.id, motivo, desc) },
                            onDialogoChange = { abierto -> dialogoAbierto = abierto },
                            onVolverAPedir = { viewModel.volverAPedir(pedido.id, carritoVm) { restId, restNombre ->
                                onIrARestaurante(restId, restNombre)
                            }},

                        )
                    }


                }
            }
        }
    }
}

@Composable
private fun PedidoClienteCard(
    pedido: Pedido,
    onRastrear: () -> Unit,
    onCalificar: (Int) -> Unit,
    onCancelar: () -> Unit,
    onReportar: (String, String) -> Unit,
    onDialogoChange: (Boolean) -> Unit,
    onVolverAPedir: () -> Unit,
) {
    val estadoColor = when (pedido.estado) {
        3    -> CletaExito
        4    -> CletaError
        2    -> CletaNaranjaClaro
        else -> CletaTextoSecundario
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CletaGrisMedio)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Pedido #${pedido.id}", color = CletaBlanco,
                    fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Surface(shape = RoundedCornerShape(50), color = estadoColor.copy(alpha = 0.2f)) {
                    Text(
                        pedido.estadoTexto.replace("_", " "),
                        color = estadoColor, fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Restaurant, null, tint = CletaNaranja,
                    modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(pedido.restauranteNombre, color = CletaBlanco, fontSize = 14.sp)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = CletaTextoSecundario,
                    modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text(pedido.fechaCreacion, color = CletaTextoSecundario, fontSize = 12.sp)
            }
            Spacer(Modifier.height(4.dp))
            if (pedido.itemsDetalle.isNotEmpty()) {
                pedido.itemsDetalle.forEach { item ->
                    Text(item, color = CletaTextoSecundario, fontSize = 12.sp)
                }
            } else {
                Text("${pedido.itemsCount} combo(s)", color = CletaTextoSecundario, fontSize = 12.sp)
            }
            Text("${pedido.distanciaKm} km", color = CletaTextoSecundario, fontSize = 12.sp)

            // Solo mostrar si el pedido está EN_CAMINO (estado 2)
            if (pedido.estado == 2) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onRastrear,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CletaNaranja)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Rastrear repartidor", fontWeight = FontWeight.SemiBold)
                }
            }

            if (pedido.estado == 3) {
                val enviado = pedido.calificado
                var rating by remember(pedido.id) { mutableStateOf(if (enviado) 5 else 0) }

                Spacer(Modifier.height(10.dp))
                Text("Calificar repartidor:", color = CletaBlanco, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Row {
                    (1..5).forEach { estrella ->
                        IconButton(
                            onClick = {
                                if (!enviado) {
                                    rating = estrella
                                    onCalificar(estrella)
                                }
                            },
                            enabled = !enviado,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (estrella <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$estrella estrellas",
                                tint = if (enviado) CletaTextoSecundario else CletaNaranja
                            )
                        }
                    }
                }
                if (enviado) {
                    Text("¡Gracias por tu calificación!", color = CletaExito, fontSize = 12.sp)
                }

                // ── Reportar problema (queja) ─────────────────────────────
                var mostrarDialogoQueja by remember { mutableStateOf(false) }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        mostrarDialogoQueja = true
                        onDialogoChange(true)   // pausa el polling
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CletaNaranjaClaro),
                    border = BorderStroke(1.dp, CletaNaranjaClaro)
                ) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Reportar problema", fontWeight = FontWeight.SemiBold)
                }

                if (mostrarDialogoQueja) {
                    DialogoQueja(
                        onConfirmar = { motivo, desc ->
                            onReportar(motivo, desc)
                            mostrarDialogoQueja = false
                            onDialogoChange(false)   // reanuda el polling
                        },
                        onCancelar = {
                            mostrarDialogoQueja = false
                            onDialogoChange(false)   // reanuda el polling
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onVolverAPedir,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CletaGrisMedio),
                    border = BorderStroke(1.dp, CletaNaranja)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp), tint = CletaNaranja)
                    Spacer(Modifier.width(8.dp))
                    Text("Volver a pedir", color = CletaNaranja, fontWeight = FontWeight.SemiBold)
                }

            }

            if (pedido.estado == 0) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onCancelar,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CletaError),
                    border = BorderStroke(1.dp, CletaError)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Cancelar pedido", fontWeight = FontWeight.SemiBold)
                }
            }

        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoQueja(
    onConfirmar: (String, String) -> Unit,
    onCancelar: () -> Unit
) {
    // motivo interno (valor que espera el backend) y etiqueta visible
    val motivos = listOf(
        "amabilidad"   to "Amabilidad",
        "tiempo"       to "Tiempo de entrega",
        "presentacion" to "Presentación",
        "otro"         to "Otro"
    )
    var motivoSeleccionado by remember { mutableStateOf(motivos.first().first) }
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = CletaGrisMedio,
        title = {
            Text("Reportar problema", color = CletaBlanco, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("¿Cuál fue el problema?", color = CletaTextoSecundario, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                motivos.forEach { (valor, etiqueta) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = motivoSeleccionado == valor,
                            onClick = { motivoSeleccionado = valor },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = CletaNaranja,
                                unselectedColor = CletaTextoSecundario
                            )
                        )
                        Text(etiqueta, color = CletaBlanco, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Describí el problema (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CletaNaranja,
                        unfocusedBorderColor = CletaTextoSecundario,
                        focusedLabelColor = CletaNaranja,
                        unfocusedLabelColor = CletaTextoSecundario,
                        focusedTextColor = CletaBlanco,
                        unfocusedTextColor = CletaBlanco,
                        cursorColor = CletaNaranja
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(motivoSeleccionado, descripcion) }) {
                Text("Enviar", color = CletaNaranja, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = CletaTextoSecundario)
            }
        }
    )
}