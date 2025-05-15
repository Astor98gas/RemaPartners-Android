package com.arsansys.remapartners.data.service.productos

import com.arsansys.remapartners.data.model.entities.ProductoEntity

interface ProductoService {
    suspend fun getProductos(): List<ProductoEntity>
    suspend fun getProductoById(id: String): ProductoEntity
}