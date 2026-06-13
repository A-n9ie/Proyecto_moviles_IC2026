package com.example.cletaeats_mobile.ui.cliente

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.cletaeats.ui.theme.*
import com.example.cletaeats_mobile.domain.model.Tarjeta
import com.example.cletaeats_mobile.viewmodel.PerfilViewModel
import com.example.cletaeats_mobile.viewmodel.TarjetaViewModel
import com.example.cletaeats_mobile.ui.utils.CardDateTransformation
import com.example.cletaeats_mobile.ui.utils.resolveImageUrl
import com.example.cletaeats_mobile.ui.utils.soloDigitosFecha
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
// ─────────────────────────────────────────────────────────────────────────────
// PANTALLA PRINCIPAL DE PERFIL
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    perfilViewModel:  PerfilViewModel,
    tarjetaViewModel: TarjetaViewModel,
    onVolver:         () -> Unit
) {
    val perfilState  by perfilViewModel.uiState.collectAsState()
    val tarjetaState by tarjetaViewModel.uiState.collectAsState()
    val context = LocalContext.current
    // Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { perfilViewModel.subirFotoPerfil(context, it) }
    }

    // Cámara: necesita un URI temporal en cache
    var cameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { perfilViewModel.subirFotoPerfil(context, it) }
        }
    }

    // Permiso de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val tmpFile = java.io.File(context.cacheDir, "foto_perfil_${System.currentTimeMillis()}.jpg")
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context, "${context.packageName}.provider", tmpFile
            )
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    // Diálogo para elegir fuente de imagen
    var mostrarDialogoFoto by remember { mutableStateOf(false) }
    // Campos editables locales — se inicializan con los datos actuales
    var nombre    by remember(perfilState.nombre)    { mutableStateOf(perfilState.nombre) }
    var telefono  by remember(perfilState.telefono)  { mutableStateOf(perfilState.telefono) }
    var direccion by remember(perfilState.direccion) { mutableStateOf(perfilState.direccion) }

    // Estado de diálogos
    var mostrarDialogoTarjeta  by remember { mutableStateOf(false) }
    var mostrarConfirmEliminar by remember { mutableStateOf<Tarjeta?>(null) }
    var mostrarEditarTarjeta by remember { mutableStateOf<Tarjeta?>(null) }

    // Cargar datos al entrar
    LaunchedEffect(Unit) {
        perfilViewModel.cargarPerfil()
        tarjetaViewModel.cargarTarjetas()
    }

    // Resetear campos cuando el servidor confirma la actualización
    LaunchedEffect(perfilState.guardadoOk) {
        if (perfilState.guardadoOk) {
            perfilViewModel.limpiarEstado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mi Perfil", color = CletaBlanco, fontWeight = FontWeight.Bold)
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {

            // ── Avatar + nombre de bienvenida ──────────────────────
            item {
                AvatarHeader(
                    nombre        = perfilState.nombre,
                    email         = perfilState.email,
                    imagenUrl     = resolveImageUrl(perfilState.imagenUrl) ?: "",
                    onCambiarFoto = { mostrarDialogoFoto = true }
                )
            }

            // ── Banner de éxito ────────────────────────────────────
            item {
                AnimatedVisibility(visible = perfilState.guardadoOk) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = CletaNaranja.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CletaNaranja,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Perfil actualizado correctamente",
                                color = CletaNaranja,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // ── Banner de error ────────────────────────────────────
            item {
                AnimatedVisibility(visible = perfilState.errorMsg != null) {
                    perfilState.errorMsg?.let { msg ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(msg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // ── Sección: Información personal ─────────────────────
            item {
                SeccionTitulo(titulo = "Información personal", icono = Icons.Default.Person)
            }

            item {
                Card(
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CletaGrisMedio)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Nombre
                        CampoEditable(
                            label    = "Nombre completo",
                            valor    = nombre,
                            icono    = Icons.Default.DisabledByDefault ?: Icons.Default.Person,
                            onChange = { nombre = it }
                        )
                        Spacer(Modifier.height(12.dp))

                        // Teléfono
                        CampoEditable(
                            label         = "Teléfono",
                            valor         = telefono,
                            icono         = Icons.Default.Phone,
                            teclado       = KeyboardType.Phone,
                            onChange      = { telefono = it }
                        )
                        Spacer(Modifier.height(12.dp))

                        // Dirección
                        CampoEditable(
                            label    = "Dirección",
                            valor    = direccion,
                            icono    = Icons.Default.Home,
                            onChange = { direccion = it },
                            lineas   = 2
                        )
                        Spacer(Modifier.height(16.dp))

                        // Botón guardar
                        Button(
                            onClick = {
                                perfilViewModel.actualizarPerfil(
                                    nombre    = nombre.trim(),
                                    telefono  = telefono.trim(),
                                    direccion = direccion.trim()
                                )
                            },
                            enabled  = !perfilState.isLoading &&
                                       nombre.isNotBlank() &&
                                       telefono.isNotBlank() &&
                                       direccion.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = CletaNaranja)
                        ) {
                            if (perfilState.isLoading) {
                                CircularProgressIndicator(
                                    modifier  = Modifier.size(18.dp),
                                    color     = CletaBlanco,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                            } else {
                                Icon(Icons.Default.Save, contentDescription = null,
                                    modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                            }
                            Text("Guardar cambios")
                        }
                    }
                }
            }

            // ── Sección: Tarjetas ──────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SeccionTitulo(titulo = "Mis tarjetas", icono = Icons.Default.CreditCard)
                    if (!tarjetaState.limiteAlcanzado) {
                        IconButton(
                            onClick = { mostrarDialogoTarjeta = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CletaNaranja)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Agregar tarjeta",
                                tint = CletaBlanco,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Mensaje de error tarjetas
            tarjetaState.errorMsg?.let { msg ->
                item {
                    Text(msg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }

            if (tarjetaState.tarjetas.isEmpty() && !tarjetaState.isLoading) {
                item {
                    Card(
                        shape  = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CletaGrisMedio)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CreditCardOff,
                                    contentDescription = null,
                                    tint = CletaTextoSecundario,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "No tenés tarjetas registradas",
                                    color    = CletaTextoSecundario,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                TextButton(onClick = { mostrarDialogoTarjeta = true }) {
                                    Text("Agregar una tarjeta", color = CletaNaranja)
                                }
                            }
                        }
                    }
                }
            }

            items(tarjetaState.tarjetas) { tarjeta ->
                TarjetaCard(
                    tarjeta    = tarjeta,
                    onEditar   = { mostrarEditarTarjeta = tarjeta },
                    onEliminar = { mostrarConfirmEliminar = tarjeta }
                )
            }

            // ── Indicador límite de tarjetas ───────────────────────
            if (tarjetaState.limiteAlcanzado) {
                item {
                    Text(
                        "Límite de 5 tarjetas alcanzado",
                        color    = CletaTextoSecundario,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Espaciado final
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    // ── Diálogo: Agregar tarjeta ───────────────────────────────────
    if (mostrarDialogoTarjeta) {
        DialogAgregarTarjeta(
            onAgregar = { numero, alias, fechaVenc, cvv, esPrincipal ->
                tarjetaViewModel.agregarTarjeta(numero, alias, fechaVenc, cvv, esPrincipal)
                mostrarDialogoTarjeta = false
            },
            onDismiss = { mostrarDialogoTarjeta = false }
        )
    }

    // ── Diálogo: Confirmar eliminación ─────────────────────────────
    mostrarConfirmEliminar?.let { tarjeta ->
        AlertDialog(
            onDismissRequest = { mostrarConfirmEliminar = null },
            containerColor   = CletaGrisMedio,
            title = {
                Text("Eliminar tarjeta", color = CletaBlanco, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "¿Eliminás la tarjeta **** ${tarjeta.numero.takeLast(4)}?",
                    color = CletaTextoSecundario
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        tarjetaViewModel.eliminarTarjeta(tarjeta.id)
                        mostrarConfirmEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmEliminar = null }) {
                    Text("Cancelar", color = CletaTextoSecundario)
                }
            }
        )
    }
    mostrarEditarTarjeta?.let { tarjeta ->
        EditarTarjetaDialog(
            tarjeta   = tarjeta,
            onGuardar = { alias, fecha, cvv, esPrincipal ->
                tarjetaViewModel.actualizarTarjeta(tarjeta.id, alias, fecha, cvv, esPrincipal)
                mostrarEditarTarjeta = null
            },
            onDismiss = { mostrarEditarTarjeta = null }
        )
    }

    // ── Diálogo: elegir galería o cámara ──────────────────────────
    if (mostrarDialogoFoto) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoFoto = false },
            containerColor   = CletaGrisMedio,
            title = {
                Text("Cambiar foto de perfil", color = CletaBlanco,
                    fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            mostrarDialogoFoto = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = CletaNaranja)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Elegir de la galería")
                    }
                    OutlinedButton(
                        onClick = {
                            mostrarDialogoFoto = false
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = CletaNaranja),
                        border   = BorderStroke(1.dp, CletaNaranja)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Tomar foto")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarDialogoFoto = false }) {
                    Text("Cancelar", color = CletaTextoSecundario)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPONENTES INTERNOS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AvatarHeader(
    nombre: String,
    email: String,
    imagenUrl: String,
    onCambiarFoto: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Box externo que envuelve avatar + ícono cámara
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            // Círculo del avatar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(CletaGrisClaro)
                    .clickable { onCambiarFoto() },
                contentAlignment = Alignment.Center
            ) {
                if (imagenUrl.isNotBlank()) {
                    AsyncImage(
                        model              = imagenUrl,
                        contentDescription = "Foto de perfil",
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint               = CletaNaranja,
                        modifier           = Modifier.size(60.dp)
                    )
                }
            }
            // Ícono cámara en la esquina inferior derecha
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CletaNaranja),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Cambiar foto",
                    tint               = CletaBlanco,
                    modifier           = Modifier.size(14.dp)
                )
            }
        }

        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text       = nombre.ifBlank { "Cargando..." },
                color      = CletaBlanco,
                fontWeight = FontWeight.Bold,
                fontSize   = 18.sp
            )
            Text(
                text     = email,
                color    = CletaTextoSecundario,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun SeccionTitulo(titulo: String, icono: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = CletaNaranja, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text       = titulo,
            color      = CletaBlanco,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 15.sp
        )
    }
}

@Composable
private fun CampoEditable(
    label:   String,
    valor:   String,
    icono:   androidx.compose.ui.graphics.vector.ImageVector,
    teclado: KeyboardType = KeyboardType.Text,
    lineas:  Int = 1,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value         = valor,
        onValueChange = onChange,
        label         = { Text(label, color = CletaTextoSecundario) },
        leadingIcon   = {
            Icon(icono, contentDescription = null, tint = CletaNaranja, modifier = Modifier.size(20.dp))
        },
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        minLines      = lineas,
        maxLines      = lineas,
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(12.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedTextColor     = CletaBlanco,
            unfocusedTextColor   = CletaBlanco,
            focusedBorderColor   = CletaNaranja,
            unfocusedBorderColor = CletaGrisClaro,
            cursorColor          = CletaNaranja
        )
    )
}

@Composable
private fun TarjetaCard(tarjeta: Tarjeta, onEditar: () -> Unit, onEliminar: () -> Unit){
    Card(
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CletaGrisMedio),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono tipo tarjeta
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CletaNaranja.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = CletaNaranja,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Número enmascarado: **** **** **** 1234
                val ultimos4 = tarjeta.numero.takeLast(4)
                Text(
                    text       = "**** **** **** $ultimos4",
                    color      = CletaBlanco,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (tarjeta.alias.isNotBlank()) {
                        Text(tarjeta.alias, color = CletaTextoSecundario, fontSize = 12.sp)
                        Spacer(Modifier.width(8.dp))
                    }
                    if (tarjeta.esPrincipal == 1) {
                        Surface(
                            color = CletaNaranja.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Principal",
                                color    = CletaNaranja,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            // editar
            IconButton(onClick = onEditar) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar tarjeta",
                    tint = CletaNaranja
                )
            }
            // Botón eliminar
            IconButton(onClick = onEliminar) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Eliminar tarjeta",
                    tint = CletaTextoSecundario
                )
            }
        }
    }
}

@Composable
private fun DialogAgregarTarjeta(
    onAgregar: (numero: String, alias: String, fechaVenc: String, cvv: String, esPrincipal: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var numero        by remember { mutableStateOf("") }
    var alias         by remember { mutableStateOf("") }
    var fechaVenc     by remember { mutableStateOf("") }
    var cvv           by remember { mutableStateOf("") }
    var esPrincipal   by remember { mutableStateOf(false) }
    var errorLocal    by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CletaGrisMedio)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                Text(
                    "Agregar tarjeta",
                    color      = CletaBlanco,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp
                )
                Spacer(Modifier.height(16.dp))

                // Número de tarjeta
                OutlinedTextField(
                    value         = numero,
                    onValueChange = {
                        // Solo dígitos, máximo 16
                        if (it.length <= 16 && it.all { c -> c.isDigit() }) {
                            numero = it
                            errorLocal = ""
                        }
                    },
                    label           = { Text("Número de tarjeta (16 dígitos)", color = CletaTextoSecundario) },
                    leadingIcon     = {
                        Icon(Icons.Default.CreditCard, null, tint = CletaNaranja)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine      = true,
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(12.dp),
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedTextColor   = CletaBlanco,
                        unfocusedTextColor = CletaBlanco,
                        focusedBorderColor = CletaNaranja,
                        unfocusedBorderColor = CletaGrisClaro,
                        cursorColor        = CletaNaranja
                    ),
                    // Mostrar número formateado en bloques de 4
                    visualTransformation = androidx.compose.ui.text.input.VisualTransformation { text ->
                        val trimmed = text.text.take(16)
                        val formatted = trimmed.chunked(4).joinToString(" ")
                        androidx.compose.ui.text.input.TransformedText(
                            androidx.compose.ui.text.AnnotatedString(formatted),
                            object : androidx.compose.ui.text.input.OffsetMapping {
                                override fun originalToTransformed(offset: Int): Int {
                                    if (offset <= 0) return 0
                                    val spaces = (offset - 1) / 4
                                    return (offset + spaces).coerceAtMost(formatted.length)
                                }
                                override fun transformedToOriginal(offset: Int): Int {
                                    if (offset <= 0) return 0
                                    val spaces = (offset - 1) / 5
                                    return (offset - spaces).coerceAtMost(trimmed.length)
                                }
                            }
                        )
                    }
                )
                Spacer(Modifier.height(12.dp))

                // Alias
                OutlinedTextField(
                    value         = alias,
                    onValueChange = { if (it.length <= 30) alias = it },
                    label         = { Text("Alias (opcional, ej: Visa personal)", color = CletaTextoSecundario) },
                    leadingIcon   = { Icon(Icons.Default.Label, null, tint = CletaNaranja) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedTextColor   = CletaBlanco,
                        unfocusedTextColor = CletaBlanco,
                        focusedBorderColor = CletaNaranja,
                        unfocusedBorderColor = CletaGrisClaro,
                        cursorColor        = CletaNaranja
                    )
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Fecha MM/YY
                    OutlinedTextField(
                        value = fechaVenc,
                        onValueChange = { input ->
                            fechaVenc = soloDigitosFecha(input)
                        },
                        visualTransformation = CardDateTransformation,
                        label = { Text("Vencimiento (MM/YY)", color = CletaTextoSecundario) },
                        leadingIcon = { Icon(Icons.Default.DateRange, null, tint = CletaNaranja) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CletaBlanco, unfocusedTextColor = CletaBlanco,
                            focusedBorderColor = CletaNaranja, unfocusedBorderColor = CletaGrisClaro,
                            cursorColor = CletaNaranja
                        )
                    )
                    // CVV
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) cvv = it },
                        label = { Text("CVV", color = CletaTextoSecundario) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = CletaNaranja) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CletaBlanco, unfocusedTextColor = CletaBlanco,
                            focusedBorderColor = CletaNaranja, unfocusedBorderColor = CletaGrisClaro,
                            cursorColor = CletaNaranja
                        )
                    )
                }
                Spacer(Modifier.height(8.dp))

                // Switch: marcar como principal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Marcar como principal", color = CletaBlanco, fontSize = 14.sp)
                    Switch(
                        checked         = esPrincipal,
                        onCheckedChange = { esPrincipal = it },
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor  = CletaBlanco,
                            checkedTrackColor  = CletaNaranja
                        )
                    )
                }

                if (errorLocal.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(errorLocal, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = CletaTextoSecundario)
                    ) { Text("Cancelar") }

                    Button(
                        onClick = {
                            when {
                                numero.length < 16 -> { errorLocal = "El número debe tener 16 dígitos"; return@Button }
                                fechaVenc.length < 4 -> { errorLocal = "Fecha de vencimiento inválida (MM/YY)"; return@Button }
                                cvv.length < 3 -> { errorLocal = "CVV debe tener 3 o 4 dígitos"; return@Button }
                            }
                            val fechaFormateada = if (fechaVenc.length >= 3)
                                "${fechaVenc.take(2)}/${fechaVenc.drop(2)}" else fechaVenc
                            onAgregar(numero, alias.trim(), fechaFormateada, cvv, esPrincipal)
                        },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = CletaNaranja)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Agregar")
                    }
                }
            }
        }
    }
}

@Composable
private fun EditarTarjetaDialog(
    tarjeta:   Tarjeta,
    onGuardar: (alias: String, fecha: String, cvv: String, esPrincipal: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var alias       by remember { mutableStateOf(tarjeta.alias) }
    var fechaVenc   by remember { mutableStateOf(tarjeta.fechaVencimiento.replace("/", "")) }
    var cvv         by remember { mutableStateOf(tarjeta.cvv) }
    var esPrincipal by remember { mutableStateOf(tarjeta.esPrincipal == 1) }
    var errorLocal  by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CletaGrisMedio)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                Text(
                    "Editar tarjeta  ···· ${tarjeta.numero.takeLast(4)}",
                    color      = CletaBlanco,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp
                )
                Spacer(Modifier.height(16.dp))

                // Alias
                OutlinedTextField(
                    value         = alias,
                    onValueChange = { if (it.length <= 30) alias = it },
                    label         = { Text("Alias (ej: Visa personal)", color = CletaTextoSecundario) },
                    leadingIcon   = { Icon(Icons.Default.Label, null, tint = CletaNaranja) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedTextColor     = CletaBlanco,
                        unfocusedTextColor   = CletaBlanco,
                        focusedBorderColor   = CletaNaranja,
                        unfocusedBorderColor = CletaGrisClaro,
                        cursorColor          = CletaNaranja
                    )
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Fecha MM/YY
                    OutlinedTextField(
                        value         = fechaVenc,
                        onValueChange = { input ->
                            fechaVenc = soloDigitosFecha(input)
                        },
                        visualTransformation = CardDateTransformation,
                        label           = { Text("Venc. (MM/YY)", color = CletaTextoSecundario) },
                        leadingIcon     = { Icon(Icons.Default.DateRange, null, tint = CletaNaranja) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine      = true,
                        modifier        = Modifier.weight(1f),
                        shape           = RoundedCornerShape(12.dp),
                        colors          = OutlinedTextFieldDefaults.colors(
                            focusedTextColor     = CletaBlanco,
                            unfocusedTextColor   = CletaBlanco,
                            focusedBorderColor   = CletaNaranja,
                            unfocusedBorderColor = CletaGrisClaro,
                            cursorColor          = CletaNaranja
                        )
                    )
                    // CVV
                    OutlinedTextField(
                        value         = cvv,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) cvv = it },
                        label         = { Text("CVV", color = CletaTextoSecundario) },
                        leadingIcon   = { Icon(Icons.Default.Lock, null, tint = CletaNaranja) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine      = true,
                        modifier        = Modifier.weight(0.5f),
                        shape           = RoundedCornerShape(12.dp),
                        colors          = OutlinedTextFieldDefaults.colors(
                            focusedTextColor     = CletaBlanco,
                            unfocusedTextColor   = CletaBlanco,
                            focusedBorderColor   = CletaNaranja,
                            unfocusedBorderColor = CletaGrisClaro,
                            cursorColor          = CletaNaranja
                        )
                    )
                }
                Spacer(Modifier.height(8.dp))

                // Switch principal
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Marcar como principal", color = CletaBlanco, fontSize = 14.sp)
                    Switch(
                        checked         = esPrincipal,
                        onCheckedChange = { esPrincipal = it },
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor = CletaBlanco,
                            checkedTrackColor = CletaNaranja
                        )
                    )
                }

                if (errorLocal.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(errorLocal, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = CletaTextoSecundario)
                    ) { Text("Cancelar") }

                    Button(
                        onClick = {
                            when {
                                fechaVenc.length < 4 -> {
                                    errorLocal = "Fecha inválida (MM/YY)"
                                    return@Button
                                }
                                cvv.length < 3 -> {
                                    errorLocal = "CVV debe tener 3 o 4 dígitos"
                                    return@Button
                                }
                            }
                            val fechaFormateada = if (fechaVenc.length >= 3)
                                "${fechaVenc.take(2)}/${fechaVenc.drop(2)}" else fechaVenc
                            onGuardar(alias.trim(), fechaFormateada, cvv, esPrincipal)
                        },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = CletaNaranja)
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
