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
import com.example.cletaeats.ui.theme.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RastreoRepartidorScreen(
    pedidoId: Int,
    onVolver: () -> Unit
) {
    var posicionRepartidor by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState()

    // Escuchar Firestore en tiempo real
    DisposableEffect(pedidoId) {
        val listener = FirebaseFirestore.getInstance()
            .collection("ubicaciones_repartidores")
            .document(pedidoId.toString())
            .addSnapshotListener { snapshot, _ ->
                val lat = snapshot?.getDouble("latitud")
                val lng = snapshot?.getDouble("longitud")
                if (lat != null && lng != null) {
                    posicionRepartidor = LatLng(lat, lng)
                }
            }
        onDispose { listener.remove() }
    }

    // Mover cámara cuando llega la ubicación
    LaunchedEffect(posicionRepartidor) {
        posicionRepartidor?.let {
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(it, 15f)
            )
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
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                posicionRepartidor?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Tu repartidor",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }
            }

            if (posicionRepartidor == null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = CletaNaranja)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Esperando ubicación del repartidor...", color = CletaBlanco)
                }
            }
        }
    }
}