package com.example.cletaeats_mobile.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Producto(
    val id:          Int    = 0,
    val nombre:      String = "",
    val descripcion: String = ""
) : Parcelable