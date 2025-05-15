package com.arsansys.remapartners.data.model.entities

import com.arsansys.remapartners.data.model.enums.EEstado
import com.arsansys.remapartners.data.model.enums.EMoneda

class ProductoEntity {

    internal val id: String? = null

    private val idUsuario: String? = null

    internal val idCategoria: String? = null

    internal val imagenes: MutableList<String?>? = null

    internal val marca: String? = null

    internal val modelo: String? = null

    internal val titulo: String? = null

    internal val descripcion: String? = null

    internal val estado: EEstado? = null

    internal val precioCentimos: Int? = null

    internal val moneda: EMoneda? = null

    internal val stock: Int? = null

    private val fechaCreacion: String? = null

    private val fechaActualizacion: String? = null

    private val fechaPublicacion: String? = null

    private val fechaBaja: String? = null

    internal val direccion: String? = null

    private val activo = true

    internal val destacado: Boolean? = null

    private val camposCategoria: Array<CampoCategoriaEntity?>? =
        arrayOfNulls<CampoCategoriaEntity>(0)

    private val visitas: Long = 0
}