package com.example.aulaguia.navegacion

import kotlinx.serialization.Serializable

/**
 * Rutas de navegación tipadas: el compilador revisa los argumentos,
 * así no hay errores por escribir mal una ruta o un parámetro.
 */
sealed interface Ruta {
    @Serializable data object Login : Ruta
    @Serializable data object Registro : Ruta
    @Serializable data object Inicio : Ruta
    @Serializable data object EditarPerfil : Ruta
    @Serializable data object Grupos : Ruta
    @Serializable data object Salones : Ruta
    @Serializable data object AgregarGrupo : Ruta
    @Serializable data object AgregarSalon : Ruta
    @Serializable data class DetalleSalon(val salonId: Int) : Ruta
    @Serializable data class EditarGrupo(val grupoId: Int) : Ruta
    @Serializable data class EditarSalon(val salonId: Int) : Ruta
}
