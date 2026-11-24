package com.example.aulaguia.ui.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aulaguia.ui.theme.Error
import com.example.aulaguia.ui.theme.Exito
import kotlinx.coroutines.delay

/** Resultado de una operación; el id hace que dos avisos iguales seguidos se animen de nuevo. */
data class Aviso(val exito: Boolean, val mensaje: String, val id: Long = System.nanoTime())

/**
 * Aviso centrado con una palomita verde que se dibuja sola cuando algo sale bien,
 * o una tache roja cuando falla. Desaparece solo después de un momento.
 */
@Composable
fun AvisoResultado(aviso: Aviso?, onTerminar: () -> Unit) {
    // Se conserva el último aviso para que se vea mientras termina la animación de salida
    var ultimo by remember { mutableStateOf(aviso) }
    if (aviso != null) ultimo = aviso

    LaunchedEffect(aviso?.id) {
        if (aviso != null) {
            delay(1_800)
            onTerminar()
        }
    }

    AnimatedVisibility(visible = aviso != null, enter = fadeIn(tween(200)), exit = fadeOut(tween(250))) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onTerminar() },
            contentAlignment = Alignment.Center
        ) {
            ultimo?.let { actual ->
                AnimatedVisibility(
                    visible = aviso != null,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy), initialScale = 0.6f) + fadeIn(),
                    exit = scaleOut(targetScale = 0.8f) + fadeOut()
                ) {
                    TarjetaAviso(actual)
                }
            }
        }
    }
}

@Composable
private fun TarjetaAviso(aviso: Aviso) {
    val color = if (aviso.exito) Exito else Error
    val progreso = remember(aviso.id) { Animatable(0f) }
    LaunchedEffect(aviso.id) {
        delay(150)
        progreso.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        modifier = Modifier
            .padding(32.dp)
            .widthIn(min = 220.dp, max = 300.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(64.dp)
                        .background(color, CircleShape)
                ) {
                    val trazo = Stroke(width = size.width * 0.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    val w = size.width
                    fun linea(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float? = null, y3: Float? = null) =
                        Path().apply {
                            moveTo(w * x1, w * y1)
                            lineTo(w * x2, w * y2)
                            if (x3 != null && y3 != null) lineTo(w * x3, w * y3)
                        }
                    // Dibuja solo la parte del trazo que corresponde al avance de la animación
                    fun parcialDe(figura: Path, avance: Float, destino: Path) {
                        val medida = PathMeasure().apply { setPath(figura, false) }
                        medida.getSegment(0f, medida.length * avance.coerceIn(0f, 1f), destino, true)
                    }
                    val parcial = Path()
                    if (aviso.exito) {
                        parcialDe(linea(0.28f, 0.52f, 0.44f, 0.68f, 0.73f, 0.36f), progreso.value, parcial)
                    } else {
                        // La tache tiene dos líneas: se dibujan una tras otra
                        val p = progreso.value * 2
                        parcialDe(linea(0.33f, 0.33f, 0.67f, 0.67f), p, parcial)
                        if (p > 1f) parcialDe(linea(0.67f, 0.33f, 0.33f, 0.67f), p - 1f, parcial)
                    }
                    drawPath(parcial, Color.White, style = trazo)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                aviso.mensaje,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
