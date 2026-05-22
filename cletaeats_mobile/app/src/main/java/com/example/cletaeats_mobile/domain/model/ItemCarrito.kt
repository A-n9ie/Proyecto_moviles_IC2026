package com.example.cletaeats_mobile.domain.model

data class ItemCarrito(
    val combo:               Combo          = Combo(),
    val cantidad:            Int            = 1,
    val productosEliminados: List<Int>      = emptyList(), // IDs de productos removidos
    val configuracion:       String         = "{}"
) {
    val subtotal: Double get() = combo.precio * cantidad
    val productosActivos: List<Producto>
        get() = combo.productos.filter { it.id !in productosEliminados }
}