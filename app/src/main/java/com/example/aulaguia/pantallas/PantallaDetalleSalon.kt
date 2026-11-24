package com.example.aulaguia.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.StickyNote2
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aulaguia.data.entity.Nota
import com.example.aulaguia.data.entity.Salon
import com.example.aulaguia.ui.theme.CafeAcento
import com.example.aulaguia.ui.theme.CafePrimario
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Se crea una vez; antes se creaba un SimpleDateFormat por cada nota
private val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

// ═══════════════════════════════════════════
//  Detalle de un Salón
//  Info completa + notas de maestros
// ═══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleSalon(
    cargando: Boolean,
    salon: Salon?,
    notas: List<Nota>,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    onAgregarNota: (String) -> Unit,
    onEliminarNota: (Nota) -> Unit
) {
    var textoNota by remember { mutableStateOf("") }
    var notaAEliminar by remember { mutableStateOf<Nota?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(salon?.nombre ?: "Salón") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (salon != null) {
                        IconButton(onClick = onEditar) {
                            Icon(Icons.Rounded.Edit, "Editar salón")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (salon == null) {
            // Mientras carga no se muestra nada; si ya no existe, se avisa y se puede regresar
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (!cargando) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.SearchOff, null,
                            Modifier.size(56.dp),
                            tint = CafeAcento
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Este salón ya no existe",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = onVolver) { Text("Regresar") }
                    }
                }
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.padding(padding).imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Info del salón ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CafePrimario)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            salon.nombre,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))

                        InfoRow(Icons.Rounded.Business, "Edificio", salon.edificio)
                        Spacer(Modifier.height(8.dp))
                        InfoRow(Icons.Rounded.Layers, "Piso", salon.piso)
                        Spacer(Modifier.height(8.dp))
                        InfoRow(Icons.Rounded.Place, "Referencia", salon.referencia)

                        if (salon.capacidad > 0) {
                            Spacer(Modifier.height(8.dp))
                            InfoRow(
                                Icons.Rounded.People,
                                "Capacidad",
                                "${salon.capacidad} alumnos"
                            )
                        }
                        if (salon.equipamiento.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            InfoRow(
                                Icons.Rounded.Devices,
                                "Equipo",
                                salon.equipamiento
                            )
                        }
                    }
                }
            }

            // ── Sección de notas ──
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Notas del salón",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Deja tips para el próximo maestro que use este salón",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Campo para nueva nota ──
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = textoNota,
                            onValueChange = { textoNota = it },
                            label = { Text("Escribe una nota...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (textoNota.isNotBlank()) {
                                    onAgregarNota(textoNota.trim())
                                    textoNota = ""
                                }
                            },
                            enabled = textoNota.isNotBlank(),
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CafePrimario
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Add, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Agregar nota")
                        }
                    }
                }
            }

            // ── Lista de notas existentes ──
            if (notas.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Sin notas aún",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(notas, key = { it.id }) { nota ->
                    TarjetaNota(
                        nota = nota,
                        onEliminar = { notaAEliminar = nota }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    notaAEliminar?.let { nota ->
        AlertDialog(
            onDismissRequest = { notaAEliminar = null },
            icon = { Icon(Icons.Rounded.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar nota") },
            text = { Text("¿Eliminar esta nota? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    onEliminarNota(nota)
                    notaAEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { notaAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun InfoRow(
    icono: ImageVector,
    etiqueta: String,
    valor: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            icono, null,
            Modifier.size(18.dp),
            tint = CafeAcento
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = CafeAcento
            )
            Text(
                valor,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun TarjetaNota(nota: Nota, onEliminar: () -> Unit) {
    val fechaTexto = remember(nota.fecha) {
        FORMATO_FECHA.format(Instant.ofEpochMilli(nota.fecha).atZone(ZoneId.systemDefault()))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.StickyNote2,
                null,
                Modifier.size(20.dp),
                tint = CafeAcento
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    nota.contenido,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    fechaTexto,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEliminar, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Rounded.Close, "Eliminar",
                    Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
