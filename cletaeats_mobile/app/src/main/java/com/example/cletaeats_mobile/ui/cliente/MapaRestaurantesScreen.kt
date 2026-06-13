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
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import com.google.android.gms.location.LocationServices
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.views.overlay.Marker as OsmMarker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaRestaurantesScreen(
    restaurantes: List<Restaurante>,
    onRestauranteClick: (Int) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    var ubicacionCliente by remember { mutableStateOf<GeoPoint?>(null) }

    // Referencia al MapView para poder mover la cámara desde LaunchedEffect
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    // Solicitar permiso y obtener ubicación del cliente
    val permisosLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedClient.lastLocation.addOnSuccessListener { location: android.location.Location? ->
                    location?.let {
                        ubicacionCliente = GeoPoint(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {
                android.util.Log.e("MAPA", "Permiso denegado: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        permisosLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Cuando llega ubicación, centrar mapa
    LaunchedEffect(ubicacionCliente) {
        ubicacionCliente?.let { gp ->
            mapViewRef.value?.controller?.animateTo(gp)
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CletaGrisMedio)
            )
        },
        containerColor = CletaGrisOscuro
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Mapa OSMDroid ──────────────────────────────────────
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(13.0)
                        // Centro inicial: Heredia, Costa Rica
                        controller.setCenter(GeoPoint(9.9981, -84.1170))

                        // Overlay de ubicación propia
                        val myLocationOverlay = MyLocationNewOverlay(
                            GpsMyLocationProvider(ctx), this
                        )
                        myLocationOverlay.enableMyLocation()
                        overlays.add(myLocationOverlay)

                        mapViewRef.value = this
                    }
                },
                update = { mapView ->
                    // Limpiar marcadores anteriores (conservar overlay de ubicación)
                    mapView.overlays.removeAll { it is Marker }

                    // Marcador de ubicación del cliente
                    ubicacionCliente?.let { gp ->
                        val marker = Marker(mapView).apply {
                            position = gp
                            title = "Tu ubicación"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mapView.overlays.add(marker)
                    }

                    // Marcadores de restaurantes
                    restaurantes.forEach { restaurante ->
                        val lat = restaurante.latitud ?: return@forEach
                        val lng = restaurante.longitud ?: return@forEach
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(lat, lng)
                            title = restaurante.nombre
                            snippet = restaurante.categorias.joinToString(" · ")
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { _, _ ->
                                onRestauranteClick(restaurante.id)
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }
                    mapView.invalidate()
                    mapViewRef.value = mapView
                }
            )

            // ── Restaurante más cercano ────────────────────────────
            ubicacionCliente?.let { clienteGp ->
                val resultados = FloatArray(1)
                val cercano = restaurantes
                    .filter { it.latitud != null && it.longitud != null }
                    .minByOrNull { r ->
                        android.location.Location.distanceBetween(
                            clienteGp.latitude, clienteGp.longitude,
                            r.latitud!!, r.longitud!!, resultados
                        )
                        resultados[0]
                    }
                cercano?.let {
                    android.location.Location.distanceBetween(
                        clienteGp.latitude, clienteGp.longitude,
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

            if (restaurantes.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = CletaNaranja
                )
            }
        }
    }
}