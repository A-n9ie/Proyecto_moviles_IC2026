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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPedidosScreen(
    viewModel: PedidosClienteViewModel,
    onVolver: () -> Unit,
    onRastrear: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargarPedidos() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis pedidos", color = CletaBlanco, fontWeight = FontWeight.Bold) },
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
                    item {
                        Text("${uiState.pedidos.size} pedido(s)",
                            color = CletaBlanco, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                    }
                    items(uiState.pedidos) { pedido ->
                        PedidoClienteCard(
                            pedido = pedido,
                            onRastrear = { onRastrear(pedido.id) },
                            onCalificar = { rating -> viewModel.calificarRepartidor(pedido.id, rating) },
                            onCancelar = { viewModel.cancelarPedido(pedido.id) }
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
    onCancelar: () -> Unit
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
            Text("${pedido.itemsCount} combo(s) · ${pedido.distanciaKm} km",
                color = CletaTextoSecundario, fontSize = 12.sp)

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
                var rating by remember { mutableStateOf(0) }
                var enviado by remember { mutableStateOf(false) }

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
                                    enviado = true
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (estrella <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$estrella estrellas",
                                tint = CletaNaranja
                            )
                        }
                    }
                }
                if (enviado) {
                    Text("¡Gracias por tu calificación!", color = CletaExito, fontSize = 12.sp)
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