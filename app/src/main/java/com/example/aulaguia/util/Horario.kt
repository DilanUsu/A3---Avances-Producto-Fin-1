package com.example.aulaguia.util

import java.text.Normalizer
import java.time.DayOfWeek
import java.util.Locale

/**
 * Utilidades para el horario de un grupo.
 * Los días se guardan como máscara de bits: lunes = 1, martes = 2, miércoles = 4, ...
 * Las horas se guardan en minutos desde la medianoche; -1 significa "sin hora".
 */
object Horario {
    const val SIN_HORA = -1

    private val LOCALE_ES: Locale = Locale.forLanguageTag("es-MX")
    private val ACENTOS = Regex("\\p{Mn}+")
    private val HORA = Regex("(\\d{1,2}):(\\d{2})")

    /** Abreviaturas en orden de lunes a domingo. */
    val ABREVIATURAS = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

    private val CLAVES = listOf("lun", "mar", "mie", "jue", "vie", "sab", "dom")

    fun bit(dia: DayOfWeek): Int = 1 shl (dia.value - 1)

    fun contiene(dias: Int, dia: DayOfWeek): Boolean = dias and bit(dia) != 0

    fun alternar(dias: Int, dia: DayOfWeek): Int = dias xor bit(dia)

    /** Lee los días escritos a mano, como "Lunes y Miércoles" o "Mar-Jue", sin importar acentos. */
    fun diasDesdeTexto(texto: String): Int {
        val limpio = Normalizer.normalize(texto.lowercase(LOCALE_ES), Normalizer.Form.NFD).replace(ACENTOS, "")
        var dias = 0
        CLAVES.forEachIndexed { i, clave -> if (limpio.contains(clave)) dias = dias or (1 shl i) }
        return dias
    }

    /** Toma las dos primeras horas "hh:mm" del texto como inicio y fin. */
    fun horasDesdeTexto(texto: String): Pair<Int, Int>? {
        val horas = HORA.findAll(texto)
            .mapNotNull { m ->
                val h = m.groupValues[1].toInt()
                val min = m.groupValues[2].toInt()
                if (h in 0..23 && min in 0..59) h * 60 + min else null
            }
            .take(2)
            .toList()
        return if (horas.size == 2) horas[0] to horas[1] else null
    }

    fun formatoHora(minutos: Int): String =
        if (minutos < 0) "--:--" else "%02d:%02d".format(minutos / 60, minutos % 60)

    /** Arma el texto que se muestra, por ejemplo "Lun, Mié y Vie 08:00 - 10:00". */
    fun texto(dias: Int, inicio: Int, fin: Int): String {
        val nombres = DayOfWeek.entries.filter { contiene(dias, it) }.map { ABREVIATURAS[it.value - 1] }
        val parteDias = when (nombres.size) {
            0 -> ""
            1 -> nombres[0]
            else -> nombres.dropLast(1).joinToString(", ") + " y " + nombres.last()
        }
        val parteHoras = if (inicio >= 0 && fin >= 0) "${formatoHora(inicio)} - ${formatoHora(fin)}" else ""
        return listOf(parteDias, parteHoras).filter { it.isNotEmpty() }.joinToString(" ")
    }
}
