package com.example.cletaeats_mobile.domain.model

data class ItemCarrito(
    val combo:         Combo,
    val cantidad:      Int    = 1,
    val configuracion: String = "{}"
) {
    /** Subtotal de este ítem: precio × cantidad */
    val subtotal: Double get() = combo.precio * cantidad
}