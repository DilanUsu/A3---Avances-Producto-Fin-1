package com.example.aulaguia.ui.componentes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.aulaguia.ui.theme.CafeAcento
import com.example.aulaguia.ui.theme.CafePrimario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Immutable
data class AvatarPreset(
    val id: String,
    val nombre: String,
    val icono: ImageVector,
    val colorFondo: Color
)

val AVATARES_PREDEFINIDOS = listOf(
    AvatarPreset("maestro_1", "Maestro", Icons.Rounded.Person, CafePrimario),
    AvatarPreset("maestra_1", "Docente", Icons.Rounded.Face, Color(0xFFD97706)),
    AvatarPreset("profesor_1", "Profesor", Icons.Rounded.School, Color(0xFF451A03)),
    AvatarPreset("sabio_1", "Búho/Sabio", Icons.Rounded.AutoStories, Color(0xFF7C2D12)),
    AvatarPreset("clase_1", "Aula", Icons.Rounded.Class, Color(0xFF9A3412)),
    AvatarPreset("psicopedagoga_1", "Pedagogo", Icons.Rounded.Psychology, Color(0xFF047857))
)

private val PRESETS_POR_ID = AVATARES_PREDEFINIDOS.associateBy { it.id }

// Caché en memoria de fotos ya decodificadas para no leer el disco otra vez
private val bitmapCache = LruCache<String, ImageBitmap>(8)

/** Lado máximo en píxeles de la foto de perfil guardada; de sobra para un avatar. */
private const val LADO_FOTO = 512

@Composable
fun AvatarUsuario(
    fotoAvatarKey: String?,
    fotoUri: String?,
    modifier: Modifier = Modifier,
    tamano: Dp = 48.dp,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    // La foto se decodifica en segundo plano; antes se hacía en el hilo principal y trababa la UI
    val bitmap by produceState(initialValue = fotoUri?.let { bitmapCache.get(it) }, fotoUri) {
        if (fotoUri.isNullOrBlank() || value != null) return@produceState
        value = withContext(Dispatchers.IO) { decodificar(context, fotoUri) }
            ?.also { bitmapCache.put(fotoUri, it) }
    }

    val preset = PRESETS_POR_ID[fotoAvatarKey] ?: AVATARES_PREDEFINIDOS.first()

    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(preset.colorFondo)
            .border(2.dp, CafeAcento, CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        val foto = bitmap
        if (foto != null) {
            Image(
                bitmap = foto,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = preset.icono,
                contentDescription = preset.nombre,
                tint = Color.White,
                modifier = Modifier.size(tamano * 0.55f)
            )
        }
    }
}

/** Fila de avatares prediseñados; se usa en Registro y en Editar Perfil. */
@Composable
fun SelectorAvatar(
    avatarSeleccionado: String,
    hayFoto: Boolean,
    onSeleccionar: (String) -> Unit,
    tamano: Dp = 56.dp
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(AVATARES_PREDEFINIDOS, key = { it.id }) { preset ->
            val esSeleccionado = !hayFoto && avatarSeleccionado == preset.id
            Box(
                modifier = Modifier
                    .size(tamano)
                    .clip(CircleShape)
                    .background(preset.colorFondo)
                    .border(
                        width = if (esSeleccionado) 3.dp else 1.dp,
                        color = if (esSeleccionado) CafeAcento else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onSeleccionar(preset.id) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = preset.icono,
                    contentDescription = preset.nombre,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

/**
 * Copia la imagen elegida a la memoria interna de la app, reducida a [LADO_FOTO] px.
 * El permiso del selector de fotos se pierde al cerrar la app, así que guardar
 * solo su Uri hacía que la foto desapareciera. Devuelve la Uri del archivo copiado.
 */
suspend fun guardarFotoPerfil(context: Context, origen: Uri): String? = withContext(Dispatchers.IO) {
    runCatching {
        val bitmap = decodificarBitmap(context, origen) ?: return@runCatching null
        val carpeta = File(context.filesDir, "fotos").apply { mkdirs() }
        val archivo = File(carpeta, "perfil_${System.currentTimeMillis()}.jpg")
        archivo.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it) }
        bitmap.recycle()
        Uri.fromFile(archivo).toString()
    }.getOrNull()
}

private fun decodificar(context: Context, uri: String): ImageBitmap? =
    runCatching { decodificarBitmap(context, uri.toUri())?.asImageBitmap() }.getOrNull()

/** Lee primero solo las medidas y luego decodifica reducido para no gastar memoria. */
private fun decodificarBitmap(context: Context, uri: Uri): Bitmap? {
    val resolver = context.contentResolver
    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, medidas) }
    if (medidas.outWidth <= 0 || medidas.outHeight <= 0) return null

    var escala = 1
    while (maxOf(medidas.outWidth, medidas.outHeight) / (escala * 2) >= LADO_FOTO) escala *= 2

    val opciones = BitmapFactory.Options().apply { inSampleSize = escala }
    return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
}
