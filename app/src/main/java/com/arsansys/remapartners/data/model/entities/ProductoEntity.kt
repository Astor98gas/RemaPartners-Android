package com.arsansys.remapartners.data.model.entities

import com.arsansys.remapartners.data.model.enums.EEstado
import com.arsansys.remapartners.data.model.enums.EMoneda

class ProductoEntity {

    private val id: String? = null

    private val idUsuario: String? = null

    private val idCategoria: String? = null

    private val imagenes: MutableList<String?>? = null

    private val marca: String? = null

    private val modelo: String? = null

    private val titulo: String? = null

    private val descripcion: String? = null

    private val estado: EEstado? = null

    private val precioCentimos: Int? = null

    private val moneda: EMoneda? = null

    private val stock: Int? = null

    private val fechaCreacion: String? = null

    private val fechaActualizacion: String? = null

    private val fechaPublicacion: String? = null

    private val fechaBaja: String? = null

    private val direccion: String? = null

    private val activo = true

    private val destacado: Boolean? = null

    private val camposCategoria: Array<CampoCategoriaEntity?>? =
        arrayOfNulls<CampoCategoriaEntity>(0)
}