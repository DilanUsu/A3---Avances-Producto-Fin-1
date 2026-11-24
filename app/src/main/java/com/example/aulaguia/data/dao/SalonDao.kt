package com.example.aulaguia.data.dao

import androidx.room.*
import com.example.aulaguia.data.entity.Salon
import kotlinx.coroutines.flow.Flow

/** Todas las consultas filtran por usuarioId: cada cuenta solo ve sus propios salones. */
@Dao
interface SalonDao {

    @Query("SELECT * FROM salones WHERE usuarioId = :usuarioId ORDER BY edificio, nombre")
    fun obtenerTodos(usuarioId: Int): Flow<List<Salon>>

    @Query("SELECT * FROM salones WHERE usuarioId = :usuarioId ORDER BY id")
    suspend fun listaDeUsuario(usuarioId: Int): List<Salon>

    @Query("SELECT * FROM salones WHERE id = :id AND usuarioId = :usuarioId")
    fun observarPorId(id: Int, usuarioId: Int): Flow<Salon?>

    @Query("SELECT * FROM salones WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun obtenerPorId(id: Int, usuarioId: Int): Salon?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(salon: Salon): Long

    @Update
    suspend fun actualizar(salon: Salon)

    @Delete
    suspend fun eliminarSolo(salon: Salon)

    @Query("DELETE FROM notas WHERE salonId = :salonId")
    suspend fun eliminarNotasDeSalon(salonId: Int)

    @Query("UPDATE grupos SET salonId = 0, salonNombre = 'Sin salón' WHERE salonId = :salonId")
    suspend fun desasignarDeGrupos(salonId: Int)

    @Query("UPDATE grupos SET salonNombre = :nombre WHERE salonId = :salonId")
    suspend fun renombrarEnGrupos(salonId: Int, nombre: String)

    /** Borra el salón y sus notas, y deja sus grupos sin salón, todo en una transacción. */
    @Transaction
    suspend fun eliminar(salon: Salon) {
        eliminarNotasDeSalon(salon.id)
        desasignarDeGrupos(salon.id)
        eliminarSolo(salon)
    }

    /** Actualiza el salón y el nombre que muestran sus grupos. */
    @Transaction
    suspend fun actualizarConGrupos(salon: Salon, etiqueta: String) {
        actualizar(salon)
        renombrarEnGrupos(salon.id, etiqueta)
    }

    @Query("DELETE FROM salones WHERE usuarioId = :usuarioId")
    suspend fun borrarDeUsuario(usuarioId: Int)

    @Query("SELECT COUNT(*) FROM salones WHERE usuarioId = :usuarioId")
    fun contarSalones(usuarioId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM salones WHERE usuarioId = :usuarioId")
    suspend fun contarDeUsuario(usuarioId: Int): Int
}
