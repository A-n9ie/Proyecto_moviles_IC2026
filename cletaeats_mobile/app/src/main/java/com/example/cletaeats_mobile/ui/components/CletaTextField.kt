package com.example.cletaeats_mobile.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.cletaeats.ui.theme.CletaNaranja
import com.example.cletaeats.ui.theme.CletaTextoSecundario
import com.example.cletaeats.ui.theme.CletaBlanco
import androidx.compose.ui.graphics.Color
import com.example.cletaeats.ui.theme.CletaError
import com.example.cletaeats.ui.theme.CletaGrisClaro

/**
 * Campo de texto estándar de CletaEats.
 *
 * Validación dinámica: el error se muestra MIENTRAS el usuario
 * escribe, no solo al hacer submit. Esto cumple el requerimiento
 * del profesor de "validación en el momento".
 *
 * @param error String? Si no es null, muestra el mensaje de error
 *              debajo del campo en rojo con un borde rojo.
 */
@Composable
fun CletaTextField(
    value:        String,
    onValueChange: (String) -> Unit,
    label:        String,
    modifier:     Modifier = Modifier.fillMaxWidth(),
    isPassword:   Boolean  = false,
    error:        String?  = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leadingIcon:  (@Composable () -> Unit)? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        modifier      = modifier,
        isError       = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        leadingIcon     = leadingIcon,
        trailingIcon = if (isPassword) {
            {
                // ImageButton (ícono) para mostrar/ocultar contraseña
                // El profesor pidió ImageButton con íconos, no texto
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Default.Visibility
                        else
                            Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible)
                            "Ocultar contraseña"
                        else
                            "Mostrar contraseña"
                    )
                }
            }
        } else null,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            // ── Estado normal ─────────────────────────────────────────
            focusedTextColor         = CletaBlanco,
            unfocusedTextColor       = CletaBlanco,
            focusedBorderColor       = CletaNaranja,
            unfocusedBorderColor     = CletaGrisClaro,
            focusedLabelColor        = CletaNaranja,
            unfocusedLabelColor      = CletaTextoSecundario,
            cursorColor              = CletaNaranja,
            focusedContainerColor    = Color.Transparent,
            unfocusedContainerColor  = Color.Transparent,

            // ── Estado de error (el texto sigue blanco, el borde/label se vuelven rojos) ──
            errorTextColor           = CletaBlanco,       // texto que escribe el usuario
            errorBorderColor         = CletaError,        // borde del campo
            errorLabelColor          = CletaError,        // la etiqueta "Contraseña"
            errorCursorColor         = CletaError,        // cursor de escritura
            errorContainerColor      = Color.Transparent, // fondo del campo (sin cambio)
            errorSupportingTextColor = CletaError         // mensaje de error debajo
        )
    )
}