package com.example.cletaeats_mobile.ui.navigation

object AppRoutes {
    const val LOGIN              = "login"
    const val REGISTER           = "register"
    const val RESTAURANTES       = "restaurantes"
    const val COMBOS = "combos/{restauranteId}/{lat}/{lng}"
    fun combosRuta(restauranteId: Int, lat: Double, lng: Double) =
        "combos/$restauranteId/$lat/$lng"
    const val CARRITO            = "carrito"
    const val FACTURA            = "factura"
    const val MIS_PEDIDOS        = "mis_pedidos"
    const val MAPA_RESTAURANTES  = "mapa_restaurantes"
    const val PEDIDOS_REPARTIDOR = "pedidos_repartidor"
    const val SELECCION_MODO     = "seleccion_modo"
    // Helper para construir la ruta con el ID
    fun combosRuta(restauranteId: Int) = "combos/$restauranteId"

    const val RASTREO_REPARTIDOR = "rastreo_repartidor/{pedidoId}"
    fun rastreoRuta(pedidoId: Int) = "rastreo_repartidor/$pedidoId"
}