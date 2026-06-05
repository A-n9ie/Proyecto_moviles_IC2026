package com.example.cletaeats_mobile.ui.repartidor

import android.location.Location
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.cletaeats.ui.theme.*
import com.example.cletaeats_mobile.domain.model.Pedido
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay

// ── Estados de la simulación ──────────────────────────────────────
enum class EstadoSimulacion {
    PREPARANDO,    // 5 seg — repartidor espera en restaurante
    EN_CAMINO,     // 10 seg — repartidor viaja hacia el cliente
    ENTREGADO      // final
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaSeguimientoScreen(
    pedido:   Pedido,
    onVolver: () -> Unit,
    onEntregado: () -> Unit
) {
    // ── Coordenadas ───────────────────────────────────────────────
    val latRest = pedido.restauranteLatitud  ?: 9.9281
    val lngRest = pedido.restauranteLongitud ?: -84.0907

    // Destino = coordenadas del restaurante desplazadas para simular
    // un punto de entrega cercano (0.01 grados ≈ 1 km)
    val latDest = latRest + 0.012
    val lngDest = lngRest + 0.008

    val origenLatLng  = LatLng(latRest, lngRest)
    val destinoLatLng = LatLng(latDest, lngDest)

    // ── Estado de la simulación ───────────────────────────────────
    var estadoSim by remember { mutableStateOf(EstadoSimulacion.PREPARANDO) }
    var progreso  by remember { mutableFloatStateOf(0f) }  // 0.0 → 1.0 durante EN_CAMINO

    // Posición animada del repartidor en el mapa
    var posRepartidor by remember { mutableStateOf(origenLatLng) }

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(origenLatLng, 13f)
    }

    // ── Simulación con coroutines ─────────────────────────────────
    LaunchedEffect(Unit) {
        // Fase 1: PREPARANDO — 5 segundos
        estadoSim = EstadoSimulacion.PREPARANDO
        delay(5_000L)

        // Fase 2: EN_CAMINO — 10 segundos interpolando posición
        estadoSim = EstadoSimulacion.EN_CAMINO
        val pasos = 20  // una actualización cada 500ms
        repeat(pasos) { i ->
            val t = (i + 1).toFloat() / pasos
            progreso = t
            // Interpolación lineal entre origen y destino
            posRepartidor = LatLng(
                latRest + (latDest - latRest) * t,
                lngRest + (lngDest - lngRest) * t
            )
            // Mover cámara suavemente siguiendo al repartidor
            cameraState.animate(
                CameraUpdateFactory.newLatLng(posRepartidor),
                durationMs = 450
            )
            delay(500L)
        }

        // Fase 3: ENTREGADO
        estadoSim = EstadoSimulacion.ENTREGADO
        delay(1_500L)
        onEntregado()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Pedido #${pedido.id} — Seguimiento",
                        color = CletaBlanco, fontWeight = FontWeight.Bold)
                },
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Mapa ──────────────────────────────────────────────
            GoogleMap(
                modifier            = Modifier.fillMaxSize(),
                cameraPositionState = cameraState
            ) {
                // Marcador restaurante (origen)
                Marker(
                    state   = MarkerState(position = origenLatLng),
                    title   = pedido.restauranteNombre,
                    snippet = "Origen",
                    icon    = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )

                // Marcador destino (cliente)
                Marker(
                    state   = MarkerState(position = destinoLatLng),
                    title   = "Punto de entrega",
                    snippet = pedido.clienteNombre,
                    icon    = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )

                // Marcador repartidor (se mueve)
                if (estadoSim != EstadoSimulacion.PREPARANDO) {
                    Marker(
                        state   = MarkerState(position = posRepartidor),
                        title   = "Repartidor",
                        snippet = "En camino",
                        icon    = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                }

                // Línea de ruta
                Polyline(
                    points = listOf(origenLatLng, destinoLatLng),
                    color  = CletaNaranja,
                    width  = 8f
                )
            }

            // ── Panel inferior de estado ──────────────────────────
            PanelEstado(
                estadoSim = estadoSim,
                progreso  = progreso,
                pedido    = pedido,
                modifier  = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

// ── Panel inferior con la info del estado ─────────────────────────
@Composable
private fun PanelEstado(
    estadoSim: EstadoSimulacion,
    progreso:  Float,
    pedido:    Pedido,
    modifier:  Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = CletaGrisMedio),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Ícono + título del estado
            Row(verticalAlignment = Alignment.CenterVertically) {
                val (icono, titulo, color) = when (estadoSim) {
                    EstadoSimulacion.PREPARANDO -> Triple(
                        Icons.Default.Restaurant, "Preparando tu pedido", CletaNaranjaClaro)
                    EstadoSimulacion.EN_CAMINO  -> Triple(
                        Icons.Default.DeliveryDining, "Repartidor en camino", CletaNaranja)
                    EstadoSimulacion.ENTREGADO  -> Triple(
                        Icons.Default.CheckCircle, "¡Pedido entregado!", CletaExito)
                }
                Icon(icono, contentDescription = null,
                    tint = color, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Text(titulo, color = CletaBlanco,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Barra de progreso (solo visible en EN_CAMINO)
            if (estadoSim == EstadoSimulacion.EN_CAMINO) {
                Text("Progreso del viaje",
                    color = CletaTextoSecundario, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color            = CletaNaranja,
                    trackColor       = CletaGrisClaro
                )
                Spacer(Modifier.height(4.dp))
                Text("${(progreso * 100).toInt()}%",
                    color = CletaNaranja, fontSize = 12.sp,
                    fontWeight = FontWeight.Bold)
            }

            // Contador visual durante PREPARANDO
            if (estadoSim == EstadoSimulacion.PREPARANDO) {
                ContadorPreparacion()
            }

            Spacer(Modifier.height(10.dp))

            // Info del pedido
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Restaurant, contentDescription = null,
                    tint = CletaTextoSecundario, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(pedido.restauranteNombre,
                    color = CletaTextoSecundario, fontSize = 13.sp)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Default.Person, contentDescription = null,
                    tint = CletaTextoSecundario, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(pedido.clienteNombre,
                    color = CletaTextoSecundario, fontSize = 13.sp)
            }
        }
    }
}

// ── Animación de puntos durante preparación ───────────────────────
@Composable
private fun ContadorPreparacion() {
    var puntos by remember { mutableStateOf(1) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(600L)
            puntos = if (puntos >= 3) 1 else puntos + 1
        }
    }
    Text(
        "El restaurante está preparando tu pedido" + ".".repeat(puntos),
        color    = CletaTextoSecundario,
        fontSize = 13.sp
    )
}