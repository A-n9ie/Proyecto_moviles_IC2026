package com.example.cletaeats_mobile.ui.navigation

object AppRoutes {
    const val LOGIN              = "login"
    const val REGISTER           = "register"
    const val RESTAURANTES       = "restaurantes"
    const val COMBOS             = "combos/{restauranteId}"     // GET /combos?restaurante=ID
    const val CARRITO            = "carrito"
    const val FACTURA            = "factura"

    const val MIS_PEDIDOS        = "mis_pedidos"

    const val MAPA_RESTAURANTES = "mapa_restaurantes"
    const val PEDIDOS_REPARTIDOR = "pedidos_repartidor"

    // Helper para construir la ruta con el ID
    fun combosRuta(restauranteId: Int) = "combos/$restauranteId"
}