package com.example.aulaguia.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.aulaguia.util.Horario

/**
 * Representa un grupo/clase asignado a un maestro.
 * Indexado por salonId y semestre para consultas rápidas.
 */
@Entity(
    tableName = "grupos",
    indices = [
        Index(value = ["salonId"]),
        Index(value = ["semestre"]),
        Index(value = ["usuarioId"])
    ]
)
data class Grupo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,       // Ej: "3A", "5B"
    val materia: String,      // Ej: "Matemáticas I"
    val salonId: Int,         // Salón asignado; 0 = sin salón
    val salonNombre: String,  // Nombre del salón (para mostrar rápido)
    val horario: String,      // Texto que se muestra, ej: "Lun y Mié 08:00 - 10:00"
    val semestre: String,     // Ej: "2025A", "2025B"
    val numAlumnos: Int = 0,  // Cantidad de alumnos en el grupo
    @ColumnInfo(defaultValue = "0")
    val dias: Int = 0,        // Máscara de días: lunes = 1, martes = 2, miércoles = 4...
    @ColumnInfo(defaultValue = "-1")
    val horaInicio: Int = Horario.SIN_HORA, // Minutos desde la medianoche
    @ColumnInfo(defaultValue = "-1")
    val horaFin: Int = Horario.SIN_HORA,
    @ColumnInfo(defaultValue = "0")
    val usuarioId: Int = 0    // Cuenta dueña del grupo
)

/** Días de clase; si el grupo es de una versión anterior se leen del texto del horario. */
fun Grupo.diasDeClase(): Int = if (dias != 0) dias else Horario.diasDesdeTexto(horario)

val Grupo.tieneSalon: Boolean get() = salonId != 0
