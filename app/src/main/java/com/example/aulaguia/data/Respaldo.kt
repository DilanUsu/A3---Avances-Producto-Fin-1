package com.example.aulaguia.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Archivo de respaldo de Aulateca (JSON). Se guarda fuera de la app con el
 * selector de archivos de Android y se puede volver a cargar después.
 * No incluye contraseñas ni datos de la cuenta, solo salones, grupos y notas.
 */
@Serializable
data class Respaldo(
    val app: String = APP,
    val version: Int = VERSION,
    val exportado: String,
    val salones: List<SalonRespaldo> = emptyList(),
    val grupos: List<GrupoRespaldo> = emptyList(),
    val notas: List<NotaRespaldo> = emptyList()
) {
    companion object {
        const val APP = "Aulateca"
        const val VERSION = 1

        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}

/** El id solo sirve para relacionar grupos y notas con su salón dentro del archivo. */
@Serializable
data class SalonRespaldo(
    val id: Int,
    val nombre: String,
    val edificio: String,
    val piso: String,
    val referencia: String,
    val capacidad: Int = 0,
    val equipamiento: String = ""
)

@Serializable
data class GrupoRespaldo(
    val nombre: String,
    val materia: String,
    val salonId: Int = 0,
    val salonNombre: String = "Sin salón",
    val horario: String = "",
    val semestre: String = "",
    val numAlumnos: Int = 0,
    val dias: Int = 0,
    val horaInicio: Int = -1,
    val horaFin: Int = -1
)

@Serializable
data class NotaRespaldo(
    val salonId: Int,
    val contenido: String,
    val fecha: Long
)
