package com.example.habitosapp.domain

/**
 * Wrapper de resultado para todas las operaciones asíncronas del sistema.
 *
 * Uso en ViewModel:
 *   when (result) {
 *       is Result.Success -> mostrarDatos(result.data)
 *       is Result.Error   -> mostrarError(result.message)
 *
 **/
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String)  : Result<Nothing>()
}