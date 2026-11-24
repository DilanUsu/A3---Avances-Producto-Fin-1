package com.example.aulaguia.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aulaguia.util.Horario

/**
 * Versión 3 a 4: el horario pasa de ser solo texto a tener días y horas reales.
 * Los grupos que ya existían se convierten leyendo su texto, así no se pierde nada.
 */
val MIGRACION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE grupos ADD COLUMN dias INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE grupos ADD COLUMN horaInicio INTEGER NOT NULL DEFAULT -1")
        db.execSQL("ALTER TABLE grupos ADD COLUMN horaFin INTEGER NOT NULL DEFAULT -1")

        db.query("SELECT id, horario FROM grupos").use { c ->
            while (c.moveToNext()) {
                val texto = c.getString(1).orEmpty()
                val horas = Horario.horasDesdeTexto(texto)
                db.execSQL(
                    "UPDATE grupos SET dias = ?, horaInicio = ?, horaFin = ? WHERE id = ?",
                    arrayOf<Any>(
                        Horario.diasDesdeTexto(texto),
                        horas?.first ?: Horario.SIN_HORA,
                        horas?.second ?: Horario.SIN_HORA,
                        c.getInt(0)
                    )
                )
            }
        }

        // Limpieza de datos que la versión anterior pudo dejar sueltos
        db.execSQL("DELETE FROM notas WHERE salonId NOT IN (SELECT id FROM salones)")
        db.execSQL(
            "UPDATE grupos SET salonId = 0, salonNombre = 'Sin salón' " +
                "WHERE salonId != 0 AND salonId NOT IN (SELECT id FROM salones)"
        )
    }
}

/**
 * Versión 4 a 5: cada salón y grupo ahora pertenece a una cuenta, así los datos de
 * la demo ya no aparecen en las demás cuentas. Lo que ya existía se asigna a la
 * cuenta demo si existe (porque la demo fue la que cargó esos datos) o, si no, a la
 * primera cuenta registrada.
 */
val MIGRACION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE salones ADD COLUMN usuarioId INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE grupos ADD COLUMN usuarioId INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_salones_usuarioId ON salones (usuarioId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_grupos_usuarioId ON grupos (usuarioId)")

        val dueno = """
            COALESCE(
                (SELECT id FROM usuarios WHERE LOWER(email) = 'profesor@aulaguia.com' LIMIT 1),
                (SELECT MIN(id) FROM usuarios),
                0
            )
        """.trimIndent()
        db.execSQL("UPDATE salones SET usuarioId = $dueno")
        db.execSQL("UPDATE grupos SET usuarioId = $dueno")
    }
}
