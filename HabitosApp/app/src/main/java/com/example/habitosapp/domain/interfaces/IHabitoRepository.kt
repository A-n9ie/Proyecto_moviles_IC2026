package com.example.habitosapp.domain.interfaces

import com.example.habitosapp.domain.Result
import com.example.habitosapp.domain.model.Habito

interface IHabitoRepository {
    fun obtenerHabitos(callback: (Result<List<Habito>>) -> Unit)
    fun crearHabito(nombre: String, descripcion: String, callback: (Result<Habito>) -> Unit)
    fun actualizarHabito(habito: Habito, callback: (Result<Habito>) -> Unit)
    fun eliminarHabito(id: Int, callback: (Result<Unit>) -> Unit)
}