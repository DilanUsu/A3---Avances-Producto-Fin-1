package com.example.aulaguia.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aulaguia.data.entity.Grupo
import com.example.aulaguia.ui.theme.CafeAcento
import com.example.aulaguia.ui.theme.CafePrimario

// ═══════════════════════════════════════════
//  Pantalla de Grupos / Clases
//  Muestra todos los grupos del maestro
// ═══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGrupos(
    grupos: List<Grupo>,
    onVolver: () -> Unit,
    onAgregarGrupo: () -> Unit,
    onEditarGrupo: (Int) -> Unit,
    onEliminarGrupo: (Grupo) -> Unit
) {
    var grupoAEliminar by remember { mutableStateOf<Grupo?>(null) }
    var busqueda by rememberSaveable { mutableStateOf("") }
    val filtrados = remember(grupos, busqueda) {
        val q = busqueda.trim()
        if (q.isEmpty()) grupos else grupos.filter { x ->
            x.nombre.contains(q, ignoreCase = true) ||
            x.materia.contains(q, ignoreCase = true) ||
            x.salonNombre.contains(q, ignoreCase = true) ||
            x.horario.contains(q, ignoreCase = true) ||
            x.semestre.contains(q, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Grupos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarGrupo,
                containerColor = CafePrimario,
                contentColor = Color.White
            ) {
                Icon(Icons.Rounded.Add, "Agregar grupo")
            }
        }
    ) { padding ->
        if (grupos.isEmpty()) {
            // Estado vacío
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.School,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = CafeAcento.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No hay grupos registrados",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Presiona + para agregar uno",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    CampoBusqueda(busqueda, { busqueda = it }, "Buscar grupo, materia o salón")
                }
                if (filtrados.isEmpty()) {
                    item { SinResultados(busqueda) }
                }
                items(filtrados, key = { it.id }) { grupo ->
                    TarjetaGrupo(
                        grupo = grupo,
                        onEditar = { onEditarGrupo(grupo.id) },
                        onEliminar = { grupoAEliminar = grupo }
                    )
                }
                // Espacio extra al final para el FAB
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Diálogo de confirmación para eliminar
    grupoAEliminar?.let { grupo ->
        AlertDialog(
            onDismissRequest = { grupoAEliminar = null },
            icon = { Icon(Icons.Rounded.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar grupo") },
            text = { Text("¿Eliminar ${grupo.nombre} - ${grupo.materia}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    onEliminarGrupo(grupo)
                    grupoAEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { grupoAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaGrupo(
    grupo: Grupo,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nombre del grupo
                Text(
                    text = grupo.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                // Semestre
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        grupo.semestre,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = grupo.materia,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Info en fila
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.MeetingRoom,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = CafeAcento
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    grupo.salonNombre,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    Icons.Rounded.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = CafeAcento
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    grupo.horario,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (grupo.numAlumnos > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = CafeAcento
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${grupo.numAlumnos} alumnos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditar) {
                    Icon(Icons.Rounded.Edit, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Editar")
                }
                TextButton(onClick = onEliminar) {
                    Icon(
                        Icons.Rounded.Delete, null,
                        Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
internal fun CampoBusqueda(valor: String, onCambio: (String) -> Unit, pista: String) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        placeholder = { Text(pista) },
        leadingIcon = { Icon(Icons.Rounded.Search, null) },
        trailingIcon = {
            if (valor.isNotEmpty()) {
                IconButton(onClick = { onCambio("") }) { Icon(Icons.Rounded.Close, "Borrar búsqueda") }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
internal fun SinResultados(busqueda: String) {
    Text(
        "No hay resultados para \"$busqueda\"",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
    )
}
