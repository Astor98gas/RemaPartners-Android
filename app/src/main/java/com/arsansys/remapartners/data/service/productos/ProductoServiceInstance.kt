package com.arsansys.remapartners.data.service

import android.content.Context
import com.arsansys.remapartners.data.repository.productos.ProductosApiRest
import com.arsansys.remapartners.data.repository.user.UserRetrofitInstance
import com.arsansys.remapartners.data.service.productos.ProductoService
import com.arsansys.remapartners.data.service.productos.ProductoServiceImpl

object ProductoServiceInstance {
    private var instance: ProductoService? = null

    fun getInstance(context: Context): ProductoService {
        if (instance == null) {
            val retrofit = UserRetrofitInstance.getRetrofitInstance(context)
            val apiRest = retrofit.create(ProductosApiRest::class.java)
            instance = ProductoServiceImpl(apiRest)
        }
        return instance!!
    }
}