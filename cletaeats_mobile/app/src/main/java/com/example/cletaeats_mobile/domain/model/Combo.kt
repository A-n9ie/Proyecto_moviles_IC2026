package com.example.cletaeats_mobile.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Combo(
    val id:            Int    = 0,
    val restauranteId: Int    = 0,
    val numeroCombo:   Int    = 0,
    val nombre:        String = "",
    val descripcion:   String = "",
    val precio:        Double = 0.0,
    val imagenUrl:     String = ""
) : Parcelable