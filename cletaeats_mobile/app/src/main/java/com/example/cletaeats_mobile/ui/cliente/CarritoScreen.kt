package com.example.cletaeats_mobile.ui.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cletaeats_mobile.domain.model.ItemCarrito
import com.example.cletaeats_mobile.ui.components.CletaButton
import com.example.cletaeats_mobile.ui.components.ErrorBanner
import com.example.cletaeats.ui.theme.*
import com.example.cletaeats_mobile.ui.utils.toCRC
import com.example.cletaeats_mobile.viewmodel.CarritoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    carritoViewModel: CarritoViewModel,
    onPedidoCreado:   () -> Unit,
    onVolver:         () -> Unit
) {
    val state by carritoViewModel.uiState.collectAsState()

    // Navegar a factura cuando el pedido se creó exitosamente
    LaunchedEffect(state.pedidoCreado) {
        if (state.pedidoCreado) onPedidoCreado()
    }

    // ── Estado local: distancia en km ─────────────────────────────
    var distanciaTexto by remember { mutableStateOf("5.0") }
    val distanciaError = distanciaTexto.toDoubleOrNull().let { d ->
        when {
            d == null -> "Ingresá un número válido"
            d <= 0    -> "La distancia debe ser mayor a 0"
            d > 100   -> "¿Más de 100 km? Verificá el dato"
            else      -> null
        }
    }
    val distanciaValida = distanciaError == null && distanciaTexto.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Mi carrito", color = CletaBlanco, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = CletaBlanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CletaGrisMedio)
            )
        },
        containerColor = CletaGrisOscuro
    ) { padding ->

        if (state.estaVacio) {
            // ── Carrito vacío ─────────────────────────────────────
            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null,
                    tint = CletaTextoSecundario, modifier = Modifier.size(80.dp))
                Spacer(Modifier.height(16.dp))
                Text("Tu carrito está vacío", color = CletaBlanco, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Agregá combos desde el menú del restaurante.", color = CletaTextoSecundario, fontSize = 14.sp)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onVolver, colors = ButtonDefaults.buttonColors(containerColor = CletaNaranja)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Volver al menú")
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Encabezado: restaurante ───────────────────────────
            item {
                Text(
                    text       = state.restauranteNombre,
                    color      = CletaBlanco,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${state.totalItems} ítems",
                    color    = CletaTextoSecundario,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
                ErrorBanner(state.errorMsg)
            }

            // ── Items del carrito ─────────────────────────────────
            items(state.items) { item ->
                ItemCarritoCard(
                    item      = item,
                    onAgregar  = { carritoViewModel.agregarCombo(item.combo) },
                    onReducir  = { carritoViewModel.reducirCombo(item.combo.id) },
                    onEliminar = { carritoViewModel.eliminarCombo(item.combo.id) }
                )
            }

            // ── Campo de distancia (validación dinámica) ──────────
            item {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = CletaGrisMedio)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Route, contentDescription = null,
                                tint = CletaNaranja, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Distancia de entrega", color = CletaBlanco, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value         = distanciaTexto,
                            onValueChange = { distanciaTexto = it },
                            label         = { Text("Kilómetros") },
                            isError       = distanciaError != null,
                            supportingText = distanciaError?.let { { Text(it) } },
                            trailingIcon  = { Text("km", color = CletaTextoSecundario, fontSize = 13.sp, modifier = Modifier.padding(end = 8.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier      = Modifier.fillMaxWidth(),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor  = CletaNaranja,
                                focusedLabelColor   = CletaNaranja,
                                focusedTextColor    = CletaBlanco,
                                unfocusedTextColor  = CletaBlanco,
                                unfocusedLabelColor = CletaTextoSecundario
                            )
                        )
                    }
                }
            }

            // ── Resumen de costos ─────────────────────────────────
            item {
                ResumenCostos(
                    subtotal    = state.subtotal,
                    distanciaKm = distanciaTexto.toDoubleOrNull() ?: 0.0
                )
            }

            // ── Botón confirmar ───────────────────────────────────
            item {
                CletaButton(
                    text      = "Confirmar pedido",
                    onClick   = {
                        carritoViewModel.confirmarPedido(
                            distanciaTexto.toDouble()
                        )
                    },
                    isLoading = state.isLoading,
                    enabled   = distanciaValida && !state.estaVacio,
                    icon      = Icons.Default.CheckCircle
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── Card de ítem en el carrito ────────────────────────────────────
@Composable
private fun ItemCarritoCard(
    item:      ItemCarrito,
    onAgregar:  () -> Unit,
    onReducir:  () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = CletaGrisMedio)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = item.combo.nombre,
                    color      = CletaBlanco,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp
                )
                Text(
                    text     = "${item.combo.precio.toCRC()} × ${item.cantidad}",
                    color    = CletaTextoSecundario,
                    fontSize = 13.sp
                )
                Text(
                    text       = item.subtotal.toCRC(),
                    color      = CletaNaranja,
                    fontWeight = FontWeight.Bold
                )
            }

            // Controles cantidad
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onReducir, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Reducir", tint = CletaTextoSecundario, modifier = Modifier.size(16.dp))
                }
                Text("${item.cantidad}", color = CletaBlanco, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                IconButton(onClick = onAgregar, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar", tint = CletaNaranja, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(4.dp))
                // Botón eliminar (ImageButton con ícono de basura)
                IconButton(
                    onClick  = onEliminar,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = CletaError, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ── Resumen de costos con cálculo IVA ────────────────────────────
@Composable
private fun ResumenCostos(subtotal: Double, distanciaKm: Double) {
    val costoTransporte = distanciaKm * 1000.0   // ₡1.000/km hábil
    val iva             = subtotal * 0.13
    val total           = subtotal + costoTransporte + iva

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CletaGrisMedio)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resumen", color = CletaBlanco, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            FilaCosto("Subtotal combos", subtotal)
            FilaCosto("Transporte (${String.format("%.1f", distanciaKm)} km × ₡1.000)", costoTransporte)
            FilaCosto("IVA (13%)", iva)
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = CletaGrisClaro)
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TOTAL", color = CletaBlanco, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                Text(total.toCRC(), color = CletaNaranja, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            }
        }
    }
}

@Composable
private fun FilaCosto(label: String, valor: Double) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = CletaTextoSecundario, fontSize = 14.sp)
        Text(valor.toCRC(), color = CletaBlanco, fontSize = 14.sp)
    }
}