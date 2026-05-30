package com.example.cletaeats_mobile.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cletaeats.ui.theme.*
import com.example.cletaeats_mobile.data.local.DataMode

@Composable
fun SeleccionModoScreen(
    nombreUsuario: String,
    onModoSeleccionado: (DataMode) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CletaGrisOscuro),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text("¡Hola, $nombreUsuario!", fontSize = 22.sp,
                fontWeight = FontWeight.Bold, color = CletaBlanco)
            Spacer(Modifier.height(8.dp))
            Text("¿Cómo querés trabajar hoy?",
                color = CletaTextoSecundario, fontSize = 15.sp)
            Spacer(Modifier.height(36.dp))

            ModoCard(
                titulo = "API Remota",
                descripcion = "Datos en tiempo real desde el servidor",
                icono = Icons.Default.Wifi,
                onClick = { onModoSeleccionado(DataMode.API_REMOTA) }
            )
            Spacer(Modifier.height(16.dp))
            ModoCard(
                titulo = "Local (SQLite)",
                descripcion = "Trabaja sin conexión a Internet",
                icono = Icons.Default.Storage,
                onClick = { onModoSeleccionado(DataMode.LOCAL_SQLITE) }
            )
            Spacer(Modifier.height(16.dp))
            ModoCard(
                titulo = "Cloud (Firebase)",
                descripcion = "Datos sincronizados en la nube",
                icono = Icons.Default.Cloud,
                onClick = { onModoSeleccionado(DataMode.CLOUD) }
            )
        }
    }
}

@Composable
private fun ModoCard(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CletaGrisClaro),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp)
        ) {
            Icon(icono, contentDescription = titulo, tint = CletaNaranja,
                modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(titulo, fontWeight = FontWeight.Bold,
                    color = CletaBlanco, fontSize = 16.sp)
                Text(descripcion, color = CletaTextoSecundario, fontSize = 13.sp)
            }
        }
    }
}