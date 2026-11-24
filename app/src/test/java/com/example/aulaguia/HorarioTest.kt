package com.example.aulaguia

import com.example.aulaguia.util.Horario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class HorarioTest {

    @Test
    fun leeDiasConYSinAcentos() {
        val dias = Horario.diasDesdeTexto("Lunes y Miercoles 08:00 - 10:00")
        assertTrue(Horario.contiene(dias, DayOfWeek.MONDAY))
        assertTrue(Horario.contiene(dias, DayOfWeek.WEDNESDAY))
        assertFalse(Horario.contiene(dias, DayOfWeek.TUESDAY))
        assertEquals(dias, Horario.diasDesdeTexto("LUN Y MIÉ"))
    }

    @Test
    fun leeSabadoYDomingo() {
        val dias = Horario.diasDesdeTexto("Sábado, Dom")
        assertTrue(Horario.contiene(dias, DayOfWeek.SATURDAY))
        assertTrue(Horario.contiene(dias, DayOfWeek.SUNDAY))
    }

    @Test
    fun leeHorasDelTexto() {
        assertEquals(8 * 60 to 10 * 60, Horario.horasDesdeTexto("Lun 08:00 - 10:00 AM"))
        assertEquals(14 * 60 to 18 * 60 + 30, Horario.horasDesdeTexto("Vie 14:00-18:30"))
        assertNull(Horario.horasDesdeTexto("Lunes en la mañana"))
        assertNull(Horario.horasDesdeTexto("Lun 25:00 - 26:00"))
    }

    @Test
    fun alternarDiaAgregaYQuita() {
        var dias = 0
        dias = Horario.alternar(dias, DayOfWeek.FRIDAY)
        assertTrue(Horario.contiene(dias, DayOfWeek.FRIDAY))
        dias = Horario.alternar(dias, DayOfWeek.FRIDAY)
        assertEquals(0, dias)
    }

    @Test
    fun armaElTextoDelHorario() {
        val lunMieVie = Horario.bit(DayOfWeek.MONDAY) or Horario.bit(DayOfWeek.WEDNESDAY) or Horario.bit(DayOfWeek.FRIDAY)
        assertEquals("Lun, Mié y Vie 08:00 - 10:00", Horario.texto(lunMieVie, 8 * 60, 10 * 60))
        assertEquals("Mar", Horario.texto(Horario.bit(DayOfWeek.TUESDAY), -1, -1))
        assertEquals("07:05", Horario.formatoHora(7 * 60 + 5))
    }
}
