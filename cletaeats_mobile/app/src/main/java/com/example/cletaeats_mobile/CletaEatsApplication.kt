package com.example.cletaeats_mobile

import android.app.Application
import org.osmdroid.config.Configuration                      
import android.preference.PreferenceManager                   

class CletaEatsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar OSMDroid con user-agent y caché
        Configuration.getInstance().load(                      
            this,                                              
            PreferenceManager.getDefaultSharedPreferences(this)
        )                                                      
        Configuration.getInstance().userAgentValue = packageName 
        AppContainer.init(this)
    }
}