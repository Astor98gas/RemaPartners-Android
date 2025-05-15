package com.arsansys.remapartners.data.service.productos

import com.arsansys.remapartners.data.model.entities.ProductoEntity
import com.arsansys.remapartners.data.repository.productos.ProductosApiRest
import com.arsansys.remapartners.data.service.productos.ProductoService

class ProductoServiceImpl(private val productosApiRest: ProductosApiRest) : ProductoService {

    override suspend fun getProductos() = productosApiRest.getProductos().body() ?: emptyList()
    override suspend fun getProductoById(id: String): ProductoEntity {
        val response = productosApiRest.getProductoById(id)
        return if (response.isSuccessful) {
            response.body() ?: ProductoEntity()
        } else {
            ProductoEntity()
        }
    }
}