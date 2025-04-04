package com.arsansys.remapartners.data.repository

import com.arsansys.remapartners.data.model.entities.UserEntity
import com.arsansys.remapartners.data.model.firebase.Note
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApiRest {

    @GET("getUsers")
    suspend fun getUsers(): Response<List<UserEntity>>

    @POST("sendNotification")
    suspend fun sendNotification(@Body note: Note?)

}