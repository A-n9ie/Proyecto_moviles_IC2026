package com.example.cletaeats_mobile.ui.cliente

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cletaeats.ui.theme.*

import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RastreoRepartidorScreen(
    pedidoId: Int,
    onVolver: () -> Unit
) {
    var posicionRepartidor by remember { mutableStateOf<GeoPoint?>(null) }
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    // Escuchar Firestore en tiempo real
    DisposableEffect(pedidoId) {
        val listener = FirebaseFirestore.getInstance()
            .collection("ubicaciones_repartidores")
            .document(pedidoId.toString())
            .addSnapshotListener { snapshot, _ ->
                val lat = snapshot?.getDouble("latitud")
                val lng = snapshot?.getDouble("longitud")
                if (lat != null && lng != null) {
                    posicionRepartidor = GeoPoint(lat, lng)
                }
            }
        onDispose { listener.remove() }
    }

    // Mover cámara cuando llega la ubicación del repartidor
    LaunchedEffect(posicionRepartidor) {
        posicionRepartidor?.let { gp ->
            mapViewRef.value?.controller?.animateTo(gp, 15.0, null)
        }
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
                    Text("Rastreando pedido #$pedidoId",
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

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        controller.setCenter(GeoPoint(9.9281, -84.0907)) // Centro inicial CR
                        mapViewRef.value = this
                    }
                },
                update = { mapView ->
                    mapView.overlays.removeAll { it is Marker }
                    posicionRepartidor?.let { gp ->
                        val marker = Marker(mapView).apply {
                            position = gp
                            title = "Tu repartidor"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mapView.overlays.add(marker)
                        mapView.invalidate()
                    }
                    mapViewRef.value = mapView
                }
            )

            if (posicionRepartidor == null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = CletaNaranja)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Esperando ubicación del repartidor...",
                        color = CletaBlanco,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "El repartidor debe tener el pedido EN_CAMINO y permisos de ubicación activos.",
                        color = CletaTextoSecundario,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}