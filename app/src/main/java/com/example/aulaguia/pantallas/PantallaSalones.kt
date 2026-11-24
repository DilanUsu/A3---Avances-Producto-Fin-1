package com.example.aulaguia.pantallas

import androidx.compose.foundation.clickable
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
import com.example.aulaguia.data.entity.Salon
import com.example.aulaguia.ui.theme.CafeAcento
import com.example.aulaguia.ui.theme.CafePrimario

// ═══════════════════════════════════════════
//  Pantalla de Salones
//  Lista de salones con ubicación y referencia
// ═══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSalones(
    salones: List<Salon>,
    onVolver: () -> Unit,
    onAgregarSalon: () -> Unit,
    onVerDetalle: (Int) -> Unit,
    onEliminarSalon: (Salon) -> Unit
) {
    var salonAEliminar by remember { mutableStateOf<Salon?>(null) }
    var busqueda by rememberSaveable { mutableStateOf("") }
    val filtrados = remember(salones, busqueda) {
        val q = busqueda.trim()
        if (q.isEmpty()) salones else salones.filter { x ->
            x.nombre.contains(q, ignoreCase = true) ||
            x.edificio.contains(q, ignoreCase = true) ||
            x.piso.contains(q, ignoreCase = true) ||
            x.referencia.contains(q, ignoreCase = true) ||
            x.equipamiento.contains(q, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Salones") },
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
                onClick = onAgregarSalon,
                containerColor = CafePrimario,
                contentColor = Color.White
            ) {
                Icon(Icons.Rounded.Add, "Agregar salón")
            }
        }
    ) { padding ->
        if (salones.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.MeetingRoom,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = CafeAcento.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No hay salones registrados",
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
                    CampoBusqueda(busqueda, { busqueda = it }, "Buscar salón, edificio o equipo")
                }
                if (filtrados.isEmpty()) {
                    item { SinResultados(busqueda) }
                }
                items(filtrados, key = { it.id }) { salon ->
                    TarjetaSalon(
                        salon = salon,
                        onClick = { onVerDetalle(salon.id) },
                        onEliminar = { salonAEliminar = salon }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    salonAEliminar?.let { salon ->
        AlertDialog(
            onDismissRequest = { salonAEliminar = null },
            icon = { Icon(Icons.Rounded.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar salón") },
            text = { Text("¿Eliminar ${salon.nombre}? Se borrarán sus notas y sus grupos quedarán sin salón. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    onEliminarSalon(salon)
                    salonAEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { salonAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaSalon(
    salon: Salon,
    onClick: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                Text(
                    text = salon.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Rounded.Delete,
                        "Eliminar",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Edificio y piso
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Business,
                    null,
                    Modifier.size(16.dp),
                    tint = CafeAcento
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "${salon.edificio} · ${salon.piso}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Referencia
            if (salon.referencia.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Place,
                        null,
                        Modifier.size(16.dp),
                        tint = CafeAcento
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        salon.referencia,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Capacidad
            if (salon.capacidad > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.People,
                        null,
                        Modifier.size(16.dp),
                        tint = CafeAcento
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Capacidad: ${salon.capacidad} alumnos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Indicador de "ver más"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ver notas",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    Icons.Rounded.ChevronRight,
                    null,
                    Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
