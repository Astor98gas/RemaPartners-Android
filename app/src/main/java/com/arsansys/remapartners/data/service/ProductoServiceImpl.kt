package com.arsansys.remapartners.data.service

import com.arsansys.remapartners.data.repository.ProductosApiRest

class ProductoServiceImpl(private val productosApiRest: ProductosApiRest) : ProductoService {

    override suspend fun getProductos() = productosApiRest.getProductos().body() ?: emptyList()
}