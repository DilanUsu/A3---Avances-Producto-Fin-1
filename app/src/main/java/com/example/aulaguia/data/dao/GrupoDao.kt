package com.example.aulaguia.data.dao

import androidx.room.*
import com.example.aulaguia.data.entity.Grupo
import kotlinx.coroutines.flow.Flow

/** Todas las consultas filtran por usuarioId: cada cuenta solo ve sus propios grupos. */
@Dao
interface GrupoDao {

    @Query("SELECT * FROM grupos WHERE usuarioId = :usuarioId ORDER BY semestre DESC, horaInicio, nombre")
    fun obtenerTodos(usuarioId: Int): Flow<List<Grupo>>

    @Query("SELECT * FROM grupos WHERE usuarioId = :usuarioId ORDER BY id")
    suspend fun listaDeUsuario(usuarioId: Int): List<Grupo>

    @Query("SELECT * FROM grupos WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun obtenerPorId(id: Int, usuarioId: Int): Grupo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(grupo: Grupo): Long

    @Insert
    suspend fun insertarTodos(grupos: List<Grupo>)

    @Update
    suspend fun actualizar(grupo: Grupo)

    @Delete
    suspend fun eliminar(grupo: Grupo)

    @Query("DELETE FROM grupos WHERE usuarioId = :usuarioId")
    suspend fun borrarDeUsuario(usuarioId: Int)

    @Query("SELECT COUNT(*) FROM grupos WHERE usuarioId = :usuarioId")
    fun contarGrupos(usuarioId: Int): Flow<Int>
}
