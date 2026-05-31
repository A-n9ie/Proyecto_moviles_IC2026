package com.example.cletaeats_mobile.ui.cliente


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.cletaeats_mobile.domain.model.Combo
import com.example.cletaeats.ui.theme.*
import com.example.cletaeats_mobile.ui.utils.toCRC
import com.example.cletaeats_mobile.viewmodel.CarritoViewModel
import com.example.cletaeats_mobile.viewmodel.ComboViewModel

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import android.os.Looper
@Composable
fun CombosScreen(
    restauranteId:    Int,
    restauranteLat:   Double,
    restauranteLng:   Double,
    comboViewModel:   ComboViewModel,
    carritoViewModel: CarritoViewModel,
    onVerCarrito:     () -> Unit,
    onVolver:         () -> Unit
) {
    val comboState   by comboViewModel.uiState.collectAsState()
    val carritoState by carritoViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var permisoUbicacion by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        permisoUbicacion = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    // Cargar combos al entrar a la pantalla
    LaunchedEffect(restauranteId) {
        comboViewModel.cargarCombos(restauranteId)
    }

    // Inicializar carrito con este restaurante cuando ya tengamos el nombre
    LaunchedEffect(comboState.restaurante) {
        comboState.restaurante?.let { r ->
            carritoViewModel.iniciarCarrito(restauranteId, r.nombre)
        }
    }

    // Pedir permiso al entrar a la pantalla
    LaunchedEffect(Unit) {
        launcher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    LaunchedEffect(permisoUbicacion) {
        // Las coordenadas llegan directo por parámetro — no dependen de la carga de combos
        if (restauranteLat == 0.0 && restauranteLng == 0.0) {
            carritoViewModel.fijarDistancia(5.0, tieneGps = false)
            return@LaunchedEffect
        }

        if (!permisoUbicacion) {
            carritoViewModel.fijarDistancia(5.0, tieneGps = false)
            return@LaunchedEffect
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)

            val lastLocation: Location? = withTimeoutOrNull(2_000L) {
                suspendCancellableCoroutine { cont ->
                    fusedClient.lastLocation
                        .addOnSuccessListener { loc -> cont.resume(loc) }
                        .addOnFailureListener { cont.resume(null) }
                }
            }

            if (lastLocation != null) {
                val resultado = FloatArray(1)
                Location.distanceBetween(
                    lastLocation.latitude, lastLocation.longitude,
                    restauranteLat, restauranteLng, resultado
                )
                carritoViewModel.fijarDistancia(
                    (resultado[0] / 1000.0).coerceAtLeast(0.5), tieneGps = true
                )
            } else {
                carritoViewModel.fijarDistancia(5.0, tieneGps = false)

                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L)
                    .setMaxUpdates(1).build()

                val activeLocation: Location? = withTimeoutOrNull(10_000L) {
                    suspendCancellableCoroutine { cont ->
                        val callback = object : LocationCallback() {
                            override fun onLocationResult(result: LocationResult) {
                                fusedClient.removeLocationUpdates(this)
                                cont.resume(result.lastLocation)
                            }
                        }
                        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
                        cont.invokeOnCancellation { fusedClient.removeLocationUpdates(callback) }
                    }
                }

                if (activeLocation != null) {
                    val resultado = FloatArray(1)
                    Location.distanceBetween(
                        activeLocation.latitude, activeLocation.longitude,
                        restauranteLat, restauranteLng, resultado
                    )
                    carritoViewModel.fijarDistancia(
                        (resultado[0] / 1000.0).coerceAtLeast(0.5), tieneGps = true
                    )
                }
            }
        } catch (e: SecurityException) {
            carritoViewModel.fijarDistancia(5.0, tieneGps = false)
        }
    }

    Scaffold(
        topBar = {
            CombosTopBar(
                titulo   = comboState.restaurante?.nombre ?: "Cargando...",
                onVolver = onVolver
            )
        },
        containerColor = CletaGrisOscuro,
        // ── FAB / barra inferior del carrito ──────────────────────
        bottomBar = {
            AnimatedVisibility(visible = !carritoState.estaVacio) {
                CarritoBarra(
                    totalItems = carritoState.totalItems,
                    subtotal   = carritoState.subtotal,
                    onVerCarrito = onVerCarrito
                )
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // ── Cargando ─────────────────────────────────────
                comboState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color    = CletaNaranja
                    )
                }

                // ── Error ────────────────────────────────────────
                comboState.errorMsg != null -> {
                    ErrorConReintento(
                        mensaje   = comboState.errorMsg!!,
                        onReintentar = { comboViewModel.cargarCombos(restauranteId) }
                    )
                }

                // ── Lista de combos ───────────────────────────────
                else -> {

                    LazyColumn(
                        contentPadding      = PaddingValues(
                            start  = 16.dp,
                            end    = 16.dp,
                            top    = 12.dp,
                            // Espacio para que la barra del carrito no tape el último ítem
                            bottom = if (carritoState.estaVacio) 16.dp else 88.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            comboState.restaurante?.let { r ->
                                Text(
                                    text     = "${r.categorias.firstOrNull() ?: "".replaceFirstChar { it.uppercase() }} · ${r.direccion}",
                                    color    = CletaTextoSecundario,
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text       = "${comboState.combos.size} combos disponibles",
                                    color      = CletaBlanco,
                                    fontSize   = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        items(comboState.combos) { combo ->
                            ComboCard(
                                combo               = combo,
                                cantidad            = carritoViewModel.getCantidad(combo.id),
                                productosEliminados = carritoViewModel.getProductosEliminados(combo.id), // ← nuevo
                                onAgregar           = { carritoViewModel.agregarCombo(combo) },
                                onReducir           = { carritoViewModel.reducirCombo(combo.id) },
                                onToggleProducto    = { productoId ->                                    // ← nuevo
                                    carritoViewModel.toggleProductoEnCombo(combo.id, productoId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Card individual de combo ─────────────────────────────────────
@Composable
private fun ComboCard(
    combo:                 Combo,
    cantidad:              Int,
    productosEliminados:   List<Int>,
    onAgregar:             () -> Unit,
    onReducir:             () -> Unit,
    onToggleProducto:      (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CletaGrisMedio),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ── Placeholder de imagen del combo ───────────────────
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CletaNaranja.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${combo.numeroCombo}",
                        color = CletaNaranja,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = combo.nombre,
                        color = CletaBlanco,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (combo.descripcion.isNotEmpty()) {
                        Text(
                            text = combo.descripcion,
                            color = CletaTextoSecundario,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = combo.precio.toCRC(),
                        color = CletaNaranja,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(8.dp))

                // ── Controles + / - (ImageButton con ícono) ───────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (cantidad > 0) {
                        IconButton(
                            onClick = onReducir,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CletaGrisClaro)
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Quitar uno",
                                tint = CletaBlanco,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "$cantidad",
                            color = CletaBlanco,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }

                    IconButton(
                        onClick = onAgregar,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CletaNaranja)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar al carrito",
                            tint = CletaBlanco,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            // ── Productos del combo (nuevo) ───────────────────────
            if (combo.productos.isNotEmpty() && cantidad > 0) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = CletaGrisClaro.copy(alpha = 0.5f))
                Spacer(Modifier.height(8.dp))
                Text(
                    "Personalizar combo:",
                    color = CletaTextoSecundario,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(6.dp))
                combo.productos.forEach { producto ->
                    val eliminado = producto.id in productosEliminados
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleProducto(producto.id) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (eliminado) Icons.Default.CheckBoxOutlineBlank
                            else Icons.Default.CheckBox,
                            contentDescription = null,
                            tint = if (eliminado) CletaTextoSecundario else CletaNaranja,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = producto.nombre,
                            color = if (eliminado) CletaTextoSecundario else CletaBlanco,
                            fontSize = 13.sp,
                            style = if (eliminado)
                                LocalTextStyle.current.copy(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            else LocalTextStyle.current
                        )
                    }
                }
            }
        }
    }
}

// ── Barra inferior del carrito ───────────────────────────────────
@Composable
private fun CarritoBarra(
    totalItems:  Int,
    subtotal:    Double,
    onVerCarrito: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = CletaNaranja,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Badge con cantidad de ítems
                Box(
                    modifier         = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CletaBlanco.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$totalItems", color = CletaBlanco, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Ver carrito",
                        color      = CletaBlanco,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp
                    )
                    Text(
                        subtotal.toCRC(),
                        color    = CletaBlanco.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
            }

            IconButton(onClick = onVerCarrito) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Ir al carrito",
                    tint = CletaBlanco
                )
            }
        }
    }
}

// ── TopBar para esta pantalla ────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CombosTopBar(titulo: String, onVolver: () -> Unit) {
    TopAppBar(
        title           = { Text(titulo, color = CletaBlanco, fontWeight = FontWeight.Bold) },
        navigationIcon  = {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = CletaBlanco)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = CletaGrisMedio)
    )
}

// ── Error con botón de reintento ─────────────────────────────────
@Composable
private fun ErrorConReintento(mensaje: String, onReintentar: () -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.WifiOff, contentDescription = null, tint = CletaNaranja, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(mensaje, color = CletaTextoSecundario, fontSize = 15.sp)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onReintentar, colors = ButtonDefaults.buttonColors(containerColor = CletaNaranja)) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}