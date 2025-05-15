package com.arsansys.remapartners.data.service.categorias

import android.content.Context
import com.arsansys.remapartners.data.repository.productos.CategoriaApiRest
import com.arsansys.remapartners.data.repository.user.RetrofitInstance

object CategoriaServiceInstance {
    private var instance: CategoriaService? = null

    fun getInstance(context: Context): CategoriaService {
        if (instance == null) {
            val retrofit = RetrofitInstance.getRetrofitInstance(context)
            val apiRest = retrofit.create(CategoriaApiRest::class.java)
            instance = CategoriaServiceImpl(apiRest)
        }
        return instance!!
    }
}