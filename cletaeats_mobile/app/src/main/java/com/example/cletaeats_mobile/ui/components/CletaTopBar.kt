package com.example.cletaeats_mobile.ui.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.cletaeats.ui.theme.CletaBlanco
import com.example.cletaeats.ui.theme.CletaGrisMedio
import com.example.cletaeats.ui.theme.CletaNaranja

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
    accionDescripcion: String       = ""
) {
    TopAppBar(
        title  = { Text(titulo, color = CletaBlanco) },
        navigationIcon = if (onMenuClick != null) {
            {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector        = Icons.Default.Menu,
                        contentDescription = "Abrir menú",
                        tint               = CletaBlanco
                    )
                }
            }
        } else ({}),
        actions = if (accionIcono != null && onAccionClick != null) {
            {
                IconButton(onClick = onAccionClick) {
                    Icon(
                        imageVector        = accionIcono,
                        contentDescription = accionDescripcion,
                        tint               = CletaBlanco
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