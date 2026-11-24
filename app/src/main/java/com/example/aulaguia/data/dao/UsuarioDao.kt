package com.example.aulaguia.data.dao

import androidx.room.*
import com.example.aulaguia.data.entity.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para gestionar usuarios localmente.
 */
@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE estaActivo = 1 LIMIT 1")
    fun obtenerUsuarioActivo(): Flow<Usuario?>

    @Query("SELECT * FROM usuarios WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun obtenerPorEmail(email: String): Usuario?

    @Query("SELECT COUNT(*) FROM usuarios WHERE LOWER(email) = LOWER(:email) AND id != :excluirId")
    suspend fun contarConEmail(email: String, excluirId: Int = 0): Int

    @Query("SELECT fotoUri FROM usuarios WHERE fotoUri IS NOT NULL")
    suspend fun fotosEnUso(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: Usuario): Long

    @Update
    suspend fun actualizar(usuario: Usuario)

    @Query("UPDATE usuarios SET estaActivo = 0 WHERE estaActivo = 1")
    suspend fun desloguearTodos()

    @Query("UPDATE usuarios SET estaActivo = 1 WHERE id = :usuarioId")
    suspend fun marcarActivo(usuarioId: Int)

    /** Cierra cualquier sesión abierta y activa al usuario indicado en una sola transacción. */
    @Transaction
    suspend fun iniciarSesion(usuarioId: Int) {
        desloguearTodos()
        marcarActivo(usuarioId)
    }
}
