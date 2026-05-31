package com.example.cletaeats_mobile.ui.cliente

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.cletaeats_mobile.domain.model.Restaurante
import com.example.cletaeats.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import android.Manifest
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.google.android.gms.location.LocationServices
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaRestaurantesScreen(
    restaurantes: List<Restaurante>,
    onRestauranteClick: (Int) -> Unit,
    onVolver: () -> Unit
) {
    val heredia = LatLng(9.9981, -84.1170)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(heredia, 12f)
    }

    // Cuando lleguen los restaurantes, mover la cámara para forzar re-render
    LaunchedEffect(restaurantes.size) {
        if (restaurantes.isNotEmpty()) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(heredia, 12f)
            )
        }
    }

    val context = LocalContext.current
    var ubicacionCliente by remember { mutableStateOf<LatLng?>(null) }

    val permisosLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        ubicacionCliente = LatLng(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {
                android.util.Log.e("MAPA", "Permiso de ubicación denegado: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        permisosLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Restaurantes cerca de vos (${restaurantes.size})",
                        color = CletaBlanco,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = CletaBlanco
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CletaGrisMedio
                )
            )
        },
        containerColor = CletaGrisOscuro
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                ubicacionCliente?.let { clienteLatLng ->
                    Marker(
                        state = MarkerState(position = clienteLatLng),
                        title = "Tu ubicación",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
                android.util.Log.d("MAPA_DEBUG", "Renderizando ${restaurantes.size} restaurantes")
                restaurantes.forEach { r ->
                    android.util.Log.d("MAPA_DEBUG", "Marcador: ${r.nombre} lat=${r.latitud} lng=${r.longitud}")
                }
                restaurantes.forEach { restaurante ->
                    val lat = restaurante.latitud ?: return@forEach
                    val lng = restaurante.longitud ?: return@forEach

                    Marker(
                        state = MarkerState(position = LatLng(lat, lng)),
                        title = restaurante.nombre,
                        snippet = restaurante.categorias.joinToString(" · "),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                        onClick = {
                            onRestauranteClick(restaurante.id)
                            true
                        }
                    )
                }
            }

            ubicacionCliente?.let { clienteLatLng ->
                val resultados = FloatArray(1)
                val cercano = restaurantes
                    .filter { it.latitud != null && it.longitud != null }
                    .minByOrNull { r ->
                        Location.distanceBetween(
                            clienteLatLng.latitude, clienteLatLng.longitude,
                            r.latitud!!, r.longitud!!, resultados
                        )
                        resultados[0]
                    }
                cercano?.let {
                    Location.distanceBetween(
                        clienteLatLng.latitude, clienteLatLng.longitude,
                        it.latitud!!, it.longitud!!, resultados
                    )
                    Text(
                        text = "Restaurante más cercano: ${it.nombre} — ${"%.0f".format(resultados[0])}m",
                        color = CletaBlanco,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }
            }

            // Mostrar contador de restaurantes cargados
            if (restaurantes.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = CletaNaranja
                )
            }
        }
    }
}