package com.arsansys.remapartners.data.interceptor

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.arsansys.remapartners.data.util.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

// 1. Agregar un interceptor para manejar respuestas 401/403 automáticamente
class AuthInterceptor(private val context: Context) : Interceptor {
    private val sessionManager = SessionManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        sessionManager.fetchAuthToken()?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        val response = chain.proceed(requestBuilder.build())

        // Si obtenemos 401 o 403, el token podría haber expirado
        if (response.code() == 401 || response.code() == 403) {
            sessionManager.clearData()
            // Lanzar un evento para navegar a la pantalla de login
            val intent = Intent("com.arsansys.remapartners.TOKEN_EXPIRED")
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

        return response
    }
}