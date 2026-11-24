package com.example.aulaguia

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aulaguia.data.AppDatabase
import com.example.aulaguia.data.MIGRACION_3_4
import com.example.aulaguia.data.MIGRACION_4_5
import com.example.aulaguia.data.entity.Grupo
import com.example.aulaguia.data.entity.Nota
import com.example.aulaguia.data.entity.Salon
import com.example.aulaguia.data.entity.Usuario
import com.example.aulaguia.data.entity.etiqueta
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaseDeDatosTest {

    private lateinit var db: AppDatabase

    @get:Rule
    val migraciones = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), AppDatabase::class.java)

    @Before
    fun crear() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java
        ).build()
    }

    @After
    fun cerrar() = db.close()

    private suspend fun salon(usuarioId: Int, nombre: String = "Aula 1") =
        db.salonDao().insertar(Salon(nombre = nombre, edificio = "A", piso = "1", referencia = "x", usuarioId = usuarioId)).toInt()

    private suspend fun grupo(usuarioId: Int, salonId: Int) = db.grupoDao().insertar(
        Grupo(
            nombre = "3A", materia = "Mate", salonId = salonId, salonNombre = "Aula 1 (A)",
            horario = "", semestre = "2026A", usuarioId = usuarioId
        )
    ).toInt()

    @Test
    fun cadaCuentaSoloVeSusDatos() = runBlocking {
        val demo = salon(usuarioId = 1, nombre = "Aula demo")
        grupo(usuarioId = 1, salonId = demo)
        db.notaDao().insertar(Nota(salonId = demo, contenido = "nota demo"))

        // La cuenta 2 no ve nada de la demo
        assertEquals(0, db.salonDao().obtenerTodos(2).first().size)
        assertEquals(0, db.grupoDao().obtenerTodos(2).first().size)
        assertEquals(0, db.notaDao().contarNotas(2).first())
        assertNull(db.salonDao().obtenerPorId(demo, 2))

        assertEquals(1, db.salonDao().obtenerTodos(1).first().size)
        assertEquals(1, db.notaDao().contarNotas(1).first())
    }

    @Test
    fun vaciarSoloBorraLaCuentaIndicada() = runBlocking {
        val s1 = salon(1); grupo(1, s1); db.notaDao().insertar(Nota(salonId = s1, contenido = "a"))
        val s2 = salon(2); grupo(2, s2); db.notaDao().insertar(Nota(salonId = s2, contenido = "b"))

        db.notaDao().borrarDeUsuario(1)
        db.grupoDao().borrarDeUsuario(1)
        db.salonDao().borrarDeUsuario(1)

        assertEquals(0, db.salonDao().contarDeUsuario(1))
        assertEquals(0, db.notaDao().contarNotas(1).first())
        assertEquals(1, db.salonDao().contarDeUsuario(2))
        assertEquals(1, db.grupoDao().obtenerTodos(2).first().size)
        assertEquals(1, db.notaDao().contarNotas(2).first())
    }

    @Test
    fun borrarSalonBorraSusNotasYDesasignaSusGrupos() = runBlocking {
        val salonId = salon(1)
        db.notaDao().insertar(Nota(salonId = salonId, contenido = "nota"))
        val grupoId = grupo(1, salonId)

        db.salonDao().eliminar(db.salonDao().obtenerPorId(salonId, 1)!!)

        assertEquals(0, db.notaDao().contarNotas(1).first())
        val g = db.grupoDao().obtenerPorId(grupoId, 1)!!
        assertEquals(0, g.salonId)
        assertEquals("Sin salón", g.salonNombre)
    }

    @Test
    fun editarSalonActualizaElNombreEnSusGrupos() = runBlocking {
        val salonId = salon(1)
        val grupoId = grupo(1, salonId)

        val editado = db.salonDao().obtenerPorId(salonId, 1)!!.copy(nombre = "Aula 9")
        db.salonDao().actualizarConGrupos(editado, editado.etiqueta())

        assertEquals("Aula 9 (A)", db.grupoDao().obtenerPorId(grupoId, 1)!!.salonNombre)
    }

    @Test
    fun detectaCorreosRepetidosSinImportarMayusculas() = runBlocking {
        val id = db.usuarioDao().insertar(Usuario(nombre = "Ana", email = "ana@correo.com", contrasena = "x")).toInt()
        assertEquals(1, db.usuarioDao().contarConEmail("ANA@correo.com"))
        // El propio usuario no cuenta como repetido al editar su perfil
        assertEquals(0, db.usuarioDao().contarConEmail("ana@correo.com", excluirId = id))
    }

    @Test
    fun migracion3a4ConservaLosGruposYLeeSuHorario() {
        val nombre = "migracion-test"
        migraciones.createDatabase(nombre, 3).apply {
            execSQL("INSERT INTO salones (id, nombre, edificio, piso, referencia, capacidad, equipamiento) VALUES (1, 'Aula', 'A', '1', 'x', 0, '')")
            execSQL(
                "INSERT INTO grupos (id, nombre, materia, salonId, salonNombre, horario, semestre, numAlumnos) " +
                    "VALUES (1, '5A', 'Android', 1, 'Aula (A)', 'Lunes y Miércoles 08:00 - 10:00 AM', '2026A', 30)"
            )
            // Grupo que apunta a un salón que ya no existe y nota huérfana
            execSQL(
                "INSERT INTO grupos (id, nombre, materia, salonId, salonNombre, horario, semestre, numAlumnos) " +
                    "VALUES (2, '3B', 'BD', 99, 'Borrado', 'Viernes', '2026A', 0)"
            )
            execSQL("INSERT INTO notas (id, salonId, contenido, fecha) VALUES (1, 99, 'vieja', 0)")
            close()
        }

        val nueva = migraciones.runMigrationsAndValidate(nombre, 4, true, MIGRACION_3_4)

        nueva.query("SELECT dias, horaInicio, horaFin, numAlumnos FROM grupos WHERE id = 1").use { c ->
            c.moveToFirst()
            assertEquals(1 or 4, c.getInt(0))   // lunes + miércoles
            assertEquals(8 * 60, c.getInt(1))
            assertEquals(10 * 60, c.getInt(2))
            assertEquals(30, c.getInt(3))
        }
        nueva.query("SELECT salonId, salonNombre, dias FROM grupos WHERE id = 2").use { c ->
            c.moveToFirst()
            assertEquals(0, c.getInt(0))
            assertEquals("Sin salón", c.getString(1))
            assertEquals(16, c.getInt(2))       // viernes
        }
        nueva.query("SELECT COUNT(*) FROM notas").use { c ->
            c.moveToFirst()
            assertEquals(0, c.getInt(0))
        }
    }

    @Test
    fun migracion4a5AsignaLosDatosAnterioresALaCuentaDemo() {
        val nombre = "migracion-test-5"
        migraciones.createDatabase(nombre, 4).apply {
            execSQL("INSERT INTO usuarios (id, nombre, email, contrasena, fotoAvatar, fotoUri, estaActivo) VALUES (1, 'Real', 'real@correo.com', 'x', 'maestro_1', NULL, 0)")
            execSQL("INSERT INTO usuarios (id, nombre, email, contrasena, fotoAvatar, fotoUri, estaActivo) VALUES (2, 'Demo', 'profesor@aulaguia.com', 'x', 'maestro_1', NULL, 1)")
            execSQL("INSERT INTO salones (id, nombre, edificio, piso, referencia, capacidad, equipamiento) VALUES (1, 'Aula', 'A', '1', 'x', 0, '')")
            execSQL(
                "INSERT INTO grupos (id, nombre, materia, salonId, salonNombre, horario, semestre, numAlumnos, dias, horaInicio, horaFin) " +
                    "VALUES (1, '5A', 'Android', 1, 'Aula (A)', 'Lun', '2026A', 0, 1, 480, 600)"
            )
            close()
        }

        val nueva = migraciones.runMigrationsAndValidate(nombre, 5, true, MIGRACION_4_5)

        nueva.query("SELECT usuarioId FROM salones").use { c -> c.moveToFirst(); assertEquals(2, c.getInt(0)) }
        nueva.query("SELECT usuarioId FROM grupos").use { c -> c.moveToFirst(); assertEquals(2, c.getInt(0)) }
    }

    @Test
    fun salonInexistenteDevuelveNull() = runBlocking {
        assertNull(db.salonDao().obtenerPorId(12345, 1))
    }
}
