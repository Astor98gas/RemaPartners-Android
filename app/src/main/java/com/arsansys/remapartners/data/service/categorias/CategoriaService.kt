package com.arsansys.remapartners.data.service.categorias

import com.arsansys.remapartners.data.model.entities.CategoriaEntity

interface CategoriaService {
    suspend fun getCategoriaById(id: String): CategoriaEntity
}