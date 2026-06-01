package com.example.cletaeats_mobile.domain.interfaces

import com.example.cletaeats_mobile.domain.Result
import com.example.cletaeats_mobile.domain.model.PerfilData

interface IPerfilRepository {
    suspend fun obtenerPerfil(): Result<PerfilData>
    suspend fun actualizarPerfil(nombre: String, telefono: String, direccion: String): Result<Unit>
}