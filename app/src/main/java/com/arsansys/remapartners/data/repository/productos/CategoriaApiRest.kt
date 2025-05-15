package com.arsansys.remapartners.data.repository.productos

import com.arsansys.remapartners.data.model.entities.CategoriaEntity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CategoriaApiRest {

    @GET("admin/categoria/getById/{id}")
    suspend fun getCategoriaById(@Path("id") id: String): Response<CategoriaEntity>

}