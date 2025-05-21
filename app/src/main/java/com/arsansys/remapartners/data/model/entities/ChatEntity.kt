package com.arsansys.remapartners.data.model.entities

import java.time.LocalDateTime

data class ChatEntity(
    val id: String? = null,
    val idProducto: String? = null,
    val idComprador: String? = null,
    val idVendedor: String? = null,
    val mensajes: MutableList<MensajeEntity?>? = null,
    val fechaCreacion: String = LocalDateTime.now().toString(),
    val ultimaActualizacion: String = LocalDateTime.now().toString(),
    val activo: Boolean? = null
)