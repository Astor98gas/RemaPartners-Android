package com.arsansys.remapartners.data.repository

import android.content.Context
import com.arsansys.remapartners.data.interceptor.AuthInterceptor
import com.arsansys.remapartners.data.util.LocalDateTimeAdapter
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime

object RetrofitInstance {
    //    private const val BASE_URL = "http://192.168.1.50:8080/"
    private const val BASE_URL = "http://172.16.55.193:8080/"

    fun getRetrofitInstance(context: Context): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()

        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}