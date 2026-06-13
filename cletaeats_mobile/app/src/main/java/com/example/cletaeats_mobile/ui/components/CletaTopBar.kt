package com.example.cletaeats_mobile.ui.components


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cletaeats.ui.theme.CletaBlanco
import com.example.cletaeats.ui.theme.CletaGrisMedio
import com.example.cletaeats.ui.theme.CletaNaranja
import com.example.cletaeats_mobile.data.local.DataMode

/**
 * TopBar reutilizable con soporte para ícono de NavigationDrawer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CletaTopBar(
    titulo:           String,
    onMenuClick:      (() -> Unit)? = null,
    accionIcono:      ImageVector?  = null,
    onAccionClick:    (() -> Unit)? = null,
    accionDescripcion: String       = "",
    modoActivo: DataMode? = null
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(titulo, color = CletaBlanco)
                modoActivo?.let { ModoBadge(it) }
            }
        },
        navigationIcon = if (onMenuClick != null) {
            {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Abrir menú",
                        tint = CletaBlanco
                    )
                }
            }
        } else ({}),
        actions = if (accionIcono != null && onAccionClick != null) {
            {
                IconButton(onClick = onAccionClick) {
                    Icon(
                        imageVector = accionIcono,
                        contentDescription = accionDescripcion,
                        tint = CletaBlanco
                    )
                }
            }
        } else ({}),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CletaGrisMedio,
            titleContentColor = CletaBlanco
        )
    )
}

@Composable
fun ModoBadge(modo: DataMode) {
    val (icono, color) = when (modo) {
        DataMode.API_REMOTA   -> "🌐" to CletaNaranja
        DataMode.LOCAL_SQLITE -> "💾" to CletaBlanco
        DataMode.CLOUD        -> "☁️" to CletaBlanco
    }
    Spacer(Modifier.width(8.dp))
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.18f)
    ) {
        Text(
            text = icono,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}