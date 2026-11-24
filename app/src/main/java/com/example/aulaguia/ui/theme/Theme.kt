package com.example.aulaguia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════
//  Tema principal de Aulateca
//  Colores cálidos café / marrón
// ═══════════════════════════════════════════

private val EsquemaClaro = lightColorScheme(
    primary            = CafePrimario,
    onPrimary          = Color.White,
    primaryContainer   = CafeAcento,
    onPrimaryContainer = CafePrimario,
    secondary          = CafePrimarioLight,
    onSecondary        = Color.White,
    background         = FondoClaro,
    onBackground       = TextoPrimarioClaro,
    surface            = SuperficieClara,
    onSurface          = TextoPrimarioClaro,
    surfaceVariant     = TarjetaClara,
    onSurfaceVariant   = TextoSecundarioClaro,
    outline            = CafeAcento,
)

private val EsquemaOscuro = darkColorScheme(
    primary            = CafeAcento,
    onPrimary          = FondoOscuro,
    primaryContainer   = CafePrimario,
    onPrimaryContainer = CafeAcento,
    secondary          = CafePrimarioLight,
    onSecondary        = FondoOscuro,
    background         = FondoOscuro,
    onBackground       = TextoPrimarioOscuro,
    surface            = SuperficieOscura,
    onSurface          = TextoPrimarioOscuro,
    surfaceVariant     = TarjetaOscura,
    onSurfaceVariant   = TextoSecundarioOscuro,
    outline            = CafePrimario,
)

@Composable
fun AulatecaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val esquema = if (darkTheme) EsquemaOscuro else EsquemaClaro

    // Los colores de las barras del sistema los ajusta enableEdgeToEdge() en MainActivity
    MaterialTheme(
        colorScheme = esquema,
        typography = AppTypography,
        content = content
    )
}
