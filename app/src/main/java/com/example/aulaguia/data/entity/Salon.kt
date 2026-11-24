package com.example.aulaguia.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa un salón/aula física de la escuela.
 * Contiene datos de ubicación y referencia para
 * que cualquier maestro (nuevo o viejo) lo encuentre fácil.
 */
@Entity(tableName = "salones", indices = [Index(value = ["usuarioId"])])
data class Salon(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,          // Ej: "Aula 204", "Lab Cómputo 1"
    val edificio: String,        // Ej: "Edificio A", "Módulo Norte"
    val piso: String,            // Ej: "Planta baja", "2do piso"
    val referencia: String,      // Ej: "Al fondo del pasillo, junto a baños"
    val capacidad: Int = 0,      // Alumnos que caben
    val equipamiento: String = "", // Ej: "Proyector, pizarrón, aire acondicionado"
    @ColumnInfo(defaultValue = "0")
    val usuarioId: Int = 0       // Cuenta dueña del salón
)

/** Nombre que se guarda en los grupos, ej: "Aula 102 (Edificio A)". */
fun Salon.etiqueta(): String = "$nombre ($edificio)"
