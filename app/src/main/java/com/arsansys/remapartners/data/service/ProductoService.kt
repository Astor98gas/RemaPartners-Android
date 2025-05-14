package com.arsansys.remapartners.data.service

import com.arsansys.remapartners.data.model.entities.ProductoEntity

interface ProductoService {
    suspend fun getProductos(): List<ProductoEntity>
//    suspend fun getProductoById(id: String): ProductoEntity
//    suspend fun addProducto(producto: ProductoEntity): ProductoEntity
//    suspend fun updateProducto(producto: ProductoEntity): ProductoEntity
//    suspend fun deleteProducto(id: String): Boolean
}