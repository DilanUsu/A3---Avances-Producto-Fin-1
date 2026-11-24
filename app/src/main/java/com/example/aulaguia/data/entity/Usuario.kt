package com.example.aulaguia.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de Usuario guardada localmente con Room.
 * Indexada por email y estaActivo para acelerar el inicio de sesión.
 */
@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["email"], unique = false),
        Index(value = ["estaActivo"])
    ]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val email: String,
    val contrasena: String,
    val fotoAvatar: String = "maestro_1", // Preset avatar identifier
    val fotoUri: String? = null,          // Custom photo URI from gallery if selected
    val estaActivo: Boolean = false       // General session indicator
)
