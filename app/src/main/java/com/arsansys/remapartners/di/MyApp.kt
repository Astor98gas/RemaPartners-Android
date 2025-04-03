package com.arsansys.remapartners.di


import android.app.Application
import org.koin.core.context.GlobalContext.startKoin

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(AppModule) // Carga el módulo de dependencias
        }
    }
}