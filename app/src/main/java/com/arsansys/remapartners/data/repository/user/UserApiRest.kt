package com.arsansys.remapartners.data.repository.user

import com.arsansys.remapartners.data.model.dto.UserDto
import com.arsansys.remapartners.data.model.entities.UserEntity
import com.arsansys.remapartners.data.model.firebase.Note
import com.arsansys.remapartners.data.model.login.LoginRequest
import com.arsansys.remapartners.data.model.login.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApiRest {

    @GET("getUsers")
    suspend fun getUsers(): Response<List<UserEntity>>

    @POST("sendNotification")
    suspend fun sendNotification(@Body note: Note?)

    @POST("login")
    suspend fun login(@Body LoginRequest: LoginRequest): Response<LoginResponse>

    @POST("createUser")
    suspend fun createUser(@Body userDto: UserDto): Response<LoginResponse>

}