package com.example.cletaeats_mobile

import android.app.Application

/**
 * Registrada en AndroidManifest: android:name=".CletaEatsApplication"
 * Inicializa AppContainer antes de que cualquier Composable intente usarlo.
 */
class CletaEatsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}