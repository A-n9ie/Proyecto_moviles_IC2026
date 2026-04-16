package com.example.habitosapp.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Entidad de dominio pura.
 * @Parcelize permite enviarla entre Activities con Intent.putExtra()
 */
@Parcelize
data class Habito(
    val id: Int = 0,
    val nombre: String = "",
    val descripcion: String = "",
    val idUsuario: Int = 0,
    val idTipo: Int? = null,
    val idEstado: Int? = null,
    val duracionObjetivo: Int? = null
) : Parcelable