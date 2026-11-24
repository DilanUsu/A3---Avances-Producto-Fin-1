package com.example.aulaguia.pantallas

import androidx.activity.compose.BackHandler
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


private val DIAS_SEMANA = listOf("L", "M", "M", "J", "V", "S", "D")

// ═══════════════════════════════════════════
//  COMPONENTE: Calendario Interactivo
// ═══════════════════════════════════════════

@Composable
internal fun CalendarioInteractivo(
    fechaSeleccionada: LocalDate,
    diasConClase: Set<DayOfWeek>,
    onFechaSeleccionada: (LocalDate) -> Unit
) {
    var mesActual by remember { mutableStateOf(YearMonth.from(fechaSeleccionada)) }

    val diasDelMes = remember(mesActual) {
        val primerDiaMes = mesActual.atDay(1)
        val primerDiaSemana = primerDiaMes.dayOfWeek.value // 1 = Lunes, 7 = Domingo
        val totalDias = mesActual.lengthOfMonth()

        val lista = mutableListOf<LocalDate?>()
        // Rellenar espacios vacíos al inicio de la semana
        repeat(primerDiaSemana - 1) {
            lista.add(null)
        }
        // Rellenar los días del mes
        for (dia in 1..totalDias) {
            lista.add(mesActual.atDay(dia))
        }
        lista
    }

    val tituloMes = remember(mesActual) {
        val nombreMes = mesActual.month.getDisplayName(TextStyle.FULL, LOCALE_ES)
            .replaceFirstChar { it.uppercase() }
        "$nombreMes ${mesActual.year}"
    }

    Column {
        // Encabezado con navegación de mes
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { mesActual = mesActual.minusMonths(1) }) {
                Icon(Icons.Rounded.ChevronLeft, contentDescription = "Mes anterior")
            }

            Text(
                text = tituloMes,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row {
                TextButton(onClick = {
                    val hoy = LocalDate.now()
                    mesActual = YearMonth.from(hoy)
                    onFechaSeleccionada(hoy)
                }) {
                    Text("Hoy", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = { mesActual = mesActual.plusMonths(1) }) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = "Mes siguiente")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Encabezados de días de la semana (Lun, Mar, Mié, Jue, Vie, Sáb, Dom)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            DIAS_SEMANA.forEach { d ->
                Text(
                    text = d,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        val hoy = remember { LocalDate.now() }

        // Rejilla de días del mes
        val filas = remember(diasDelMes) { diasDelMes.chunked(7) }
        filas.forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (dia in fila) {
                    if (dia != null) {
                        val esSeleccionado = dia == fechaSeleccionada
                        val esHoy = dia == hoy
                        val tieneClase = dia.dayOfWeek in diasConClase

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        esSeleccionado -> CafePrimario
                                        tieneClase -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if (esHoy && !esSeleccionado) 1.5.dp else 0.dp,
                                    color = if (esHoy && !esSeleccionado) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { onFechaSeleccionada(dia) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${dia.dayOfMonth}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (esSeleccionado || esHoy || tieneClase) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        esSeleccionado -> Color.White
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (tieneClase) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (esSeleccionado) Color.White else CafeAcento)
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.size(36.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
