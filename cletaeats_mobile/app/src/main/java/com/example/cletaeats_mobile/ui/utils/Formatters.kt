package com.example.cletaeats_mobile.ui.utils

/** Formatea un Double como colones costarricenses: ₡4.000 */
fun Double.toCRC(): String {
    val entero = this.toLong()
    return "₡" + String.format("%,d", entero).replace(",", ".")
}