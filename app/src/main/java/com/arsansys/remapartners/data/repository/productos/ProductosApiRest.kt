package com.arsansys.remapartners.data.repository.productos

import com.arsansys.remapartners.data.model.entities.ProductoEntity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductosApiRest {

    @GET("vendedor/producto/getAll")
    suspend fun getProductos(): Response<List<ProductoEntity>>

    @GET("vendedor/producto/getById/{id}")
    suspend fun getProductoById(@Path("id") id: String): Response<ProductoEntity>

}