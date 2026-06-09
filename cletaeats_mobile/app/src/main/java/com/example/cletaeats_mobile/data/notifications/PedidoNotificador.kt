package com.example.cletaeats_mobile.data.notifications

import com.example.cletaeats_mobile.domain.model.Pedido

/**
 * Detecta cambios de estado entre dos "fotos" de la lista de pedidos y dispara
 * la notificación correcta según el ROL del usuario.
 *
 * DECISIÓN DE DISEÑO:
 * El notificador solo recibe la lista anterior y la nueva, las compara, y delega
 * el "cómo mostrar" al NotificationHelper. Esto mantiene una sola responsabilidad
 * por clase.
 *
 * Guarda internamente el último estado conocido de cada pedido (por id), así que
 * una misma instancia debe vivir mientras la pantalla esté activa.
 */
class PedidoNotificador(
    private val helper: NotificationHelper,
    private val rol: String   // "CLIENTE" o "REPARTIDOR"
) {

    // Recuerda el último estado visto de cada pedido: id -> estado
    private val estadosPrevios = mutableMapOf<Int, Int>()

    // Evita notificar la primera vez (cuando aún no hay nada con qué comparar)
    private var primeraCarga = true

    /**
     * Compara la lista nueva contra lo que recordamos y notifica los cambios.
     */
    fun procesar(pedidos: List<Pedido>) {
        for (pedido in pedidos) {
            val estadoAnterior = estadosPrevios[pedido.id]

            // Solo notificamos si YA conocíamos el pedido y su estado cambió.
            if (!primeraCarga && estadoAnterior != null && estadoAnterior != pedido.estado) {
                notificarCambio(pedido)
            }

            // Detectar pedido NUEVO asignado al repartidor (no estaba antes).
            if (!primeraCarga && estadoAnterior == null && rol == "REPARTIDOR") {
                helper.mostrarNotificacion(
                    id      = pedido.id,
                    titulo  = "Nuevo pedido asignado",
                    mensaje = "Pedido #${pedido.id} de ${pedido.restauranteNombre}"
                )
            }

            estadosPrevios[pedido.id] = pedido.estado
        }
        primeraCarga = false
    }

    private fun notificarCambio(pedido: Pedido) {
        when (rol) {
            "CLIENTE" -> {
                val mensaje = when (pedido.estado) {
                    1 -> "Tu pedido está en preparación 👨‍🍳"
                    2 -> "Tu pedido va en camino 🛵"
                    3 -> "Tu pedido fue entregado ✅"
                    4 -> "Tu pedido fue cancelado"
                    else -> return
                }
                helper.mostrarNotificacion(
                    id      = pedido.id,
                    titulo  = "Pedido #${pedido.id} · ${pedido.restauranteNombre}",
                    mensaje = mensaje
                )
            }
            "REPARTIDOR" -> {
                // Al repartidor le interesa cuando el pedido se cierra.
                val mensaje = when (pedido.estado) {
                    3 -> "Entrega completada ✅"
                    4 -> "Pedido cancelado"
                    else -> return
                }
                helper.mostrarNotificacion(
                    id      = pedido.id,
                    titulo  = "Pedido #${pedido.id}",
                    mensaje = mensaje
                )
            }
        }
    }
}

//Notas del archivo:
/*
- compara estado anterior guardado vs. el nuevo. Solo notifica cuando un pedido que ya conocíamos cambió de estado.
(eso es para evitar que re-notifique lo mismo a cada ratito)
- primeraCarga: la primera vez que se abre la pantalla no se notifica nada.  Solo empieza a notificar a partir de la segunda lectura.
(si no fuera así, llegarían notis de todos los pedidos viejos de golpe)
- el id de la notificación es pedido.id: si el mismo pedido cambia dos veces, la nueva noti reemplaza a la vieja (no se acumulan). Pero con pedidos distintos sí se acumulan.
- diferencia por rol: al cliente le notifica las 4 transiciones (preparando/en camino/entregado/cancelado); al repartidor le notifica los pedidos nuevos asignados y cuando se cierran.
 */