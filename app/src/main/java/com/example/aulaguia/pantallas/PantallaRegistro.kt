package com.example.aulaguia.pantallas

import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.aulaguia.ui.componentes.AvatarUsuario
import com.example.aulaguia.ui.componentes.SelectorAvatar
import com.example.aulaguia.ui.componentes.guardarFotoPerfil
import com.example.aulaguia.ui.theme.CafePrimario
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════
//  Pantalla de Registro de Usuario
// ═══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistro(
    onRegistrar: (nombre: String, email: String, contrasena: String, avatarKey: String, fotoUri: String?) -> Unit,
    onVolver: () -> Unit,
    errorExterno: String? = null,
    procesando: Boolean = false
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var avatarSeleccionado by remember { mutableStateOf("maestro_1") }
    var fotoUriSeleccionada by remember { mutableStateOf<String?>(null) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

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
                title = { Text("Crear Cuenta") },
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
            Text(
                text = "Perfil del Maestro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ── Previsualizar Avatar/Foto ──
            AvatarUsuario(
                fotoAvatarKey = avatarSeleccionado,
                fotoUri = fotoUriSeleccionada,
                tamano = 96.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Cargar de la galería
            OutlinedButton(
                onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (fotoUriSeleccionada != null) "Cambiar foto de galería" else "Elegir foto de galería")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "O elige un avatar prediseñado:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            SelectorAvatar(
                avatarSeleccionado = avatarSeleccionado,
                hayFoto = fotoUriSeleccionada != null,
                onSeleccionar = {
                    avatarSeleccionado = it
                    fotoUriSeleccionada = null
                },
                tamano = 54.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Formulario ──
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre Completo / Titular") },
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico / Usuario") },
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            val error = errorMensaje ?: errorExterno
            if (!error.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    errorMensaje = when {
                        nombre.isBlank() || email.isBlank() || contrasena.isBlank() ->
                            "Por favor completa todos los campos"
                        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                            "Escribe un correo electrónico válido"
                        contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
                        else -> null
                    }
                    if (errorMensaje == null) {
                        onRegistrar(nombre.trim(), email.trim(), contrasena, avatarSeleccionado, fotoUriSeleccionada)
                    }
                },
                enabled = !procesando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CafePrimario)
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear Cuenta", fontWeight = FontWeight.Bold)
            }
        }
    }
}
