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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cletaeats_mobile.domain.model.FacturaData
import com.example.cletaeats_mobile.domain.model.ItemFactura
import com.example.cletaeats.ui.theme.*
import com.example.cletaeats_mobile.ui.utils.toCRC
import com.example.cletaeats_mobile.viewmodel.CarritoViewModel

@Composable
fun FacturaScreen(
    carritoViewModel: CarritoViewModel,
    onVolver:         () -> Unit
) {
    val state   by carritoViewModel.uiState.collectAsState()
    val factura = state.factura

    // Si la factura es null (no debería ocurrir en flujo normal), volver
    if (factura == null) {
        LaunchedEffect(Unit) { onVolver() }
        return
    }

    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .background(CletaGrisOscuro),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Cabecera ──────────────────────────────────────────────
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(CletaExito.copy(alpha = 0.15f), shape = RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null,
                        tint = CletaExito, modifier = Modifier.size(48.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text("¡Pedido confirmado!", color = CletaBlanco, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text("Pedido #${factura.pedidoId}", color = CletaTextoSecundario, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text(factura.fechaCreacion, color = CletaTextoSecundario, fontSize = 12.sp)
            }
        }

        // ── Info de entrega ───────────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = CletaGrisMedio)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Información del pedido", color = CletaBlanco, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Divider(color = CletaGrisClaro)
                    InfoFila(Icons.Default.Restaurant, "Restaurante", factura.restauranteNombre)
                    InfoFila(Icons.Default.Person,     "Cliente",     factura.clienteNombre)
                    InfoFila(Icons.Default.DeliveryDining, "Repartidor", factura.repartidorNombre)
                    InfoFila(Icons.Default.Route,      "Distancia",   "${factura.distanciaKm} km")
                }
            }
        }

        // ── Detalle de combos ─────────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = CletaGrisMedio)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Detalle de combos", color = CletaBlanco, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = CletaGrisClaro)
                    factura.items.forEach { item ->
                        FacturaItemFila(item)
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }

        // ── Totales ───────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = CletaGrisMedio)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Resumen de cobro", color = CletaBlanco, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Divider(color = CletaGrisClaro)
                    Spacer(Modifier.height(4.dp))
                    TotalFila("Subtotal combos",  factura.subtotal)
                    TotalFila("Costo transporte", factura.costoTransporte)
                    TotalFila("IVA (13%)",        factura.iva)
                    Divider(modifier = Modifier.padding(vertical = 6.dp), color = CletaNaranja)
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL PAGADO", color = CletaBlanco, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text(factura.total.toCRC(), color = CletaNaranja, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Cobrado a tu tarjeta registrada",
                        color     = CletaTextoSecundario,
                        fontSize  = 12.sp,
                        modifier  = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ── Botón volver al inicio ────────────────────────────────
        item {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick  = onVolver,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CletaNaranja)
            ) {
                Icon(Icons.Default.Home, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Volver al inicio", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoFila(icono: androidx.compose.ui.graphics.vector.ImageVector, label: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = CletaNaranja, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = CletaTextoSecundario, fontSize = 11.sp)
            Text(valor, color = CletaBlanco, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun FacturaItemFila(item: ItemFactura) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Combo #${item.numeroCombo} — ${item.comboNombre}", color = CletaBlanco, fontSize = 13.sp)
            Text("${item.precioUnitario.toCRC()} × ${item.cantidad}", color = CletaTextoSecundario, fontSize = 12.sp)
        }
        Text(item.subtotalItem.toCRC(), color = CletaBlanco, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun TotalFila(label: String, valor: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = CletaTextoSecundario, fontSize = 14.sp)
        Text(valor.toCRC(), color = CletaBlanco, fontSize = 14.sp)
    }
}