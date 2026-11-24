package com.example.aulaguia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.aulaguia.data.entity.Grupo
import com.example.aulaguia.data.entity.Salon
import com.example.aulaguia.navegacion.Ruta
import com.example.aulaguia.pantallas.*
import com.example.aulaguia.ui.componentes.AvisoResultado
import com.example.aulaguia.ui.theme.AulatecaTheme

/**
 * Aulateca – App para maestros
 *
 * Administra clases, grupos y salones.
 */
class MainActivity : ComponentActivity() {
    private val vm: AulaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // La pantalla de bienvenida se queda visible solo mientras se lee la sesión guardada
        installSplashScreen().setKeepOnScreenCondition { vm.sesion.value == Sesion.Cargando }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AulatecaTheme {
                AppNavegacion(vm)
            }
        }
    }
}

@Composable
fun AppNavegacion(vm: AulaViewModel = viewModel()) {
    val sesion by vm.sesion.collectAsStateWithLifecycle()

    // Mientras se lee la sesión guardada solo se pinta el fondo; así no parpadea el login
    if (sesion == Sesion.Cargando) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    val navController = rememberNavController()
    // El destino inicial se decide una sola vez; después la navegación es explícita
    val destinoInicial: Ruta = remember { if (sesion is Sesion.Activa) Ruta.Inicio else Ruta.Login }
    val usuarioActivo = (sesion as? Sesion.Activa)?.usuario

    val aviso by vm.aviso.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
    NavHost(navController = navController, startDestination = destinoInicial) {

        composable<Ruta.Login> {
            val error by vm.errorLogin.collectAsStateWithLifecycle()
            val procesando by vm.procesando.collectAsStateWithLifecycle()
            PantallaLogin(
                errorMensaje = error,
                procesando = procesando,
                onLoginExitoso = { email, contrasena ->
                    vm.login(email, contrasena) { navController.irAInicio() }
                },
                onLoginDemo = { vm.cargarDemo { navController.irAInicio() } },
                onIrARegistro = {
                    vm.limpiarErrores()
                    navController.navigate(Ruta.Registro)
                }
            )
        }

        composable<Ruta.Registro> {
            val error by vm.errorRegistro.collectAsStateWithLifecycle()
            val procesando by vm.procesando.collectAsStateWithLifecycle()
            PantallaRegistro(
                errorExterno = error,
                procesando = procesando,
                onRegistrar = { nombre, email, contrasena, avatarKey, fotoUri ->
                    vm.registrar(nombre, email, contrasena, avatarKey, fotoUri) { navController.irAInicio() }
                },
                onVolver = {
                    vm.limpiarErrores()
                    navController.popBackStack()
                }
            )
        }

        composable<Ruta.Inicio> {
            val grupos by vm.grupos.collectAsStateWithLifecycle()
            val totalGrupos by vm.totalGrupos.collectAsStateWithLifecycle()
            val totalSalones by vm.totalSalones.collectAsStateWithLifecycle()
            val totalNotas by vm.totalNotas.collectAsStateWithLifecycle()
            PantallaInicio(
                usuario = usuarioActivo,
                grupos = grupos,
                totalGrupos = totalGrupos,
                totalSalones = totalSalones,
                totalNotas = totalNotas,
                onIrAGrupos = { navController.navigate(Ruta.Grupos) },
                onIrASalones = { navController.navigate(Ruta.Salones) },
                onAgregarGrupo = { navController.navigate(Ruta.AgregarGrupo) },
                onAgregarSalon = { navController.navigate(Ruta.AgregarSalon) },
                onIrAPerfil = { navController.navigate(Ruta.EditarPerfil) },
                onCerrarSesion = { vm.cerrarSesion { navController.irALogin() } },
                onVerDetalleSalon = { salonId -> navController.navigate(Ruta.DetalleSalon(salonId)) },
                onExportar = vm::exportar,
                onImportar = vm::importar,
                onVaciarTodo = vm::vaciarTodo
            )
        }

        composable<Ruta.EditarPerfil> {
            val error by vm.errorPerfil.collectAsStateWithLifecycle()
            PantallaEditarPerfil(
                usuario = usuarioActivo,
                errorMensaje = error,
                onGuardarPerfil = { nombre, email, avatarKey, fotoUri ->
                    vm.guardarPerfil(nombre, email, avatarKey, fotoUri) { navController.popBackStack() }
                },
                onCerrarSesion = { vm.cerrarSesion { navController.irALogin() } },
                onVolver = {
                    vm.limpiarErrores()
                    navController.popBackStack()
                }
            )
        }

        composable<Ruta.Grupos> {
            val grupos by vm.grupos.collectAsStateWithLifecycle()
            PantallaGrupos(
                grupos = grupos,
                onVolver = { navController.popBackStack() },
                onAgregarGrupo = { navController.navigate(Ruta.AgregarGrupo) },
                onEditarGrupo = { id -> navController.navigate(Ruta.EditarGrupo(id)) },
                onEliminarGrupo = vm::eliminarGrupo
            )
        }

        composable<Ruta.Salones> {
            val salones by vm.salones.collectAsStateWithLifecycle()
            PantallaSalones(
                salones = salones,
                onVolver = { navController.popBackStack() },
                onAgregarSalon = { navController.navigate(Ruta.AgregarSalon) },
                onVerDetalle = { salonId -> navController.navigate(Ruta.DetalleSalon(salonId)) },
                onEliminarSalon = vm::eliminarSalon
            )
        }

        composable<Ruta.AgregarGrupo> {
            val salones by vm.salones.collectAsStateWithLifecycle()
            PantallaAgregarGrupo(
                salones = salones,
                onGuardar = { grupo -> vm.guardarGrupo(grupo) { navController.popBackStack() } },
                onVolver = { navController.popBackStack() }
            )
        }

        composable<Ruta.EditarGrupo> { entrada ->
            val grupoId = entrada.toRoute<Ruta.EditarGrupo>().grupoId
            val salones by vm.salones.collectAsStateWithLifecycle()
            val grupo by produceState<Grupo?>(null, grupoId) { value = vm.obtenerGrupo(grupoId) }

            grupo?.let {
                PantallaAgregarGrupo(
                    grupoExistente = it,
                    salones = salones,
                    onGuardar = { editado -> vm.guardarGrupo(editado) { navController.popBackStack() } },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable<Ruta.AgregarSalon> {
            PantallaAgregarSalon(
                onGuardar = { salon -> vm.guardarSalon(salon) { navController.popBackStack() } },
                onVolver = { navController.popBackStack() }
            )
        }

        composable<Ruta.EditarSalon> { entrada ->
            val salonId = entrada.toRoute<Ruta.EditarSalon>().salonId
            val salon by produceState<Salon?>(null, salonId) { value = vm.obtenerSalon(salonId) }

            salon?.let {
                PantallaAgregarSalon(
                    salonExistente = it,
                    onGuardar = { editado -> vm.guardarSalon(editado) { navController.popBackStack() } },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable<Ruta.DetalleSalon> { entrada ->
            val salonId = entrada.toRoute<Ruta.DetalleSalon>().salonId
            // remember evita crear una consulta nueva en cada recomposición
            val salonFlow = remember(salonId) { vm.observarSalon(salonId) }
            val notasFlow = remember(salonId) { vm.notasDeSalon(salonId) }
            val estado by salonFlow.collectAsStateWithLifecycle(initialValue = null)
            val notas by notasFlow.collectAsStateWithLifecycle(initialValue = emptyList())

            PantallaDetalleSalon(
                cargando = estado == null,
                salon = estado?.valor,
                notas = notas,
                onVolver = { navController.popBackStack() },
                onEditar = { navController.navigate(Ruta.EditarSalon(salonId)) },
                onAgregarNota = { texto -> vm.agregarNota(salonId, texto) },
                onEliminarNota = vm::eliminarNota
            )
        }
    }

    // Palomita o tache animada después de borrar, exportar o importar
    AvisoResultado(aviso = aviso, onTerminar = vm::cerrarAviso)
    }
}

private fun NavHostController.irAInicio() = navigate(Ruta.Inicio) {
    popUpTo(graph.id) { inclusive = true }
    launchSingleTop = true
}

private fun NavHostController.irALogin() = navigate(Ruta.Login) {
    popUpTo(graph.id) { inclusive = true }
    launchSingleTop = true
}
