package com.arsansys.remapartners.data.repository

import com.arsansys.remapartners.data.model.entities.UserEntity
import retrofit2.Response
import retrofit2.http.GET

interface UserApiRest {

    @GET("getUsers")
    suspend fun getUsers(): Response<List<UserEntity>>

}