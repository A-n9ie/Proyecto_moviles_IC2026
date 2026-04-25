package com.example.cletaeats_mobile.ui.repartidor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cletaeats_mobile.AppContainer
import com.example.cletaeats_mobile.ui.components.CletaTopBar
import com.example.cletaeats.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PedidosRepartidorScreen(onLogout: () -> Unit) {
    val session     = AppContainer.getSessionManager()
    val authVM      = remember { AppContainer.authViewModel() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            DrawerRepartidor(
                nombre = session.getNombre(),
                email  = session.getEmail(),
                onCerrarSesion = {
                    scope.launch {
                        drawerState.close()
                        authVM.logout()
                        onLogout()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CletaTopBar(
                    titulo      = "Mis pedidos",
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            containerColor = CletaGrisOscuro
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // Tarjeta de identificación del repartidor
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = CletaGrisMedio)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DeliveryDining,
                            contentDescription = null,
                            tint     = CletaNaranja,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(session.getNombre(), color = CletaBlanco, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(session.getEmail(),  color = CletaTextoSecundario, fontSize = 13.sp)
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = CletaExito.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier              = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(CletaExito, shape = RoundedCornerShape(50))
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Disponible", color = CletaExito, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Estado: sin pedidos asignados (placeholder)
                Icon(
                    Icons.Default.Inbox,
                    contentDescription = null,
                    tint     = CletaTextoSecundario,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Sin pedidos asignados",
                    color      = CletaBlanco,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Cuando te asignen un pedido,\naparecerá aquí.",
                    color     = CletaTextoSecundario,
                    fontSize  = 14.sp
                )

                // TODO (siguiente fase):
                // - Implementar endpoint GET /pedidos/repartidor/{id}
                // - PedidosRepartidorViewModel con StateFlow
                // - LazyColumn con PedidoCard + botón "Marcar entregado"
            }
        }
    }
}

@Composable
private fun DrawerRepartidor(
    nombre:        String,
    email:         String,
    onCerrarSesion: () -> Unit
) {
    ModalDrawerSheet(drawerContainerColor = CletaGrisMedio) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CletaNaranjaOscuro)
                .padding(vertical = 32.dp, horizontal = 20.dp)
        ) {
            Column {
                Icon(Icons.Default.DeliveryDining, contentDescription = null,
                    tint = CletaBlanco, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(8.dp))
                Text(nombre, color = CletaBlanco, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(email,  color = CletaBlanco.copy(alpha = 0.8f), fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Surface(shape = RoundedCornerShape(50), color = CletaBlanco.copy(alpha = 0.2f)) {
                    Text("Repartidor", color = CletaBlanco, fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        NavigationDrawerItem(
            icon     = { Icon(Icons.Default.History, contentDescription = null, tint = CletaNaranja) },
            label    = { Text("Historial de entregas", color = CletaBlanco) },
            selected = false,
            onClick  = { /* TODO fase siguiente */ },
            colors   = NavigationDrawerItemDefaults.colors(unselectedContainerColor = CletaGrisMedio)
        )
        Spacer(Modifier.weight(1f))
        Divider(color = CletaGrisClaro)
        NavigationDrawerItem(
            icon     = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = CletaError) },
            label    = { Text("Cerrar sesión", color = CletaError) },
            selected = false,
            onClick  = onCerrarSesion,
            colors   = NavigationDrawerItemDefaults.colors(unselectedContainerColor = CletaGrisMedio)
        )
        Spacer(Modifier.height(16.dp))
    }
}