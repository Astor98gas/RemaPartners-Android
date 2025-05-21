package com.arsansys.remapartners.data.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
    private val formatter = DateTimeFormatter.ISO_DATE_TIME

    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(formatter.format(value))
        }
    }

    override fun read(reader: JsonReader): LocalDateTime? {
        val dateString = reader.nextString()
        if (dateString.isNullOrEmpty()) {
            return null
        }

        return try {
            LocalDateTime.parse(dateString, formatter)
        } catch (e: DateTimeParseException) {
            try {
                // Intenta con formato alternativo (por si el servidor usa otro formato)
                LocalDateTime.parse(
                    dateString,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                )
            } catch (e: DateTimeParseException) {
                LocalDateTime.now() // Valor por defecto
            }
        }
    }
}