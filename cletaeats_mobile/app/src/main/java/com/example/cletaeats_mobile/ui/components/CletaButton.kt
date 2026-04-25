package com.example.cletaeats_mobile.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.cletaeats.ui.theme.CletaBlanco
import com.example.cletaeats.ui.theme.CletaNaranja

/**
 * Botón primario de CletaEats.
 * Muestra ProgressIndicator cuando isLoading=true.
 * Soporta ícono vectorial (ImageButton nativo Compose).
 */
@Composable
fun CletaButton(
    text:      String,
    onClick:   () -> Unit,
    modifier:  Modifier = Modifier.fillMaxWidth(),
    isLoading: Boolean  = false,
    enabled:   Boolean  = true,
    icon:      ImageVector? = null
) {
    Button(
        onClick  = onClick,
        enabled  = enabled && !isLoading,
        modifier = modifier.height(52.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = CletaNaranja,
            contentColor   = CletaBlanco
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color    = CletaBlanco,
                strokeWidth = 2.dp
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector         = icon,
                    contentDescription  = null,
                    modifier            = Modifier.size(20.dp)
                )
            }
            Text(
                text     = text,
                modifier = Modifier.run {
                    if (icon != null) this else this
                }
            )
        }
    }
}