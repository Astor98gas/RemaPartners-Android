package com.arsansys.remapartners.data.service.categorias

import com.arsansys.remapartners.data.model.entities.CategoriaEntity
import com.arsansys.remapartners.data.repository.productos.CategoriaApiRest

class CategoriaServiceImpl(private val categoriaApiRest: CategoriaApiRest) : CategoriaService {
    override suspend fun getCategoriaById(id: String): CategoriaEntity {
        val response = categoriaApiRest.getCategoriaById(id)
        return if (response.isSuccessful) {
            response.body() ?: CategoriaEntity()
        } else {
            CategoriaEntity()
        }
    }
}