package com.example.cletaeats_mobile.domain.model

data class FacturaData(
    val pedidoId:          Int             = 0,
    val estado:            Int             = 0,
    val restauranteNombre: String          = "",
    val clienteNombre:     String          = "",
    val repartidorNombre:  String          = "",
    val items:             List<ItemFactura> = emptyList(),
    val subtotal:          Double          = 0.0,
    val distanciaKm:       Double          = 0.0,
    val costoTransporte:   Double          = 0.0,
    val iva:               Double          = 0.0,
    val total:             Double          = 0.0,
    val fechaCreacion:     String          = ""
)

data class ItemFactura(
    val comboId: Int = 0,
    val comboNombre:    String = "",
    val numeroCombo:    Int    = 0,
    val cantidad:       Int    = 0,
    val precioUnitario: Double = 0.0,
    val subtotalItem:   Double = 0.0
)