package com.example.cletaeats_mobile.ui.repartidor

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
import com.example.cletaeats_mobile.domain.model.Pedido
import com.example.cletaeats.ui.theme.*
import com.example.cletaeats_mobile.viewmodel.HistorialRepartidorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialRepartidorScreen(
    viewModel: HistorialRepartidorViewModel,
    onBack:    () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargarHistorial() }

    Scaffold(
        containerColor = CletaGrisOscuro,
        topBar = {
            TopAppBar(
                title = { Text("Historial de entregas", color = CletaBlanco) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = CletaBlanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CletaGrisMedio)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // --- Tarjeta de resumen (métricas) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp),
                colors   = CardDefaults.cardColors(containerColor = CletaNaranja)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Resumen de actividad",
                        color = CletaBlanco,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricaItem("Entregas", "${uiState.totalEntregas}")
                        MetricaItem("Km totales", String.format("%.1f", uiState.kmTotales))
                        MetricaItem("Ganancias", "₡${String.format("%,.0f", uiState.ganancias)}")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CletaNaranja)
                    }
                }
                uiState.errorMsg != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.errorMsg!!, color = CletaError)
                    }
                }
                uiState.entregas.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Inbox,
                                contentDescription = null,
                                tint = CletaGrisClaro,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Aún no tenés entregas", color = CletaBlanco, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Tus pedidos entregados aparecerán aquí.",
                                color = CletaGrisClaro,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                else -> {
                    Text(
                        "${uiState.entregas.size} entrega(s)",
                        color = CletaBlanco,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(uiState.entregas) { pedido ->
                            EntregaCard(pedido)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricaItem(label: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, color = CletaBlanco, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = CletaBlanco, fontSize = 12.sp)
    }
}

@Composable
private fun EntregaCard(pedido: Pedido) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = CletaGrisMedio)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pedido #${pedido.id}", color = CletaBlanco, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CletaExito.copy(alpha = 0.2f)
                ) {
                    Text(
                        "ENTREGADO",
                        color = CletaExito,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = CletaNaranja, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(pedido.restauranteNombre, color = CletaBlanco)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = CletaGrisClaro, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(pedido.clienteNombre, color = CletaGrisClaro, fontSize = 13.sp)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = CletaGrisClaro, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (pedido.fechaEntrega.isNotBlank()) pedido.fechaEntrega else pedido.fechaCreacion,
                    color = CletaGrisClaro,
                    fontSize = 13.sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    String.format("%.1f km", pedido.distanciaKm),
                    color = CletaNaranja,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}