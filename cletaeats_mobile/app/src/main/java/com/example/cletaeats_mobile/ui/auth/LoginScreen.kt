package com.example.cletaeats_mobile.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

import com.example.cletaeats_mobile.data.local.DataMode
@Composable
fun LoginScreen(
    viewModel:    AuthViewModel,
    onLoginOk:    (rol: String) -> Unit,
    onIrRegistro: () -> Unit
) {
    // ── Estado local de los campos ────────────────────────────────
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var modoSeleccionado by remember { mutableStateOf(DataMode.API_REMOTA) }

    // ── Validaciones DINÁMICAS (en el momento, sin submit) ────────
    val emailError    = if (email.isNotEmpty() && !email.contains("@"))
        "Correo inválido"
    else null

    val passwordError = if (password.isNotEmpty() && password.length < 6)
        "Mínimo 6 caracteres"
    else null

    // El botón se habilita solo cuando ambos campos son válidos
    val formValido = email.isNotEmpty()
            && password.isNotEmpty()
            && emailError == null
            && passwordError == null

    // ── Estado del ViewModel ──────────────────────────────────────
    val uiState by viewModel.uiState.collectAsState()

    // Navegar cuando el login es exitoso
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginOk(uiState.rol)
    }

    // ── UI ────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CletaGrisOscuro)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))

            // ── Logo / Título ─────────────────────────────────────
            Text(
                text       = "🍔 CletaEats",
                fontSize   = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = CletaNaranja
            )
            Text(
                text     = "Tu comida, a tiempo",
                color    = CletaTextoSecundario,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(48.dp))

            Text(
                text       = "Iniciar sesión",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = CletaBlanco,
                modifier   = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(20.dp))

            // ── Error del servidor ────────────────────────────────
            ErrorBanner(uiState.errorMsg)

            // ── Campo Email ───────────────────────────────────────
            CletaTextField(
                value          = email,
                onValueChange  = {
                    email = it
                    viewModel.clearError()
                },
                label          = "Correo electrónico",
                error          = emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                ),
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = CletaNaranja)
                }
            )

            Spacer(Modifier.height(12.dp))

            // ── Campo Password ────────────────────────────────────
            CletaTextField(
                value          = password,
                onValueChange  = {
                    password = it
                    viewModel.clearError()
                },
                label          = "Contraseña",
                isPassword     = true,
                error          = passwordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                ),
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = CletaNaranja)
                }
            )

            Spacer(Modifier.height(28.dp))

            Text(
                "Modo de datos",
                color    = CletaTextoSecundario,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DataMode.entries.forEach { modo ->
                    val (label, icono) = when (modo) {
                        DataMode.API_REMOTA   -> "Remoto"  to Icons.Default.Wifi
                        DataMode.LOCAL_SQLITE -> "Local"   to Icons.Default.Storage
                        DataMode.CLOUD        -> "Nube"    to Icons.Default.Cloud
                    }
                    FilterChip(
                        selected = modoSeleccionado == modo,
                        onClick  = { modoSeleccionado = modo },
                        label    = { Text(label, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(icono, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        modifier = Modifier.weight(1f),
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor   = CletaNaranja,
                            selectedLabelColor       = CletaBlanco,
                            selectedLeadingIconColor = CletaBlanco,
                            containerColor           = CletaGrisMedio,
                            labelColor               = CletaTextoSecundario
                        )
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            // ── Botón ingresar (ImageButton con ícono + texto) ────
            CletaButton(
                text      = "Ingresar",
                onClick   = { viewModel.login(email, password, modoSeleccionado) },
                isLoading = uiState.isLoading,
                enabled   = formValido,
                icon      = Icons.Default.Login
            )

            Spacer(Modifier.height(16.dp))

            // ── Ir a registro ─────────────────────────────────────
            TextButton(onClick = onIrRegistro) {
                Text(
                    text  = "¿No tenés cuenta? Registrate aquí",
                    color = CletaNaranja
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Hint de prueba (remover en producción) ────────────
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CletaGrisClaro.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Cuentas de prueba:", color = CletaTextoSecundario, fontSize = 12.sp)
                    Text("CLIENTE:     cliente@test.com / 123456", color = CletaTextoSecundario, fontSize = 11.sp)
                    Text("REPARTIDOR:  repartidor@test.com / 123456", color = CletaTextoSecundario, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}