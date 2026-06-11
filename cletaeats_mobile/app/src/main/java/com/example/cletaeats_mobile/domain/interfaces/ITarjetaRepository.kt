package com.example.cletaeats_mobile.domain.interfaces

import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.model.Tarjeta

interface ITarjetaRepository {
    suspend fun listarTarjetas(): Result<List<Tarjeta>>
    suspend fun agregarTarjeta(
        numero: String,
        alias: String,
        fechaVencimiento: String = "",
        cvv: String = "",
        esPrincipal: Boolean
    ): Result<Int>
    suspend fun actualizarTarjeta(id: Int, alias: String, fechaVencimiento: String, cvv: String, esPrincipal: Boolean): Result<Unit>
    suspend fun eliminarTarjeta(id: Int): Result<Unit>
}