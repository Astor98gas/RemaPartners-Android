package com.arsansys.remapartners.data.interceptor

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.arsansys.remapartners.data.util.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    private val sessionManager = SessionManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        // Almacenar si había un token presente en la solicitud
        val token = sessionManager.fetchAuthToken()
        val teniaToken = token != null

        // Si hay token, añadirlo a la cabecera
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        val response = chain.proceed(requestBuilder.build())

        // Solo manejar la expiración si teníamos un token
        if ((response.code == 401 || response.code == 403) && teniaToken) {
            // Eliminar el token caducado de la sesión
            sessionManager.clearAuthToken()
            sessionManager.clearData()

            // Notificar la expiración del token
            val intent = Intent("com.arsansys.remapartners.TOKEN_EXPIRED")
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

        return response
    }
}