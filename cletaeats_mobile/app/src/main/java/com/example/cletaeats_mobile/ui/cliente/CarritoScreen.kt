package com.example.cletaeats_mobile.ui.cliente

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cletaeats_mobile.domain.model.ItemCarrito
import com.example.cletaeats_mobile.ui.components.CletaButton
import com.example.cletaeats_mobile.ui.components.ErrorBanner
import com.example.cletaeats.ui.theme.*
import com.example.cletaeats_mobile.ui.utils.toCRC
import com.example.cletaeats_mobile.viewmodel.CarritoViewModel
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import com.example.cletaeats_mobile.viewmodel.TarjetaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    carritoViewModel: CarritoViewModel,
    tarjetaViewModel: TarjetaViewModel,
    onPedidoCreado: () -> Unit,
    onVolver: () -> Unit
) {
    val state by carritoViewModel.uiState.collectAsState()

    LaunchedEffect(state.pedidoCreado) {
        if (state.pedidoCreado) onPedidoCreado()
    }

    val distanciaKm = state.distanciaKm

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

            items(state.items) { item ->
                ItemCarritoCard(
                    item      = item,
                    onAgregar  = { carritoViewModel.agregarCombo(item.combo) },
                    onReducir  = { carritoViewModel.reducirCombo(item.combo.id) },
                    onEliminar = { carritoViewModel.eliminarCombo(item.combo.id) }
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = CletaGrisMedio)
                ) {
                    Row(
                        modifier          = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (state.tieneGps) Icons.Default.MyLocation else Icons.Default.LocationOff,
                            contentDescription = null,
                            tint     = if (state.tieneGps) CletaNaranja else CletaTextoSecundario,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "Distancia de entrega",
                                color      = CletaBlanco,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (state.distanciaKm > 0)
                                    "${String.format("%.1f", state.distanciaKm)} km"
                                else
                                    "Calculando...",
                                color    = CletaNaranja,
                                fontSize = 14.sp
                            )
                            Text(
                                if (state.tieneGps) "Calculada con tu ubicación GPS"
                                else                "Estimada (GPS no disponible)",
                                color    = CletaTextoSecundario,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            item {
                ResumenCostos(subtotal = state.subtotal, distanciaKm = distanciaKm)
            }

            item {
                MetodoPagoCard(
                    tarjetaViewModel = tarjetaViewModel,
                    carritoViewModel = carritoViewModel
                )
            }

            item {
                CletaButton(
                    text      = "Confirmar pedido",
                    onClick   = {
                        carritoViewModel.confirmarPedido()
                    },
                    isLoading = state.isLoading,
                    enabled = !state.estaVacio && state.tarjetaId != null && !state.isLoading,
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onReducir, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Reducir", tint = CletaTextoSecundario, modifier = Modifier.size(16.dp))
                }
                Text("${item.cantidad}", color = CletaBlanco, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                IconButton(onClick = onAgregar, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar", tint = CletaNaranja, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(4.dp))
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

// ── Resumen de costos ─────────────────────────────────────────────
@Composable
private fun ResumenCostos(subtotal: Double, distanciaKm: Double) {
    val costoTransporte = distanciaKm * 1000.0
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
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = CletaGrisClaro)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MetodoPagoCard(
    tarjetaViewModel: TarjetaViewModel,
    carritoViewModel: CarritoViewModel
) {
    val tarjetaState by tarjetaViewModel.uiState.collectAsState()
    // Estado del popup tipo wallet
    var mostrarWallet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { tarjetaViewModel.cargarTarjetas() }

    LaunchedEffect(tarjetaState.tarjetaSeleccionada) {
        tarjetaState.tarjetaSeleccionada?.let {
            carritoViewModel.seleccionarTarjeta(it.id)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CletaGrisMedio)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CreditCard, contentDescription = null,
                    tint = CletaNaranja, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Método de pago", color = CletaBlanco, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))

                TextButton(
                    onClick  = { mostrarWallet = true },
                    enabled  = !tarjetaState.limiteAlcanzado
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null,
                        tint = CletaNaranja, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("+ Agregar", color = CletaNaranja, fontSize = 12.sp)
                }
            }

            if (tarjetaState.limiteAlcanzado) {
                Text(
                    "Límite de 5 tarjetas alcanzado",
                    color    = CletaTextoSecundario,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            Spacer(Modifier.height(10.dp))

            if (tarjetaState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = CletaNaranja)
            } else if (tarjetaState.tarjetas.isEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CletaGrisClaro.copy(alpha = 0.3f))
                        .clickable { mostrarWallet = true }
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.AddCard, contentDescription = null,
                        tint = CletaNaranja, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tocá aquí para agregar una tarjeta", color = CletaTextoSecundario, fontSize = 13.sp)
                }
            } else {
                tarjetaState.tarjetas.forEach { tarjeta ->
                    val seleccionada = tarjeta.id == tarjetaState.tarjetaSeleccionada?.id
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .clickable { tarjetaViewModel.seleccionarTarjeta(tarjeta) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = seleccionada,
                            onClick  = { tarjetaViewModel.seleccionarTarjeta(tarjeta) },
                            colors   = RadioButtonDefaults.colors(selectedColor = CletaNaranja)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = "•••• ${tarjeta.numero.takeLast(4)}",
                                color = CletaBlanco, fontSize = 14.sp
                            )
                            if (tarjeta.alias.isNotEmpty()) {
                                Text(tarjeta.alias, color = CletaTextoSecundario, fontSize = 12.sp)
                            }
                        }
                        if (tarjeta.esPrincipal == 1) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = CletaNaranja.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "Principal",
                                    color    = CletaNaranja,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick  = {
                                tarjetaViewModel.eliminarTarjeta(tarjeta.id)
                                // Si se eliminó la tarjeta activa, limpiar selección en el carrito
                                if (tarjeta.id == tarjetaState.tarjetaSeleccionada?.id) {
                                    carritoViewModel.seleccionarTarjeta(-1)  // -1 = ninguna
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar",
                                tint = CletaError, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    // ── Wallet popup ─────────────────────────────────────────────
    if (mostrarWallet) {
        WalletDialog(
            onDismiss = { mostrarWallet = false },
            onGuardar = { numero, alias, fechaVencimiento, cvv, esPrincipal ->
                tarjetaViewModel.agregarTarjeta(
                    numero,
                    alias,
                    fechaVencimiento,
                    cvv,
                    esPrincipal
                )
                mostrarWallet = false
            }
        )
    }
}

// ── Wallet Dialog tipo popup ──────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletDialog(
    onDismiss: () -> Unit,
    onGuardar: (
        numero: String,
        alias: String,
        fechaVencimiento: String,
        cvv: String,
        esPrincipal: Boolean
    ) -> Unit
) {
    var numero by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var fechaVencimiento by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var principal by remember { mutableStateOf(false) }

    val numeroError = when {
        numero.isEmpty()    -> null
        numero.length < 8  -> "Mínimo 8 dígitos"
        numero.length > 19  -> "Máximo 19 dígitos"
        else                -> null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape    = RoundedCornerShape(24.dp),
            color    = CletaGrisMedio,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // ── Header wallet ──────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CletaNaranja.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint     = CletaNaranja,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Agregar tarjeta",
                            color      = CletaBlanco,
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Tu información está protegida",
                            color    = CletaTextoSecundario,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = CletaTextoSecundario)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Preview visual de tarjeta ──────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (numero.isEmpty()) CletaGrisClaro
                            else CletaNaranja.copy(alpha = 0.85f)
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (numero.length >= 4)
                                    "•••• •••• •••• ${numero.takeLast(4)}"
                                else "•••• •••• •••• ••••",
                                color      = CletaBlanco,
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text     = alias.ifEmpty { "Alias de tarjeta" },
                                color    = CletaBlanco.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint     = CletaBlanco.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Campo número ───────────────────────────────────
                OutlinedTextField(
                    value         = numero,
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 19) numero = it },
                    label         = { Text("Número de tarjeta") },
                    isError       = numeroError != null,
                    supportingText = numeroError?.let { { Text(it) } },
                    leadingIcon   = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = CletaNaranja) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor  = CletaNaranja,
                        focusedLabelColor   = CletaNaranja,
                        focusedTextColor    = CletaBlanco,
                        unfocusedTextColor  = CletaBlanco,
                        unfocusedLabelColor = CletaTextoSecundario
                    )
                )

                Spacer(Modifier.height(12.dp))

                // ── Campo alias ────────────────────────────────────
                OutlinedTextField(
                    value         = alias,
                    onValueChange = { alias = it },
                    label         = { Text("Alias (ej. Visa personal)") },
                    leadingIcon   = { Icon(Icons.Default.Label, contentDescription = null, tint = CletaNaranja) },
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor  = CletaNaranja,
                        focusedLabelColor   = CletaNaranja,
                        focusedTextColor    = CletaBlanco,
                        unfocusedTextColor  = CletaBlanco,
                        unfocusedLabelColor = CletaTextoSecundario
                    )
                )

                Spacer(Modifier.height(12.dp))

                // ── Toggle principal ───────────────────────────────
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null,
                        tint = if (principal) CletaNaranja else CletaTextoSecundario,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Marcar como tarjeta principal", color = CletaBlanco, fontSize = 14.sp,
                        modifier = Modifier.weight(1f))
                    Switch(
                        checked         = principal,
                        onCheckedChange = { principal = it },
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor  = CletaBlanco,
                            checkedTrackColor  = CletaNaranja
                        )
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Botones ────────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors   = ButtonColors(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                            contentColor   = CletaTextoSecundario,
                            disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            disabledContentColor   = CletaTextoSecundario
                        ),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            onGuardar(
                                numero,
                                alias,
                                fechaVencimiento,
                                cvv,
                                principal
                            )
                        },
                        enabled  = numero.length >= 8 && numeroError == null,
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(containerColor = CletaNaranja)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Guardar")
                    }
                }
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


