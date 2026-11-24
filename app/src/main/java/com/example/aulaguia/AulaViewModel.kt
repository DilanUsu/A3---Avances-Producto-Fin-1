package com.example.aulaguia

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.aulaguia.data.AppDatabase
import com.example.aulaguia.data.GrupoRespaldo
import com.example.aulaguia.data.NotaRespaldo
import com.example.aulaguia.data.Respaldo
import com.example.aulaguia.data.SalonRespaldo
import com.example.aulaguia.data.Seguridad
import com.example.aulaguia.data.entity.Grupo
import com.example.aulaguia.data.entity.Nota
import com.example.aulaguia.data.entity.Salon
import com.example.aulaguia.data.entity.Usuario
import com.example.aulaguia.data.entity.etiqueta
import com.example.aulaguia.ui.componentes.Aviso
import com.example.aulaguia.util.Horario
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Estado de la sesión. [Cargando] evita mostrar el login un instante antes de entrar. */
sealed interface Sesion {
    data object Cargando : Sesion
    data object SinSesion : Sesion
    data class Activa(val usuario: Usuario) : Sesion
}

/**
 * Concentra el acceso a la base de datos. Los Flow se crean una sola vez y se
 * comparten entre pantallas, en lugar de recrearse en cada recomposición.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AulaViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.obtenerDatabase(app)
    private val usuarios = db.usuarioDao()
    private val salonesDao = db.salonDao()
    private val gruposDao = db.grupoDao()
    private val notasDao = db.notaDao()

    private fun <T> Flow<T>.compartir(inicial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), inicial)

    val sesion: StateFlow<Sesion> = usuarios.obtenerUsuarioActivo()
        .map { u -> if (u != null) Sesion.Activa(u) else Sesion.SinSesion }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Sesion.Cargando)

    /** Id de la cuenta con sesión abierta; los datos que se muestran son solo de ella. */
    private val usuarioId: StateFlow<Int?> = sesion
        .map { (it as? Sesion.Activa)?.usuario?.id }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private fun <T> deLaCuenta(inicial: T, consulta: (Int) -> Flow<T>): StateFlow<T> =
        usuarioId.flatMapLatest { id -> if (id == null) flowOf(inicial) else consulta(id) }.compartir(inicial)

    val grupos = deLaCuenta(emptyList()) { gruposDao.obtenerTodos(it) }
    val salones = deLaCuenta(emptyList()) { salonesDao.obtenerTodos(it) }
    val totalGrupos = deLaCuenta(0) { gruposDao.contarGrupos(it) }
    val totalSalones = deLaCuenta(0) { salonesDao.contarSalones(it) }
    val totalNotas = deLaCuenta(0) { notasDao.contarNotas(it) }

    /** Aviso animado de éxito o error que se muestra encima de cualquier pantalla. */
    private val _aviso = MutableStateFlow<Aviso?>(null)
    val aviso = _aviso.asStateFlow()

    fun cerrarAviso() {
        _aviso.value = null
    }

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin = _errorLogin.asStateFlow()

    private val _errorRegistro = MutableStateFlow<String?>(null)
    val errorRegistro = _errorRegistro.asStateFlow()

    private val _errorPerfil = MutableStateFlow<String?>(null)
    val errorPerfil = _errorPerfil.asStateFlow()

    private val _procesando = MutableStateFlow(false)
    val procesando = _procesando.asStateFlow()

    init {
        // Al abrir la app se borran las fotos de perfil que ya nadie usa
        viewModelScope.launch { limpiarFotosSinUso() }
    }

    fun limpiarErrores() {
        _errorLogin.value = null
        _errorRegistro.value = null
        _errorPerfil.value = null
    }

    // ── Sesión ──

    fun login(email: String, contrasena: String, onExito: () -> Unit) = tarea {
        val usuario = usuarios.obtenerPorEmail(email.trim())
        val valida = usuario != null && withContext(Dispatchers.Default) {
            Seguridad.verificar(contrasena, usuario.contrasena)
        }
        if (usuario == null || !valida) {
            _errorLogin.value = "Usuario o contraseña incorrectos"
            return@tarea
        }
        // Si la contraseña venía de la versión anterior sin cifrar, se cifra ahora
        if (!Seguridad.estaCifrada(usuario.contrasena)) {
            val cifrada = withContext(Dispatchers.Default) { Seguridad.cifrar(contrasena) }
            usuarios.actualizar(usuario.copy(contrasena = cifrada))
        }
        _errorLogin.value = null
        usuarios.iniciarSesion(usuario.id)
        onExito()
    }

    fun registrar(
        nombre: String,
        email: String,
        contrasena: String,
        avatarKey: String,
        fotoUri: String?,
        onExito: () -> Unit
    ) = tarea {
        if (usuarios.contarConEmail(email) > 0) {
            _errorRegistro.value = "El correo ya está registrado"
            return@tarea
        }
        val cifrada = withContext(Dispatchers.Default) { Seguridad.cifrar(contrasena) }
        val id = usuarios.insertar(
            Usuario(
                nombre = nombre,
                email = email,
                contrasena = cifrada,
                fotoAvatar = avatarKey,
                fotoUri = fotoUri
            )
        )
        _errorRegistro.value = null
        usuarios.iniciarSesion(id.toInt())
        onExito()
    }

    fun cerrarSesion(onExito: () -> Unit) = lanzar {
        usuarios.desloguearTodos()
        onExito()
    }

    fun guardarPerfil(nombre: String, email: String, avatarKey: String, fotoUri: String?, onExito: () -> Unit) =
        lanzar {
            val u = (sesion.value as? Sesion.Activa)?.usuario ?: return@lanzar
            // Evita que dos cuentas terminen con el mismo correo
            if (usuarios.contarConEmail(email, excluirId = u.id) > 0) {
                _errorPerfil.value = "Ese correo ya lo usa otra cuenta"
                return@lanzar
            }
            _errorPerfil.value = null
            usuarios.actualizar(u.copy(nombre = nombre, email = email, fotoAvatar = avatarKey, fotoUri = fotoUri))
            limpiarFotosSinUso()
            onExito()
        }

    // ── Grupos ──

    suspend fun obtenerGrupo(id: Int): Grupo? = usuarioId.value?.let { gruposDao.obtenerPorId(id, it) }

    fun guardarGrupo(grupo: Grupo, onExito: () -> Unit) = lanzar {
        val uid = usuarioId.value ?: return@lanzar
        val propio = grupo.copy(usuarioId = uid)
        if (propio.id == 0) gruposDao.insertar(propio) else gruposDao.actualizar(propio)
        onExito()
    }

    fun eliminarGrupo(grupo: Grupo) = conAviso("Grupo eliminado", "No se pudo eliminar el grupo") {
        gruposDao.eliminar(grupo)
    }

    // ── Salones y notas ──

    /** null mientras carga; después [Cargado] con el salón, o con null si ya no existe. */
    fun observarSalon(id: Int): Flow<Cargado<Salon?>> =
        salonesDao.observarPorId(id, usuarioId.value ?: -1).map { Cargado(it) }

    suspend fun obtenerSalon(id: Int): Salon? = usuarioId.value?.let { salonesDao.obtenerPorId(id, it) }

    fun notasDeSalon(id: Int): Flow<List<Nota>> = notasDao.obtenerPorSalon(id)

    fun guardarSalon(salon: Salon, onExito: () -> Unit) = lanzar {
        val uid = usuarioId.value ?: return@lanzar
        val propio = salon.copy(usuarioId = uid)
        if (propio.id == 0) salonesDao.insertar(propio) else salonesDao.actualizarConGrupos(propio, propio.etiqueta())
        onExito()
    }

    fun eliminarSalon(salon: Salon) = conAviso("Salón eliminado", "No se pudo eliminar el salón") {
        salonesDao.eliminar(salon)
    }

    fun agregarNota(salonId: Int, texto: String) = lanzar {
        val uid = usuarioId.value ?: return@lanzar
        // Solo se puede escribir en salones de la propia cuenta
        if (salonesDao.obtenerPorId(salonId, uid) != null) {
            notasDao.insertar(Nota(salonId = salonId, contenido = texto))
        }
    }

    fun eliminarNota(nota: Nota) = conAviso("Nota eliminada", "No se pudo eliminar la nota") {
        notasDao.eliminar(nota)
    }

    // ── Vaciar, exportar e importar ──

    /** Borra todos los salones, grupos y notas de la cuenta, por ejemplo al terminar el semestre. */
    fun vaciarTodo() = conAviso("Todo se borró correctamente", "No se pudo vaciar la información") {
        val uid = usuarioId.value ?: error("Sin sesión")
        db.withTransaction {
            notasDao.borrarDeUsuario(uid)
            gruposDao.borrarDeUsuario(uid)
            salonesDao.borrarDeUsuario(uid)
        }
    }

    /** Escribe el respaldo en el archivo que el usuario eligió fuera de la app. */
    fun exportar(destino: Uri) = conAviso("Respaldo guardado", "No se pudo guardar el respaldo") {
        val uid = usuarioId.value ?: error("Sin sesión")
        val respaldo = Respaldo(
            exportado = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            salones = salonesDao.listaDeUsuario(uid).map {
                SalonRespaldo(it.id, it.nombre, it.edificio, it.piso, it.referencia, it.capacidad, it.equipamiento)
            },
            grupos = gruposDao.listaDeUsuario(uid).map {
                GrupoRespaldo(
                    it.nombre, it.materia, it.salonId, it.salonNombre, it.horario, it.semestre,
                    it.numAlumnos, it.dias, it.horaInicio, it.horaFin
                )
            },
            notas = notasDao.listaDeUsuario(uid).map { NotaRespaldo(it.salonId, it.contenido, it.fecha) }
        )
        val texto = Respaldo.json.encodeToString(Respaldo.serializer(), respaldo)
        withContext(Dispatchers.IO) {
            getApplication<Application>().contentResolver.openOutputStream(destino, "wt")
                ?.bufferedWriter()?.use { it.write(texto) }
                ?: error("No se pudo abrir el archivo")
        }
    }

    /**
     * Carga un respaldo y reemplaza los datos actuales de la cuenta.
     * Todo pasa en una transacción: si el archivo tiene un error, no se borra nada.
     */
    fun importar(origen: Uri) = conAviso("Respaldo cargado", "El archivo no es un respaldo válido de Aulateca") {
        val uid = usuarioId.value ?: error("Sin sesión")
        val texto = withContext(Dispatchers.IO) {
            getApplication<Application>().contentResolver.openInputStream(origen)
                ?.bufferedReader()?.use { it.readText() }
                ?: error("No se pudo leer el archivo")
        }
        val respaldo = Respaldo.json.decodeFromString(Respaldo.serializer(), texto)
        require(respaldo.app == Respaldo.APP) { "No es un respaldo de Aulateca" }

        db.withTransaction {
            notasDao.borrarDeUsuario(uid)
            gruposDao.borrarDeUsuario(uid)
            salonesDao.borrarDeUsuario(uid)

            // Los salones reciben ids nuevos; se guarda la equivalencia para grupos y notas
            val nuevosIds = respaldo.salones.associate { s ->
                s.id to salonesDao.insertar(
                    Salon(
                        nombre = s.nombre, edificio = s.edificio, piso = s.piso, referencia = s.referencia,
                        capacidad = s.capacidad, equipamiento = s.equipamiento, usuarioId = uid
                    )
                ).toInt()
            }
            gruposDao.insertarTodos(respaldo.grupos.map { g ->
                val salonId = nuevosIds[g.salonId] ?: 0
                Grupo(
                    nombre = g.nombre, materia = g.materia, salonId = salonId,
                    salonNombre = if (salonId == 0) "Sin salón" else g.salonNombre,
                    horario = g.horario, semestre = g.semestre, numAlumnos = g.numAlumnos,
                    dias = g.dias, horaInicio = g.horaInicio, horaFin = g.horaFin, usuarioId = uid
                )
            })
            notasDao.insertarTodas(respaldo.notas.mapNotNull { n ->
                nuevosIds[n.salonId]?.let { Nota(salonId = it, contenido = n.contenido, fecha = n.fecha) }
            })
        }
    }

    /** Ejecuta una operación y muestra la palomita si sale bien o la tache si falla. */
    private fun conAviso(exito: String, fallo: String, bloque: suspend () -> Unit) {
        viewModelScope.launch {
            _aviso.value = try {
                bloque()
                Aviso(exito = true, mensaje = exito)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Aviso(exito = false, mensaje = fallo)
            }
        }
    }

    // ── Demo ──

    fun cargarDemo(onExito: () -> Unit) = tarea {
        cargarDatosDemo()
        _errorLogin.value = null
        onExito()
    }

    /** Borra de la memoria interna las fotos que ningún usuario tiene asignadas. */
    private suspend fun limpiarFotosSinUso() {
        val enUso = usuarios.fotosEnUso().toSet()
        withContext(Dispatchers.IO) {
            File(getApplication<Application>().filesDir, "fotos").listFiles()?.forEach { archivo ->
                if (android.net.Uri.fromFile(archivo).toString() !in enUso) archivo.delete()
            }
        }
    }

    private fun lanzar(bloque: suspend () -> Unit) {
        viewModelScope.launch { bloque() }
    }

    /** Ejecuta una operación evitando dobles toques mientras se procesa. */
    private fun tarea(bloque: suspend () -> Unit) {
        if (_procesando.value) return
        _procesando.value = true
        viewModelScope.launch {
            try {
                bloque()
            } finally {
                _procesando.value = false
            }
        }
    }

    /** Carga un usuario y datos ficticios de demostración en una sola transacción. */
    private suspend fun cargarDatosDemo() {
        val cifrada = withContext(Dispatchers.Default) { Seguridad.cifrar("123456") }
        db.withTransaction {
            val demo = usuarios.obtenerPorEmail(EMAIL_DEMO)
            val userId = demo?.id ?: usuarios.insertar(
                Usuario(
                    nombre = "Prof. Alejandro Ruiz",
                    email = EMAIL_DEMO,
                    contrasena = cifrada,
                    fotoAvatar = "maestro_1"
                )
            ).toInt()
            usuarios.iniciarSesion(userId)

            // Los datos de ejemplo pertenecen solo a la cuenta demo
            if (salonesDao.contarDeUsuario(userId) > 0) return@withTransaction

            val s1 = salonesDao.insertar(
                Salon(
                    usuarioId = userId,
                    nombre = "Aula 102",
                    edificio = "Edificio A",
                    piso = "Planta Baja",
                    referencia = "Pasillo principal, a la izquierda de la biblioteca",
                    capacidad = 35,
                    equipamiento = "Proyector HD, Aire acondicionado, Pizarra blanca"
                )
            ).toInt()
            val s2 = salonesDao.insertar(
                Salon(
                    usuarioId = userId,
                    nombre = "Lab Cómputo 2",
                    edificio = "Edificio B",
                    piso = "2do Piso",
                    referencia = "Frente a escaleras principales del Edificio B",
                    capacidad = 25,
                    equipamiento = "25 Computadoras i7, Red LAN, Pizarra interactiva"
                )
            ).toInt()
            val s3 = salonesDao.insertar(
                Salon(
                    usuarioId = userId,
                    nombre = "Auditorio Magistral",
                    edificio = "Edificio Central",
                    piso = "1er Piso",
                    referencia = "Junto a la explanada principal de rectoría",
                    capacidad = 120,
                    equipamiento = "Sistema de sonido ambiental, Micrófonos inalámbricos, Proyector 4K"
                )
            ).toInt()

            gruposDao.insertarTodos(
                listOf(
                    Grupo(
                        usuarioId = userId,
                        nombre = "Grupo 5A", materia = "Desarrollo Móvil Android", salonId = s1,
                        salonNombre = "Aula 102 (Edificio A)", semestre = "2026A", numAlumnos = 32,
                        horario = Horario.texto(LUN or MIE, 8 * 60, 10 * 60),
                        dias = LUN or MIE, horaInicio = 8 * 60, horaFin = 10 * 60
                    ),
                    Grupo(
                        usuarioId = userId,
                        nombre = "Grupo 3B", materia = "Bases de Datos Avanzadas", salonId = s2,
                        salonNombre = "Lab Cómputo 2 (Edificio B)", semestre = "2026A", numAlumnos = 24,
                        horario = Horario.texto(MAR or JUE, 10 * 60, 12 * 60),
                        dias = MAR or JUE, horaInicio = 10 * 60, horaFin = 12 * 60
                    ),
                    Grupo(
                        usuarioId = userId,
                        nombre = "Grupo 4C", materia = "Sistemas Operativos y Redes", salonId = s3,
                        salonNombre = "Auditorio Magistral (Edificio Central)", semestre = "2026A", numAlumnos = 45,
                        horario = Horario.texto(VIE, 14 * 60, 18 * 60),
                        dias = VIE, horaInicio = 14 * 60, horaFin = 18 * 60
                    )
                )
            )

            notasDao.insertarTodas(
                listOf(
                    Nota(salonId = s1, contenido = "El proyector requiere adaptador HDMI. El control remoto blanco está en la mesa docente."),
                    Nota(salonId = s1, contenido = "El aire acondicionado tarda 5 minutos en enfriar la habitación al encenderlo."),
                    Nota(salonId = s2, contenido = "Solicitar la llave del laboratorio en coordinación académica 10 minutos antes."),
                    Nota(salonId = s3, contenido = "Probar los micrófonos inalámbricos y sonido 15 minutos antes de la conferencia.")
                )
            )
        }
    }

    private companion object {
        const val EMAIL_DEMO = "profesor@aulaguia.com"
        const val LUN = 1
        const val MAR = 2
        const val MIE = 4
        const val JUE = 8
        const val VIE = 16
    }
}

/** Envoltura para distinguir "todavía cargando" (null) de "cargado pero vacío". */
data class Cargado<T>(val valor: T)
