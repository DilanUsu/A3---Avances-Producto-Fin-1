package com.example.aulaguia.data.dao

import androidx.room.*
import com.example.aulaguia.data.entity.Nota
import kotlinx.coroutines.flow.Flow

/** Las notas pertenecen a un salón, así que se filtran por el dueño de ese salón. */
@Dao
interface NotaDao {

    @Query("SELECT * FROM notas WHERE salonId = :salonId ORDER BY fecha DESC")
    fun obtenerPorSalon(salonId: Int): Flow<List<Nota>>

    @Query("SELECT n.* FROM notas n INNER JOIN salones s ON n.salonId = s.id WHERE s.usuarioId = :usuarioId ORDER BY n.id")
    suspend fun listaDeUsuario(usuarioId: Int): List<Nota>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(nota: Nota): Long

    @Insert
    suspend fun insertarTodas(notas: List<Nota>)

    @Delete
    suspend fun eliminar(nota: Nota)

    @Query("DELETE FROM notas WHERE salonId IN (SELECT id FROM salones WHERE usuarioId = :usuarioId)")
    suspend fun borrarDeUsuario(usuarioId: Int)

    @Query("SELECT COUNT(*) FROM notas n INNER JOIN salones s ON n.salonId = s.id WHERE s.usuarioId = :usuarioId")
    fun contarNotas(usuarioId: Int): Flow<Int>
}
