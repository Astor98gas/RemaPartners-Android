package com.arsansys.remapartners.di


import android.app.Application
import android.util.Log
import androidx.compose.ui.text.intl.Locale
import com.google.firebase.FirebaseApp
import org.koin.core.context.GlobalContext.startKoin
import kotlin.text.format
import java.text.SimpleDateFormat
import java.util.Date

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",
            java.util.Locale.getDefault()).format(Date())
        Log.d("MyApplication", "$currentTime onCreate() called - Starting Firebase initialization")
        FirebaseApp.initializeApp(this)
        Log.d("MyApplication", "$currentTime Firebase initialization completed")
        startKoin {
            modules(AppModule) // Carga el módulo de dependencias
        }
    }
}