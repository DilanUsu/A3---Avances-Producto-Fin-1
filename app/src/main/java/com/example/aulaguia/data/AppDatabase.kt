package com.example.aulaguia.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.aulaguia.data.dao.GrupoDao
import com.example.aulaguia.data.dao.NotaDao
import com.example.aulaguia.data.dao.SalonDao
import com.example.aulaguia.data.dao.UsuarioDao
import com.example.aulaguia.data.entity.Grupo
import com.example.aulaguia.data.entity.Nota
import com.example.aulaguia.data.entity.Salon
import com.example.aulaguia.data.entity.Usuario

/**
 * Base de datos local Room.
 * Todo se guarda en el teléfono, funciona offline.
 * Los datos persisten entre sesiones de la app.
 */
@Database(
    entities = [Salon::class, Grupo::class, Nota::class, Usuario::class],
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun salonDao(): SalonDao
    abstract fun grupoDao(): GrupoDao
    abstract fun notaDao(): NotaDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun obtenerDatabase(context: Context): AppDatabase =
            INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aulaguia_database"
                )
                    // Las migraciones conservan los datos al actualizar la app
                    .addMigrations(MIGRACION_3_4, MIGRACION_4_5)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                    .also { INSTANCIA = it }
            }
    }
}
