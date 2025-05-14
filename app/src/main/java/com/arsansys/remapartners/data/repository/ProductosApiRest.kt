package com.arsansys.remapartners.data.repository

import com.arsansys.remapartners.data.model.entities.ProductoEntity
import retrofit2.Response
import retrofit2.http.GET

interface ProductosApiRest {

    @GET("vendedor/producto/getAll")
    suspend fun getProductos(): Response<List<ProductoEntity>>

//    suspend fun getProductoById(id: String): ProductoEntity
//
//    suspend fun addProducto(producto: ProductoEntity): ProductoEntity
//
//    suspend fun updateProducto(producto: ProductoEntity): ProductoEntity
//
//    suspend fun deleteProducto(id: String): Boolean
}