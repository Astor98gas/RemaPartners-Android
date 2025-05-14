package com.arsansys.remapartners.data.repository

import android.content.Context
import com.arsansys.remapartners.data.model.login.LoginRequest
import com.arsansys.remapartners.data.model.login.LoginResponse
import retrofit2.Response

class AuthRepository(private val context: Context) {

    private val apiService =
        UserRetrofitInstance.getRetrofitInstance(context).create(UserApiRest::class.java)

    suspend fun login(email: String, password: String): Response<LoginResponse> {
        return apiService.login(LoginRequest(email, password))
    }

}