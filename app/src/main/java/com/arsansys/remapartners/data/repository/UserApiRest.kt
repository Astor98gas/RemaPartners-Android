package com.arsansys.remapartners.data.repository

import com.arsansys.remapartners.data.model.entities.UserEntity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface UserApiRest {

    @GET("getUsers")
    suspend fun getUsers(): Response<List<UserEntity>>

    @GET("getNotification")
    suspend fun getNotification(@Header("Authorization") token: String): Response<String>

}