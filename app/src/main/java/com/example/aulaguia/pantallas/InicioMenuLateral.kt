package com.example.aulaguia.pantallas

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import com.example.aulaguia.R
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


// ═══════════════════════════════════════════
//  Menú lateral de la pantalla de inicio
// ═══════════════════════════════════════════

@Composable
internal fun MenuLateral(
    usuario: Usuario?,
    totalGrupos: Int,
    totalSalones: Int,
    totalNotas: Int,
    onCerrar: () -> Unit,
    onIrAPerfil: () -> Unit,
    onIrAGrupos: () -> Unit,
    onIrASalones: () -> Unit,
    onAgregarGrupo: () -> Unit,
    onAgregarSalon: () -> Unit,
    onCerrarSesion: () -> Unit,
    onExportar: () -> Unit,
    onImportar: () -> Unit,
    onVaciar: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(310.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Encabezado del Menú Lateral
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_aulateca),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    text = "Aulateca",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { onCerrar() }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar menú"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta Perfil dentro del menú
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onCerrar()
                        onIrAPerfil()
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarUsuario(
                        fotoAvatarKey = usuario?.fotoAvatar,
                        fotoUri = usuario?.fotoUri,
                        tamano = 46.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = usuario?.nombre ?: "Maestro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Editar Perfil",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── RESUMEN DEL SEMESTRE ──
            Text(
                text = "Resumen del semestre",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CafePrimario)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ItemResumen(valor = "$totalGrupos", etiqueta = "Grupos")
                    ItemResumen(valor = "$totalSalones", etiqueta = "Salones")
                    ItemResumen(valor = "$totalNotas", etiqueta = "Notas")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── ACCESOS RÁPIDOS ──
            Text(
                text = "Accesos rápidos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            OpcionMenuLateral(
                icono = Icons.Rounded.Groups,
                titulo = "Mis Grupos",
                subtitulo = "Ver todos los horarios",
                onClick = {
                    onCerrar()
                    onIrAGrupos()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OpcionMenuLateral(
                icono = Icons.Rounded.MeetingRoom,
                titulo = "Salones",
                subtitulo = "Directorio de aulas",
                onClick = {
                    onCerrar()
                    onIrASalones()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OpcionMenuLateral(
                icono = Icons.Rounded.AddCircle,
                titulo = "Nuevo Grupo",
                subtitulo = "Registrar materia",
                onClick = {
                    onCerrar()
                    onAgregarGrupo()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OpcionMenuLateral(
                icono = Icons.Rounded.Add,
                titulo = "Nuevo Salón",
                subtitulo = "Registrar aula física",
                onClick = {
                    onCerrar()
                    onAgregarSalon()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── TUS DATOS ──
            Text(
                text = "Tus datos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            OpcionMenuLateral(
                icono = Icons.Rounded.FileUpload,
                titulo = "Exportar respaldo",
                subtitulo = "Guarda tus datos en un archivo",
                onClick = {
                    onCerrar()
                    onExportar()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OpcionMenuLateral(
                icono = Icons.Rounded.FileDownload,
                titulo = "Importar respaldo",
                subtitulo = "Carga un archivo guardado",
                onClick = {
                    onCerrar()
                    onImportar()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OpcionMenuLateral(
                icono = Icons.Rounded.DeleteSweep,
                titulo = "Vaciar todo",
                subtitulo = "Borra salones, grupos y notas",
                peligro = true,
                onClick = {
                    onCerrar()
                    onVaciar()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tip Informativo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(
                        Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = CafeAcento,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Selecciona un día en el calendario para consultar tus aulas y clases programadas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Cerrar Sesión
            OutlinedButton(
                onClick = {
                    onCerrar()
                    onCerrarSesion()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
        }
    }
}

@Composable
private fun ItemResumen(valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = CafeAcento
        )
    }
}

@Composable
private fun OpcionMenuLateral(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
    peligro: Boolean = false
) {
    val colorIcono = if (peligro) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = colorIcono,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (peligro) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
