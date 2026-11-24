package com.example.aulaguia.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Notas que un maestro puede agregar sobre un salón.
 * Indexado por salonId y fecha para consultas optimizadas.
 */
@Entity(
    tableName = "notas",
    indices = [
        Index(value = ["salonId"]),
        Index(value = ["fecha"])
    ]
)
data class Nota(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val salonId: Int,          // FK al salón
    val contenido: String,     // Texto de la nota
    val fecha: Long = System.currentTimeMillis() // Timestamp
)
