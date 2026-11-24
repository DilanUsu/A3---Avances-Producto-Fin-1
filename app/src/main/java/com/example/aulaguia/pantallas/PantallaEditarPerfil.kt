package com.example.aulaguia.pantallas

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aulaguia.data.entity.Usuario
import com.example.aulaguia.ui.componentes.AvatarUsuario
import com.example.aulaguia.ui.componentes.SelectorAvatar
import com.example.aulaguia.ui.componentes.guardarFotoPerfil
import com.example.aulaguia.ui.theme.CafePrimario
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════
//  Pantalla para Editar Perfil y Foto
// ═══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarPerfil(
    usuario: Usuario?,
    onGuardarPerfil: (nombre: String, email: String, avatarKey: String, fotoUri: String?) -> Unit,
    onCerrarSesion: () -> Unit,
    onVolver: () -> Unit,
    errorMensaje: String? = null
) {
    var nombre by remember(usuario) { mutableStateOf(usuario?.nombre ?: "") }
    var email by remember(usuario) { mutableStateOf(usuario?.email ?: "") }
    var avatarSeleccionado by remember(usuario) { mutableStateOf(usuario?.fotoAvatar ?: "maestro_1") }
    var fotoUriSeleccionada by remember(usuario) { mutableStateOf(usuario?.fotoUri) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Selector de fotos moderno de Android; no necesita permisos de almacenamiento
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) scope.launch {
            guardarFotoPerfil(context, uri)?.let { fotoUriSeleccionada = it }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Previsualización Avatar ──
            AvatarUsuario(
                fotoAvatarKey = avatarSeleccionado,
                fotoUri = fotoUriSeleccionada,
                tamano = 110.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Editar / Cambiar Foto de Galería
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CafePrimario)
                ) {
                    Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Editar Foto (Galería)")
                }

                if (fotoUriSeleccionada != null) {
                    OutlinedButton(
                        onClick = { fotoUriSeleccionada = null },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Quitar Foto")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "O selecciona un icono de perfil:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            SelectorAvatar(
                avatarSeleccionado = avatarSeleccionado,
                hayFoto = fotoUriSeleccionada != null,
                onSeleccionar = {
                    avatarSeleccionado = it
                    fotoUriSeleccionada = null
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Editar Datos ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Información Personal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del Maestro") },
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            if (!errorMensaje.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Guardar Cambios
            Button(
                onClick = {
                    onGuardarPerfil(nombre.trim(), email.trim(), avatarSeleccionado, fotoUriSeleccionada)
                },
                enabled = nombre.isNotBlank() && email.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CafePrimario)
            ) {
                Icon(Icons.Rounded.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Cambios", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Cerrar Sesión
            OutlinedButton(
                onClick = onCerrarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
            }
        }
    }
}
