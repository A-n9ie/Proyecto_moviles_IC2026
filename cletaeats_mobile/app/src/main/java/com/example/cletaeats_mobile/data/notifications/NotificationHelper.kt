package com.example.cletaeats_mobile.data.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Centraliza la creación y el envío de notificaciones locales del sistema.
 *
 * DECISIÓN DE DISEÑO:
 * Usamos notificaciones LOCALES (NotificationManager) disparadas por polling,
 * en lugar de FCM (Firebase Cloud Messaging) con push desde el servidor.
 * Razón: el backend corre en Render free tier.
 * El polling local detecta cambios de estado y emite la notificación nativa,
 * logrando la misma experiencia de usuario sin costo de servidor.
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID   = "cletaeats_pedidos"
        const val CHANNEL_NAME = "Estado de pedidos"
        const val CHANNEL_DESC = "Notificaciones sobre el estado de tus pedidos"
    }

    init {
        crearCanal()
    }

    /**
     * Crea el canal de notificaciones. Obligatorio desde Android 8 (API 26).
     * Llamarlo varias veces no causa problema: el sistema lo ignora si ya existe.
     */
    private fun crearCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    /**
     * Muestra una notificación. El [id] permite que cada pedido tenga su propia
     * notificación (notificaciones con el mismo id se reemplazan, distinto id se apilan).
     */
    fun mostrarNotificacion(id: Int, titulo: String, mensaje: String) {
        // En Android 13+ hay que verificar el permiso antes de notificar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concedido = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!concedido) return  // sin permiso no notificamos (no crashea)
        }

        val notificacion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)  // ícono genérico del sistema
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)  // se cierra al tocarla
            .build()

        NotificationManagerCompat.from(context).notify(id, notificacion)
    }
}

//Notas del archivo:
/*
- El canal (crearCanal): desde Android 8, toda notificación debe pertenecer a un "canal". Sin él, la notificación simplemente no aparece. Se crea una vez en el init.
- mostrarNotificacion: chequea el permiso en Android 13+ y si no está, retorna sin hacer nada (en vez de crashear). Esto es importante: si el usuario rechazó el permiso, la app no debe romperse.
- El id: lo usará como pedido.id, así cada pedido maneja su propia notificación.
- El ícono: por ahora se usa uno genérico del sistema (ic_dialog_info) para no depender de un recurso que quizás no exista. Funciona perfecto; si después se quiere un ícono propio lo podemos cambiar.
*/