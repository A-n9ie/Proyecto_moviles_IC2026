package com.example.cletaeats_mobile

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.compose.rememberNavController
import com.example.cletaeats_mobile.ui.navigation.AppNavigation
import com.example.cletaeats_mobile.ui.navigation.AppRoutes
import com.example.cletaeats.ui.theme.CletaEatsTheme

class MainActivity : ComponentActivity() {

    // Launcher para pedir el permiso de notificaciones (Android 13+)
    private val permisoNotificaciones = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        Log.d("PERMISO", "Notificaciones concedidas: $concedido")
    }

    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // ── Lifecycle completo (requerido por el lab) ─────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LIFECYCLE", "MainActivity → onCreate")

        // Solicitar permiso de notificaciones al iniciar
        pedirPermisoNotificaciones()

        val session = AppContainer.getSessionManager()

        // Decidir pantalla inicial según sesión activa
        val startDestination = when {
            !session.isLoggedIn()         -> AppRoutes.LOGIN
            session.getRol() == "CLIENTE" -> AppRoutes.RESTAURANTES
            else                          -> AppRoutes.PEDIDOS_REPARTIDOR
        }

        setContent {
            CletaEatsTheme {
                val navController = rememberNavController()
                AppNavigation(
                    navController     = navController,
                    startDestination  = startDestination
                )
            }
        }
    }

    override fun onStart()   { super.onStart();   Log.d("LIFECYCLE", "MainActivity → onStart")   }
    override fun onResume()  { super.onResume();  Log.d("LIFECYCLE", "MainActivity → onResume")  }
    override fun onPause()   { super.onPause();   Log.d("LIFECYCLE", "MainActivity → onPause")   }
    override fun onStop()    { super.onStop();    Log.d("LIFECYCLE", "MainActivity → onStop")    }
    override fun onRestart() { super.onRestart(); Log.d("LIFECYCLE", "MainActivity → onRestart") }
    override fun onDestroy() { super.onDestroy(); Log.d("LIFECYCLE", "MainActivity → onDestroy") }
}