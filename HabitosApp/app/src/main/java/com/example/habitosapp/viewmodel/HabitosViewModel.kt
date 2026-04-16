package com.example.habitosapp.viewmodel

import com.example.habitosapp.domain.Result
import com.example.habitosapp.domain.interfaces.IHabitoRepository
import com.example.habitosapp.domain.model.Habito

/**
 * ViewModel manual de hábitos.
 *
 * FUTURO: Igual que AuthViewModel — migrar a AndroidViewModel + LiveData.
 */
class HabitosViewModel(private val repo: IHabitoRepository) {

    fun obtenerHabitos(
        onSuccess: (List<Habito>) -> Unit,
        onError: (String) -> Unit
    ) {
        repo.obtenerHabitos { result ->
            when (result) {
                is Result.Success -> onSuccess(result.data)
                is Result.Error   -> onError(result.message)
            }
        }
    }

    fun crearHabito(
        nombre: String,
        descripcion: String,
        onSuccess: (Habito) -> Unit,
        onError: (String) -> Unit
    ) {
        repo.crearHabito(nombre, descripcion) { result ->
            when (result) {
                is Result.Success -> onSuccess(result.data)
                is Result.Error   -> onError(result.message)
            }
        }
    }

    fun actualizarHabito(
        habito: Habito,
        onSuccess: (Habito) -> Unit,
        onError: (String) -> Unit
    ) {
        repo.actualizarHabito(habito) { result ->
            when (result) {
                is Result.Success -> onSuccess(result.data)
                is Result.Error   -> onError(result.message)
            }
        }
    }

    fun eliminarHabito(
        id: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        repo.eliminarHabito(id) { result ->
            when (result) {
                is Result.Success -> onSuccess()
                is Result.Error   -> onError(result.message)
            }
        }
    }
}