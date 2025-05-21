package com.arsansys.remapartners.data.model.entities

import java.time.LocalDateTime

class MensajeEntity(
    val id: String? = null,
    val idEmisor: String? = null,
    val fecha: String = LocalDateTime.now().toString(),
    val mensaje: String? = null,
    val leido: Boolean? = null
)