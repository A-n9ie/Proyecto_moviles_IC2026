package com.example.cletaeats_mobile.ui.auth


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cletaeats_mobile.ui.components.CletaButton
import com.example.cletaeats_mobile.ui.components.CletaTextField
import com.example.cletaeats_mobile.ui.components.ErrorBanner
import com.example.cletaeats.ui.theme.*
import com.example.cletaeats_mobile.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel:    AuthViewModel,
    onRegistroOk: (rol: String) -> Unit,
    onVolver:     () -> Unit
) {
    var tabSeleccionado by remember { mutableIntStateOf(0) }
    val tabs = listOf("🧑‍💼 Cliente", "🚲 Repartidor")

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onRegistroOk(uiState.rol)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CletaGrisOscuro)
    ) {
        // ── Encabezado ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CletaGrisMedio)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = CletaBlanco
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = "Crear cuenta",
                    color      = CletaBlanco,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ── Tabs CLIENTE / REPARTIDOR ──────────────────────────────
        TabRow(
            selectedTabIndex = tabSeleccionado,
            containerColor   = CletaGrisMedio,
            contentColor     = CletaNaranja
        ) {
            tabs.forEachIndexed { index, titulo ->
                Tab(
                    selected = tabSeleccionado == index,
                    onClick  = {
                        tabSeleccionado = index
                        viewModel.clearError()
                    },
                    text = {
                        Text(
                            titulo,
                            color = if (tabSeleccionado == index) CletaNaranja else CletaTextoSecundario
                        )
                    }
                )
            }
        }

        // ── Contenido según tab ────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            ErrorBanner(uiState.errorMsg)

            if (tabSeleccionado == 0) {
                FormularioCliente(
                    isLoading = uiState.isLoading,
                    onRegistrar = { email, pass, confirmPass, cedula, nombre, direccion, tel, tarjeta, fechaVenc, cvv ->
                        viewModel.registroCliente(
                            email, pass, confirmPass,
                            cedula, nombre, direccion, tel, tarjeta, fechaVenc, cvv
                        )
                    },
                    onCambio = { viewModel.clearError() }
                )
            } else {
                FormularioRepartidor(
                    isLoading = uiState.isLoading,
                    onRegistrar = { email, pass, confirmPass, cedula, nombre, correoContacto, dir, tel, tarjeta ->
                        viewModel.registroRepartidor(
                            email, pass, confirmPass,
                            cedula, nombre, correoContacto, dir, tel, tarjeta
                        )
                    },
                    onCambio = { viewModel.clearError() }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// Formulario CLIENTE
// ══════════════════════════════════════════════════════════════════
@Composable
private fun FormularioCliente(
    isLoading:   Boolean,
    onRegistrar: (email: String, pass: String, confirmPass: String, cedula: String,
                  nombre: String, direccion: String, tel: String, tarjeta: String,
                  fechaVenc: String, cvv: String) -> Unit,
    onCambio:    () -> Unit
) {
    var email       by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var cedula      by remember { mutableStateOf("") }
    var nombre      by remember { mutableStateOf("") }
    var direccion   by remember { mutableStateOf("") }
    var telefono    by remember { mutableStateOf("") }
    var tarjeta     by remember { mutableStateOf("") }
    var fechaVenc   by remember { mutableStateOf("") }
    var cvvReg      by remember { mutableStateOf("") }

    // ── Validaciones dinámicas ────────────────────────────────────
    val emailError    = if (email.isNotEmpty() && !email.contains("@")) "Correo inválido" else null
    val passError     = if (password.isNotEmpty() && password.length < 6) "Mínimo 6 caracteres" else null
    val confirmError  = if (confirmPass.isNotEmpty() && confirmPass != password) "Las contraseñas no coinciden" else null
    val cedulaError   = if (cedula.isNotEmpty() && cedula.length < 9) "Cédula inválida" else null
    val telefonoError = if (telefono.isNotEmpty() && telefono.length < 8) "Teléfono inválido" else null
    val tarjetaError  = if (tarjeta.isNotEmpty() && tarjeta.length < 16) "Debe tener 16 dígitos" else null
    val fechaVencError = if (fechaVenc.isNotEmpty() && fechaVenc.length < 5) "Formato MM/YY" else null
    val cvvRegError   = if (cvvReg.isNotEmpty() && cvvReg.length < 3) "Mínimo 3 dígitos" else null

    val formValido = email.isNotEmpty() && password.isNotEmpty()
            && confirmPass.isNotEmpty() && cedula.isNotEmpty()
            && nombre.isNotEmpty() && direccion.isNotEmpty()
            && telefono.isNotEmpty() && tarjeta.isNotEmpty()
            && fechaVenc.isNotEmpty() && cvvReg.isNotEmpty()
            && listOf(emailError, passError, confirmError, cedulaError, telefonoError,
        tarjetaError, fechaVencError, cvvRegError).all { it == null }

    CampoConIcono(valor = email, onValor = { email = it; onCambio() },
        label = "Correo electrónico", icono = Icons.Default.Email,
        error = emailError, tipo = KeyboardType.Email)

    Spacer(Modifier.height(10.dp))
    CampoConIcono(valor = password, onValor = { password = it; onCambio() },
        label = "Contraseña", icono = Icons.Default.Lock,
        error = passError, tipo = KeyboardType.Password, esPassword = true)

    Spacer(Modifier.height(10.dp))
    CampoConIcono(valor = confirmPass, onValor = { confirmPass = it; onCambio() },
        label = "Confirmar contraseña", icono = Icons.Default.Lock,
        error = confirmError, tipo = KeyboardType.Password, esPassword = true)

    Spacer(Modifier.height(10.dp))
    CampoConIcono(valor = cedula, onValor = { cedula = it; onCambio() },
        label = "Cédula", icono = Icons.Default.Badge,
        error = cedulaError, tipo = KeyboardType.Number)

    Spacer(Modifier.height(10.dp))
    CampoConIcono(valor = nombre, onValor = { nombre = it; onCambio() },
        label = "Nombre completo", icono = Icons.Default.Person)

    Spacer(Modifier.height(10.dp))
    CampoConIcono(valor = direccion, onValor = { direccion = it; onCambio() },
        label = "Dirección exacta", icono = Icons.Default.LocationOn)

    Spacer(Modifier.height(10.dp))
    CampoConIcono(valor = telefono, onValor = { telefono = it; onCambio() },
        label = "Teléfono celular", icono = Icons.Default.Phone,
        error = telefonoError, tipo = KeyboardType.Phone)

    Spacer(Modifier.height(10.dp))
    CampoConIcono(valor = tarjeta, onValor = { if (it.length <= 16 && it.all { c -> c.isDigit() }) { tarjeta = it; onCambio() } },
        label = "Número de tarjeta (16 dígitos)", icono = Icons.Default.CreditCard,
        error = tarjetaError, tipo = KeyboardType.Number)

    Spacer(Modifier.height(10.dp))
    // Fila con fecha y CVV
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CampoConIcono(
            valor = fechaVenc,
            onValor = { input ->
                val digits = input.filter { it.isDigit() }.take(4)
                fechaVenc = if (digits.length >= 3) "${digits.take(2)}/${digits.drop(2)}" else digits
                onCambio()
            },
            label = "Venc. (MM/YY)", icono = Icons.Default.DateRange,
            error = fechaVencError, tipo = KeyboardType.Number,
        )
        Spacer(Modifier.height(10.dp))

        CampoConIcono(
            valor = cvvReg,
            onValor = { if (it.length <= 4 && it.all { c -> c.isDigit() }) { cvvReg = it; onCambio() } },
            label = "CVV", icono = Icons.Default.Lock,
            error = cvvRegError, tipo = KeyboardType.Number,
        )
        Spacer(Modifier.height(10.dp))

    }

    Spacer(Modifier.height(24.dp))

    CletaButton(
        text      = "Crear cuenta",
        onClick   = { onRegistrar(email, password,
            confirmPass, cedula, nombre,
            direccion, telefono,
            tarjeta, fechaVenc, cvvReg) },
        isLoading = isLoading,
        enabled   = formValido,
        icon      = Icons.Default.PersonAdd
    )

    Spacer(Modifier.height(24.dp))
}

// ══════════════════════════════════════════════════════════════════
// Formulario REPARTIDOR
// ══════════════════════════════════════════════════════════════════
@Composable
private fun FormularioRepartidor(
    isLoading:   Boolean,
    onRegistrar: (String, String, String, String, String, String, String, String, String) -> Unit,
    onCambio:    () -> Unit
) {
    var email          by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }
    var confirmPass    by remember { mutableStateOf("") }
    var cedula         by remember { mutableStateOf("") }
    var nombre         by remember { mutableStateOf("") }
    var correoContacto by remember { mutableStateOf("") }
    var direccion      by remember { mutableStateOf("") }
    var telefono       by remember { mutableStateOf("") }
    var tarjeta        by remember { mutableStateOf("") }

    val emailError      = if (email.isNotEmpty() && !email.contains("@")) "Correo inválido" else null
    val passError       = if (password.isNotEmpty() && password.length < 6) "Mínimo 6 caracteres" else null
    val confirmError    = if (confirmPass.isNotEmpty() && confirmPass != password) "Las contraseñas no coinciden" else null
    val cedulaError     = if (cedula.isNotEmpty() && cedula.length < 9) "Cédula inválida" else null
    val contactoError   = if (correoContacto.isNotEmpty() && !correoContacto.contains("@")) "Correo inválido" else null
    val telefonoError   = if (telefono.isNotEmpty() && telefono.length < 8) "Teléfono inválido" else null
    val tarjetaError    = if (tarjeta.isNotEmpty() && tarjeta.length < 4) "Debe tener 4 dígitos" else null

    val formValido = email.isNotEmpty() && password.isNotEmpty()
            && confirmPass.isNotEmpty() && cedula.isNotEmpty()
            && nombre.isNotEmpty() && direccion.isNotEmpty()
            && telefono.isNotEmpty() && tarjeta.isNotEmpty()
            && listOf(emailError, passError, confirmError, cedulaError, contactoError, telefonoError, tarjetaError).all { it == null }

    CampoConIcono(valor = email, onValor = { email = it; onCambio() },
        label = "Correo electrónico", icono = Icons.Default.Email,
        error = emailError, tipo = KeyboardType.Email)

    Spacer(Modifier.height(10.dp))
    CampoConIcono(valor = password, onValor = { password = it; onCambio() },
        label = "Contraseña", icono = Icons.Default.Lock,
        error = passError, tipo = KeyboardType.Password, esPassword = true)

    Spacer(Modifier.height(10.dp))
    CampoConIcono(valor = confirmPass, onValor = { confirmPass = it; onCambio() },
        label = "Confirmar contraseña", icono = Icons.Default.Lock,
        error = confirmError, tipo = KeyboardType.Password, esPassword = true)

    Spacer(Modifier.height(10.dp))
    CampoConIcono(valor = cedula, onValor = { cedula = it; onCambio() },
        label = "Cédula", icono = Icons.Default.Badge,
        error = cedulaError, tipo = KeyboardType.Number)

    Spacer(Modifier.height(10.dp))
    CampoConIcono(valor = nombre, onValor = { nombre = it; onCambio() },
        label = "Nombre completo", icono = Icons.Default.Person)

    Spacer(Modifier.height(10.dp))
    CampoConIcono(valor = correoContacto, onValor = { correoContacto = it; onCambio() },
        label = "Correo de contacto", icono = Icons.Default.AlternateEmail,
        error = contactoError, tipo = KeyboardType.Email)

    Spacer(Modifier.height(10.dp))
    CampoConIcono(valor = direccion, onValor = { direccion = it; onCambio() },
        label = "Dirección exacta", icono = Icons.Default.LocationOn)

    Spacer(Modifier.height(10.dp))
    CampoConIcono(valor = telefono, onValor = { telefono = it; onCambio() },
        label = "Teléfono celular", icono = Icons.Default.Phone,
        error = telefonoError, tipo = KeyboardType.Phone)

    Spacer(Modifier.height(10.dp))
    CampoConIcono(valor = tarjeta, onValor = { tarjeta = it; onCambio() },
        label = "Número de tarjeta (4 dígitos)", icono = Icons.Default.CreditCard,
        error = tarjetaError, tipo = KeyboardType.Number)

    Spacer(Modifier.height(24.dp))

    CletaButton(
        text      = "Registrarme como repartidor",
        onClick   = {
            onRegistrar(
                email, password, confirmPass, cedula, nombre,
                correoContacto.ifEmpty { email }, direccion, telefono, tarjeta
            )
        },
        isLoading = isLoading,
        enabled   = formValido,
        icon      = Icons.Default.DeliveryDining
    )

    Spacer(Modifier.height(24.dp))
}

// ── Helper reutilizable dentro de este archivo ────────────────────
@Composable
private fun CampoConIcono(
    valor:      String,
    onValor:    (String) -> Unit,
    label:      String,
    icono: ImageVector,
    error:      String?          = null,
    tipo:       KeyboardType     = KeyboardType.Text,
    esPassword: Boolean          = false
) {
    CletaTextField(
        value          = valor,
        onValueChange  = onValor,
        label          = label,
        isPassword     = esPassword,
        error          = error,
        keyboardOptions = KeyboardOptions(
            keyboardType = tipo,
            imeAction    = ImeAction.Next
        ),
        leadingIcon = {
            Icon(icono, contentDescription = null, tint = CletaNaranja)
        }
    )
}