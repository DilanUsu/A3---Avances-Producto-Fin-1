package com.example.aulaguia.pantallas

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aulaguia.data.entity.Grupo
import com.example.aulaguia.data.entity.Usuario
import com.example.aulaguia.ui.componentes.AvatarUsuario
import com.example.aulaguia.data.entity.diasDeClase
import com.example.aulaguia.data.entity.tieneSalon
import com.example.aulaguia.ui.theme.*
import com.example.aulaguia.util.Horario
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.text.Normalizer
import java.time.format.TextStyle
import java.util.Locale

internal val LOCALE_ES: Locale = Locale.forLanguageTag("es-MX")

// ═══════════════════════════════════════════
//  Pantalla de Inicio con Calendario y Menú Desplegable Lateral Suave
// ═══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    usuario: Usuario?,
    grupos: List<Grupo>,
    totalGrupos: Int,
    totalSalones: Int,
    totalNotas: Int,
    onIrAGrupos: () -> Unit,
    onIrASalones: () -> Unit,
    onAgregarGrupo: () -> Unit,
    onAgregarSalon: () -> Unit,
    onIrAPerfil: () -> Unit,
    onCerrarSesion: () -> Unit,
    onVerDetalleSalon: (Int) -> Unit = {},
    onExportar: (Uri) -> Unit = {},
    onImportar: (Uri) -> Unit = {},
    onVaciarTodo: () -> Unit = {}
) {
    // Selector de Android para elegir dónde guardar el respaldo (Descargas, Drive, USB...)
    val exportarLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(onExportar) }
    var archivoAImportar by remember { mutableStateOf<Uri?>(null) }
    val importarLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> archivoAImportar = uri }
    var confirmarVaciar by remember { mutableStateOf(false) }
    val exportar = { exportarLauncher.launch("Aulateca-respaldo-${LocalDate.now()}.json") }

    var fechaSeleccionada by remember { mutableStateOf(LocalDate.now()) }
    var vistaCompacta by remember { mutableStateOf(false) }
    var menuAbierto by remember { mutableStateOf(false) }

    // El botón atrás cierra el menú lateral en lugar de salir de la app
    BackHandler(enabled = menuAbierto) { menuAbierto = false }

    // Los días de cada grupo se calculan una sola vez cuando cambia la lista, no en cada dibujo
    val diasPorGrupo = remember(grupos) { grupos.associateWith { it.diasDeClase() } }
    val diasConClase = remember(diasPorGrupo) {
        DayOfWeek.entries.filter { d -> diasPorGrupo.values.any { Horario.contiene(it, d) } }.toSet()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── CONTENIDO PRINCIPAL ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ── Barra Superior (Top Header con botón de Menú Derecho) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Perfil de usuario (Clickable para editar)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onIrAPerfil)
                        .padding(4.dp)
                ) {
                    AvatarUsuario(
                        fotoAvatarKey = usuario?.fotoAvatar,
                        fotoUri = usuario?.fotoUri,
                        tamano = 44.dp,
                        onClick = onIrAPerfil
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "¡Hola, ${usuario?.nombre ?: "Maestro"}!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Aulateca",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Botón del Menú Desplegable (Lado derecho superior)
                IconButton(
                    onClick = { menuAbierto = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Widgets,
                        contentDescription = "Abrir Menú de Opciones",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── CALENDARIO INTERACTIVO ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CalendarioInteractivo(
                        fechaSeleccionada = fechaSeleccionada,
                        diasConClase = diasConClase,
                        onFechaSeleccionada = { nuevaFecha -> fechaSeleccionada = nuevaFecha }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── SECCIÓN DE CLASES DEL DÍA SELECCIONADO ──
            val clasesDelDia = remember(fechaSeleccionada, diasPorGrupo) {
                val dia = fechaSeleccionada.dayOfWeek
                grupos.filter { Horario.contiene(diasPorGrupo[it] ?: 0, dia) }
                    .sortedWith(compareBy({ if (it.horaInicio < 0) Int.MAX_VALUE else it.horaInicio }, { it.nombre }))
            }

            val fechaFormateada = remember(fechaSeleccionada) {
                val nombreDia = fechaSeleccionada.dayOfWeek.getDisplayName(TextStyle.FULL, LOCALE_ES)
                    .replaceFirstChar { it.uppercase() }
                val mes = fechaSeleccionada.month.getDisplayName(TextStyle.FULL, LOCALE_ES)
                "$nombreDia ${fechaSeleccionada.dayOfMonth} de $mes"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Clases del día",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = fechaFormateada,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Botón para alternar Vista Compacta / Normal ("hacer más pequeñas las cosas")
                FilterChip(
                    selected = vistaCompacta,
                    onClick = { vistaCompacta = !vistaCompacta },
                    label = { Text(if (vistaCompacta) "Compacto" else "Normal", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (vistaCompacta) Icons.Rounded.ViewHeadline else Icons.Rounded.ViewAgenda,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CafePrimario,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (clasesDelDia.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EventAvailable,
                            contentDescription = null,
                            tint = CafeAcento,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Sin clases programadas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "No hay clases registradas para este día de la semana.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    clasesDelDia.forEach { grupo ->
                        key(grupo.id) {
                        TarjetaClaseDia(
                            grupo = grupo,
                            esCompacto = vistaCompacta,
                            onVerSalon = if (grupo.tieneSalon) {
                                { onVerDetalleSalon(grupo.salonId) }
                            } else null
                        )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // ── MENÚ DESPLEGABLE LATERAL DERECHO CON ANIMACIÓN SUAVE ──
        AnimatedVisibility(
            visible = menuAbierto,
            enter = fadeIn(animationSpec = tween(250)),
            exit = fadeOut(animationSpec = tween(250))
        ) {
            // Fondo oscuro semitransparente al tocar cierra el menú
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { menuAbierto = false }
            )
        }

        AnimatedVisibility(
            visible = menuAbierto,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            MenuLateral(
                usuario = usuario,
                totalGrupos = totalGrupos,
                totalSalones = totalSalones,
                totalNotas = totalNotas,
                onCerrar = { menuAbierto = false },
                onIrAPerfil = onIrAPerfil,
                onIrAGrupos = onIrAGrupos,
                onIrASalones = onIrASalones,
                onAgregarGrupo = onAgregarGrupo,
                onAgregarSalon = onAgregarSalon,
                onCerrarSesion = onCerrarSesion,
                onExportar = exportar,
                onImportar = { importarLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) },
                onVaciar = { confirmarVaciar = true }
            )
        }
    }

    // ── Aviso antes de vaciar todo ──
    if (confirmarVaciar) {
        val nadaQueBorrar = totalGrupos + totalSalones + totalNotas == 0
        AlertDialog(
            onDismissRequest = { confirmarVaciar = false },
            icon = { Icon(Icons.Rounded.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(if (nadaQueBorrar) "No hay nada que borrar" else "¿Vaciar toda la información?") },
            text = {
                Text(
                    if (nadaQueBorrar) "Tu cuenta no tiene salones, grupos ni notas."
                    else "Se borrarán $totalGrupos grupos, $totalSalones salones y $totalNotas notas de tu cuenta. " +
                        "Esta acción no se puede deshacer.\n\nTe recomendamos exportar un respaldo antes."
                )
            },
            confirmButton = {
                if (nadaQueBorrar) {
                    TextButton(onClick = { confirmarVaciar = false }) { Text("Entendido") }
                } else {
                    TextButton(onClick = {
                        confirmarVaciar = false
                        onVaciarTodo()
                    }) {
                        Text("Vaciar todo", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!nadaQueBorrar) {
                    Row {
                        TextButton(onClick = {
                            confirmarVaciar = false
                            exportar()
                        }) { Text("Exportar primero") }
                        TextButton(onClick = { confirmarVaciar = false }) { Text("Cancelar") }
                    }
                }
            }
        )
    }

    // ── Aviso antes de reemplazar los datos con un respaldo ──
    archivoAImportar?.let { uri ->
        AlertDialog(
            onDismissRequest = { archivoAImportar = null },
            icon = { Icon(Icons.Rounded.FileDownload, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("¿Cargar este respaldo?") },
            text = {
                Text("Tus salones, grupos y notas actuales se reemplazarán por los del archivo. Si el archivo tiene un error, no se borrará nada.")
            },
            confirmButton = {
                TextButton(onClick = {
                    archivoAImportar = null
                    onImportar(uri)
                }) { Text("Cargar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { archivoAImportar = null }) { Text("Cancelar") }
            }
        )
    }
}
