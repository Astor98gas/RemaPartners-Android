package com.arsansys.remapartners.data.repository.user

import android.content.Context
import com.arsansys.remapartners.data.model.dto.UserDto
import com.arsansys.remapartners.data.model.entities.UserEntity
import com.arsansys.remapartners.data.model.login.LoginRequest
import com.arsansys.remapartners.data.model.login.LoginResponse
import com.arsansys.remapartners.data.repository.RetrofitInstance
import retrofit2.Response

class AuthRepository(private val context: Context) {

    private val apiService =
        RetrofitInstance.getRetrofitInstance(context).create(UserApiRest::class.java)

    suspend fun login(
        username: String,
        password: String,
        googleToken: String
    ): Response<LoginResponse> {
        return apiService.login(LoginRequest(username, password, googleToken))
    }

    suspend fun createUser(userDto: UserDto): Response<LoginResponse> {
        return apiService.createUser(userDto)
    }

}