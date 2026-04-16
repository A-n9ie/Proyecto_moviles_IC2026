package com.example.habitosapp

import android.app.Application

/**
 * Inicializa AppContainer una sola vez antes de que exista
 * cualquier Activity, garantizando que SessionManager tenga
 * el contexto de aplicación correcto.
 */
class HabitosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}