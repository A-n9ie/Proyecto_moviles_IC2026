package com.example.cletaeats_mobile.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @Parcelize para pasar restaurante seleccionado entre pantallas
 * sin volver a pedirlo al servidor.
 */
@Parcelize
data class Restaurante(
    val id:         Int             = 0,
    val nombre:     String          = "",
    val categorias: List<String>    = emptyList(),
    val direccion:  String          = "",
    val imagenUrl:  String          = "",
    val estado:     Int             = 1
) : Parcelable