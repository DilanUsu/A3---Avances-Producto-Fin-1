package com.example.aulaguia.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aulaguia.data.entity.Grupo
import com.example.aulaguia.data.entity.Salon
import com.example.aulaguia.data.entity.diasDeClase
import com.example.aulaguia.data.entity.etiqueta
import com.example.aulaguia.ui.theme.CafePrimario
import com.example.aulaguia.util.Horario
import java.time.DayOfWeek

// ═══════════════════════════════════════════
//  Formulario para agregar/editar un Grupo
// ═══════════════════════════════════════════

private enum class CampoHora { INICIO, FIN }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaAgregarGrupo(
    grupoExistente: Grupo? = null,
    salones: List<Salon>,
    onGuardar: (Grupo) -> Unit,
    onVolver: () -> Unit
) {
    val esEdicion = grupoExistente != null
    // Si el grupo es de la versión anterior, la hora se intenta leer del texto
    val horasViejas = remember(grupoExistente) {
        grupoExistente?.takeIf { it.horaInicio < 0 }?.let { Horario.horasDesdeTexto(it.horario) }
    }

    var nombre by remember { mutableStateOf(grupoExistente?.nombre ?: "") }
    var materia by remember { mutableStateOf(grupoExistente?.materia ?: "") }
    var semestre by remember { mutableStateOf(grupoExistente?.semestre ?: "") }
    var numAlumnos by remember { mutableStateOf(grupoExistente?.numAlumnos?.takeIf { it > 0 }?.toString() ?: "") }
    var dias by remember { mutableIntStateOf(grupoExistente?.diasDeClase() ?: 0) }
    var horaInicio by remember {
        mutableIntStateOf(grupoExistente?.horaInicio?.takeIf { it >= 0 } ?: horasViejas?.first ?: Horario.SIN_HORA)
    }
    var horaFin by remember {
        mutableIntStateOf(grupoExistente?.horaFin?.takeIf { it >= 0 } ?: horasViejas?.second ?: Horario.SIN_HORA)
    }
    var salonIdSeleccionado by remember { mutableStateOf(grupoExistente?.salonId?.takeIf { it != 0 }) }
    val salonSeleccionado = remember(salones, salonIdSeleccionado) {
        salones.find { it.id == salonIdSeleccionado }
    }
    var menuExpanded by remember { mutableStateOf(false) }
    var editandoHora by remember { mutableStateOf<CampoHora?>(null) }

    val horasValidas = horaInicio >= 0 && horaFin > horaInicio
    val formularioValido = nombre.isNotBlank() && materia.isNotBlank() && semestre.isNotBlank() &&
        dias != 0 && horasValidas

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar Grupo" else "Nuevo Grupo") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del grupo") },
                placeholder = { Text("Ej: 3A, 5B") },
                leadingIcon = { Icon(Icons.Rounded.Group, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = materia,
                onValueChange = { materia = it },
                label = { Text("Materia") },
                placeholder = { Text("Ej: Matemáticas I") },
                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.MenuBook, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Selector de salón
            ExposedDropdownMenuBox(
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = it }
            ) {
                OutlinedTextField(
                    value = salonSeleccionado?.etiqueta() ?: "Sin salón",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Salón asignado") },
                    leadingIcon = { Icon(Icons.Rounded.MeetingRoom, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sin salón") },
                        onClick = {
                            salonIdSeleccionado = null
                            menuExpanded = false
                        }
                    )
                    if (salones.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay salones. Agrega uno primero.") },
                            onClick = { menuExpanded = false },
                            enabled = false
                        )
                    }
                    for (salon in salones) {
                        DropdownMenuItem(
                            text = { Text(salon.etiqueta()) },
                            onClick = {
                                salonIdSeleccionado = salon.id
                                menuExpanded = false
                            }
                        )
                    }
                }
            }

            // ── Días de clase ──
            Column {
                Text(
                    "Días de clase",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (dia in DayOfWeek.entries) {
                        FilterChip(
                            selected = Horario.contiene(dias, dia),
                            onClick = { dias = Horario.alternar(dias, dia) },
                            label = { Text(Horario.ABREVIATURAS[dia.value - 1]) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CafePrimario,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // ── Horas ──
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoDeHora(
                    etiqueta = "Inicio",
                    minutos = horaInicio,
                    onClick = { editandoHora = CampoHora.INICIO },
                    modifier = Modifier.weight(1f)
                )
                CampoDeHora(
                    etiqueta = "Fin",
                    minutos = horaFin,
                    onClick = { editandoHora = CampoHora.FIN },
                    modifier = Modifier.weight(1f)
                )
            }
            if (horaInicio >= 0 && horaFin >= 0 && !horasValidas) {
                Text(
                    "La hora de fin debe ser después de la de inicio",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = semestre,
                onValueChange = { semestre = it },
                label = { Text("Semestre") },
                placeholder = { Text("Ej: 2026A") },
                leadingIcon = { Icon(Icons.Rounded.CalendarMonth, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = numAlumnos,
                onValueChange = { numAlumnos = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text("Número de alumnos (opcional)") },
                leadingIcon = { Icon(Icons.Rounded.People, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onGuardar(
                        Grupo(
                            id = grupoExistente?.id ?: 0,
                            nombre = nombre.trim(),
                            materia = materia.trim(),
                            salonId = salonSeleccionado?.id ?: 0,
                            salonNombre = salonSeleccionado?.etiqueta() ?: "Sin salón",
                            horario = Horario.texto(dias, horaInicio, horaFin),
                            semestre = semestre.trim(),
                            numAlumnos = numAlumnos.toIntOrNull() ?: 0,
                            dias = dias,
                            horaInicio = horaInicio,
                            horaFin = horaFin
                        )
                    )
                },
                enabled = formularioValido,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CafePrimario)
            ) {
                Icon(Icons.Rounded.Save, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (esEdicion) "Guardar cambios" else "Agregar grupo")
            }
        }
    }

    editandoHora?.let { campo ->
        val actual = if (campo == CampoHora.INICIO) horaInicio else horaFin
        val sugerida = when {
            actual >= 0 -> actual
            campo == CampoHora.FIN && horaInicio >= 0 -> (horaInicio + 120).coerceAtMost(23 * 60 + 59)
            else -> 8 * 60
        }
        DialogoHora(
            titulo = if (campo == CampoHora.INICIO) "Hora de inicio" else "Hora de fin",
            minutosIniciales = sugerida,
            onConfirmar = { minutos ->
                if (campo == CampoHora.INICIO) horaInicio = minutos else horaFin = minutos
                editandoHora = null
            },
            onCancelar = { editandoHora = null }
        )
    }
}

@Composable
private fun CampoDeHora(etiqueta: String, minutos: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Rounded.Schedule, null, Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(etiqueta, style = MaterialTheme.typography.labelSmall)
            Text(
                if (minutos >= 0) Horario.formatoHora(minutos) else "Elegir",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoHora(
    titulo: String,
    minutosIniciales: Int,
    onConfirmar: (Int) -> Unit,
    onCancelar: () -> Unit
) {
    val estado = rememberTimePickerState(
        initialHour = minutosIniciales / 60,
        initialMinute = minutosIniciales % 60,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { TimePicker(state = estado) },
        confirmButton = {
            TextButton(onClick = { onConfirmar(estado.hour * 60 + estado.minute) }) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
