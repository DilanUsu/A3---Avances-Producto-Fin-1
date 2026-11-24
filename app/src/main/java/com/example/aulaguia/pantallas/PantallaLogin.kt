package com.example.aulaguia.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.aulaguia.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.aulaguia.ui.theme.CafePrimario

// ═══════════════════════════════════════════
//  Pantalla de Inicio de Sesión
// ═══════════════════════════════════════════

@Composable
fun PantallaLogin(
    onLoginExitoso: (email: String, contrasena: String) -> Unit,
    onIrARegistro: () -> Unit,
    onLoginDemo: () -> Unit,
    errorMensaje: String? = null,
    procesando: Boolean = false
) {
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var verContrasena by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // ── Logo de la app ──
            Image(
                painter = painterResource(R.drawable.logo_aulateca),
                contentDescription = "Logo de Aulateca",
                modifier = Modifier.size(112.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Aulateca",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Inicia sesión para gestionar tus salones y horarios",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Formulario ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Campo Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico / Usuario") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Contraseña
                    OutlinedTextField(
                        value = contrasena,
                        onValueChange = { contrasena = it },
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { verContrasena = !verContrasena }) {
                                Icon(
                                    imageVector = if (verContrasena) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = if (verContrasena) "Ocultar" else "Mostrar"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (verContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

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

                    // Botón Iniciar Sesión
                    Button(
                        onClick = { onLoginExitoso(email.trim(), contrasena) },
                        enabled = !procesando && email.isNotBlank() && contrasena.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CafePrimario)
                    ) {
                        if (procesando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Rounded.Login, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar Sesión", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón Demo interactivo con datos ficticios
                    OutlinedButton(
                        onClick = onLoginDemo,
                        enabled = !procesando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.PlayCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Probar Demo interactiva de la App")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Crear Cuenta ──
            TextButton(onClick = onIrARegistro) {
                Text(
                    text = "¿No tienes una cuenta? Regístrate aquí",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
