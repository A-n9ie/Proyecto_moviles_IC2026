package com.example.cletaeats_mobile.ui.repartidor

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
import kotlinx.coroutines.delay

import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

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

    val origenGp  = GeoPoint(latRest, lngRest)
    val destinoGp = GeoPoint(latDest, lngDest)

    // ── Estado de la simulación ───────────────────────────────────
    var estadoSim by remember { mutableStateOf(EstadoSimulacion.PREPARANDO) }
    var progreso  by remember { mutableFloatStateOf(0f) }  // 0.0 → 1.0 durante EN_CAMINO

    // Posición animada del repartidor en el mapa
    var posRepartidor by remember { mutableStateOf(origenGp) }
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

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
            posRepartidor = GeoPoint(
                latRest + (latDest - latRest) * t,
                lngRest + (lngDest - lngRest) * t
            )

            // Mover cámara siguiendo al repartidor
            mapViewRef.value?.controller?.animateTo(posRepartidor)

            delay(500L)
        }

        // Fase 3: ENTREGADO
        estadoSim = EstadoSimulacion.ENTREGADO
        delay(1_500L)
        onEntregado()
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME  -> mapViewRef.value?.onResume()
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE   -> mapViewRef.value?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewRef.value?.onDetach()
        }
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
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(13.0)
                        controller.setCenter(origenGp)
                        mapViewRef.value = this
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()

                    // Polyline de ruta
                    val polyline = Polyline(mapView).apply {
                        setPoints(listOf(origenGp, destinoGp))
                        outlinePaint.color = android.graphics.Color.parseColor("#FF6600") // CletaNaranja
                        outlinePaint.strokeWidth = 8f
                    }
                    mapView.overlays.add(polyline)

                    // Marcador origen (restaurante)
                    val markerOrigen = Marker(mapView).apply {
                        position = origenGp
                        title = pedido.restauranteNombre
                        snippet = "Origen"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mapView.overlays.add(markerOrigen)

                    // Marcador destino (cliente)
                    val markerDestino = Marker(mapView).apply {
                        position = destinoGp
                        title = "Punto de entrega"
                        snippet = pedido.clienteNombre
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mapView.overlays.add(markerDestino)

                    // Marcador repartidor (solo cuando está en camino)
                    if (estadoSim != EstadoSimulacion.PREPARANDO) {
                        val markerRep = Marker(mapView).apply {
                            position = posRepartidor
                            title = "Repartidor"
                            snippet = "En camino"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mapView.overlays.add(markerRep)
                    }

                    mapView.invalidate()
                    mapViewRef.value = mapView
                }
            )

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