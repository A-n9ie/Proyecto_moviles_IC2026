package com.example.cletaeats_mobile.ui.repartidor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.cletaeats.ui.theme.*
import com.example.cletaeats_mobile.viewmodel.PerfilRepartidorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilRepartidorScreen(
    viewModel: PerfilRepartidorViewModel,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    var nombre    by remember(state.nombre)    { mutableStateOf(state.nombre) }
    var telefono  by remember(state.telefono)  { mutableStateOf(state.telefono) }
    var direccion by remember(state.direccion) { mutableStateOf(state.direccion) }
    var tarjeta   by remember(state.tarjeta)   { mutableStateOf(state.tarjeta) }
    var editarTarjeta by remember { mutableStateOf(false) }
    var nuevaTarjeta  by remember { mutableStateOf("") }
    var tarjetaError  by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.cargarPerfil() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = CletaBlanco, fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {

            // ── Banner éxito ───────────────────────────────────────────
            if (state.guardadoOk) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CletaNaranja.copy(alpha = 0.15f)),
                        shape  = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = CletaNaranja, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Cambios guardados", color = CletaNaranja, fontSize = 13.sp)
                        }
                    }
                }
            }

            // ── Banner error ───────────────────────────────────────────
            state.errorMsg?.let { msg ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape  = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(msg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        }
                    }
                }
            }

            // ── Información personal ───────────────────────────────────
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = CletaNaranja, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Información personal", color = CletaBlanco, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CletaGrisMedio)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        CampoEditableRep(label = "Nombre completo", valor = nombre, icono = Icons.Default.Person) { nombre = it }
                        Spacer(Modifier.height(12.dp))
                        CampoEditableRep(label = "Teléfono", valor = telefono, icono = Icons.Default.Phone, teclado = KeyboardType.Phone) { telefono = it }
                        Spacer(Modifier.height(12.dp))
                        CampoEditableRep(label = "Dirección", valor = direccion, icono = Icons.Default.Home, lineas = 2) { direccion = it }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.actualizarPerfil(nombre.trim(), telefono.trim(), direccion.trim()) },
                            enabled  = !state.isLoading && nombre.isNotBlank() && telefono.isNotBlank() && direccion.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = CletaNaranja)
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = CletaBlanco, strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                            } else {
                                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                            }
                            Text("Guardar cambios")
                        }
                    }
                }
            }

            // ── Tarjeta de cobro ───────────────────────────────────────
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CreditCard, null, tint = CletaNaranja, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Tarjeta de cobro", color = CletaBlanco, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CletaGrisMedio)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (!editarTarjeta) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CreditCard, null, tint = CletaNaranja, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (state.tarjeta.length >= 4) "•••• •••• •••• ${state.tarjeta.takeLast(4)}"
                                        else if (state.tarjeta.isBlank()) "Sin tarjeta registrada"
                                        else state.tarjeta,
                                        color = CletaBlanco,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Text("Tarjeta de cobro del repartidor", color = CletaTextoSecundario, fontSize = 12.sp)
                                }
                                IconButton(onClick = {
                                    nuevaTarjeta = state.tarjeta
                                    editarTarjeta = true
                                    tarjetaError = ""
                                }) {
                                    Icon(Icons.Default.Edit, null, tint = CletaNaranja)
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = nuevaTarjeta,
                                onValueChange = {
                                    if (it.length <= 19 && it.all { c -> c.isDigit() }) {
                                        nuevaTarjeta = it
                                        tarjetaError = ""
                                    }
                                },
                                label = { Text("Número de tarjeta", color = CletaTextoSecundario) },
                                leadingIcon = { Icon(Icons.Default.CreditCard, null, tint = CletaNaranja) },
                                isError = tarjetaError.isNotBlank(),
                                supportingText = if (tarjetaError.isNotBlank()) {{ Text(tarjetaError) }} else null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = CletaBlanco, unfocusedTextColor = CletaBlanco,
                                    focusedBorderColor = CletaNaranja, unfocusedBorderColor = CletaGrisClaro,
                                    cursorColor = CletaNaranja
                                )
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { editarTarjeta = false },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Cancelar", color = CletaTextoSecundario) }
                                Button(
                                    onClick = {
                                        when {
                                            nuevaTarjeta.isBlank() -> tarjetaError = "Ingresá el número de tarjeta"
                                            nuevaTarjeta.length < 8 -> tarjetaError = "Número demasiado corto"
                                            else -> {
                                                viewModel.actualizarTarjeta(nuevaTarjeta)
                                                editarTarjeta = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CletaNaranja)
                                ) {
                                    Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Guardar")
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun CampoEditableRep(
    label: String, valor: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    teclado: KeyboardType = KeyboardType.Text,
    lineas: Int = 1,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = valor, onValueChange = onChange,
        label = { Text(label, color = CletaTextoSecundario) },
        leadingIcon = { Icon(icono, null, tint = CletaNaranja, modifier = Modifier.size(20.dp)) },
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        minLines = lineas, maxLines = lineas,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = CletaBlanco, unfocusedTextColor = CletaBlanco,
            focusedBorderColor = CletaNaranja, unfocusedBorderColor = CletaGrisClaro,
            cursorColor = CletaNaranja
        )
    )
}