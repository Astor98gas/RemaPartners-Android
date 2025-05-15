package com.arsansys.remapartners.data.repository.productos

import com.arsansys.remapartners.data.model.entities.CategoriaEntity
import retrofit2.Response
import retrofit2.http.GET

interface CategoriaApiRest {

    @GET("admin/categoria/getById/{id}")
    suspend fun getCategoriaById(id: String): Response<CategoriaEntity>

}