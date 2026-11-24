package com.example.aulaguia.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aulaguia.data.entity.Salon
import com.example.aulaguia.ui.theme.CafePrimario

// ═══════════════════════════════════════════
//  Formulario para agregar un Salón nuevo
// ═══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarSalon(
    onGuardar: (Salon) -> Unit,
    onVolver: () -> Unit,
    salonExistente: Salon? = null
) {
    val esEdicion = salonExistente != null
    var nombre by remember { mutableStateOf(salonExistente?.nombre ?: "") }
    var edificio by remember { mutableStateOf(salonExistente?.edificio ?: "") }
    var piso by remember { mutableStateOf(salonExistente?.piso ?: "") }
    var referencia by remember { mutableStateOf(salonExistente?.referencia ?: "") }
    var capacidad by remember { mutableStateOf(salonExistente?.capacidad?.takeIf { it > 0 }?.toString() ?: "") }
    var equipamiento by remember { mutableStateOf(salonExistente?.equipamiento ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar Salón" else "Nuevo Salón") },
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
            // Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del salón") },
                placeholder = { Text("Ej: Aula 204, Lab Cómputo 1") },
                leadingIcon = { Icon(Icons.Rounded.MeetingRoom, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Edificio
            OutlinedTextField(
                value = edificio,
                onValueChange = { edificio = it },
                label = { Text("Edificio") },
                placeholder = { Text("Ej: Edificio A, Módulo Norte") },
                leadingIcon = { Icon(Icons.Rounded.Business, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Piso
            OutlinedTextField(
                value = piso,
                onValueChange = { piso = it },
                label = { Text("Piso / Nivel") },
                placeholder = { Text("Ej: Planta baja, 2do piso") },
                leadingIcon = { Icon(Icons.Rounded.Layers, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Referencia (campo importante)
            OutlinedTextField(
                value = referencia,
                onValueChange = { referencia = it },
                label = { Text("Referencia para encontrarlo") },
                placeholder = { Text("Ej: Al fondo del pasillo, junto a baños") },
                leadingIcon = { Icon(Icons.Rounded.Place, null) },
                supportingText = {
                    Text("Describe cómo llegar para un maestro nuevo")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 3
            )

            // Capacidad
            OutlinedTextField(
                value = capacidad,
                onValueChange = { capacidad = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text("Capacidad (alumnos)") },
                leadingIcon = { Icon(Icons.Rounded.People, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Equipamiento
            OutlinedTextField(
                value = equipamiento,
                onValueChange = { equipamiento = it },
                label = { Text("Equipamiento (opcional)") },
                placeholder = { Text("Ej: Proyector, pizarrón, aire acondicionado") },
                leadingIcon = { Icon(Icons.Rounded.Devices, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón guardar
            Button(
                onClick = {
                    val salon = Salon(
                        id = salonExistente?.id ?: 0,
                        nombre = nombre.trim(),
                        edificio = edificio.trim(),
                        piso = piso.trim(),
                        referencia = referencia.trim(),
                        capacidad = capacidad.toIntOrNull() ?: 0,
                        equipamiento = equipamiento.trim()
                    )
                    onGuardar(salon)
                },
                enabled = nombre.isNotBlank() && edificio.isNotBlank()
                        && piso.isNotBlank() && referencia.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CafePrimario)
            ) {
                Icon(Icons.Rounded.Save, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (esEdicion) "Guardar cambios" else "Guardar salón")
            }
        }
    }
}
