package com.example.cletaeats_mobile.ui.navigation

object AppRoutes {
    const val LOGIN              = "login"
    const val REGISTER           = "register"
    const val RESTAURANTES       = "restaurantes"
    const val COMBOS = "combos/{restauranteId}/{lat}/{lng}/{nombre}"
    fun combosRuta(id: Int, lat: Double?, lng: Double?, nombre: String) =
        "combos/$id/${lat ?: 0.0}/${lng ?: 0.0}/${java.net.URLEncoder.encode(nombre, "UTF-8")}"

    const val CARRITO            = "carrito"
    const val FACTURA            = "factura"
    const val MIS_PEDIDOS        = "mis_pedidos"
    const val MAPA_RESTAURANTES  = "mapa_restaurantes"
    const val PEDIDOS_REPARTIDOR = "pedidos_repartidor"
    const val HISTORIAL_REPARTIDOR = "historial_repartidor"
    const val SELECCION_MODO     = "seleccion_modo"
    const val PERFIL             = "perfil"
    const val MAPA_SEGUIMIENTO   = "mapa_seguimiento/{pedidoId}"
    // Helper para construir la ruta con el ID
    fun combosRuta(restauranteId: Int) = "combos/$restauranteId"
    fun mapaSeguimientoRuta(pedidoId: Int) = "mapa_seguimiento/$pedidoId"
    const val RASTREO_REPARTIDOR = "rastreo_repartidor/{pedidoId}"
    fun rastreoRuta(pedidoId: Int) = "rastreo_repartidor/$pedidoId"
}