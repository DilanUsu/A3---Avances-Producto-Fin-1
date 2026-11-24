package com.example.aulaguia

import com.example.aulaguia.data.GrupoRespaldo
import com.example.aulaguia.data.NotaRespaldo
import com.example.aulaguia.data.Respaldo
import com.example.aulaguia.data.SalonRespaldo
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RespaldoTest {

    private val ejemplo = Respaldo(
        exportado = "2026-11-24T10:00:00",
        salones = listOf(SalonRespaldo(7, "Aula 102", "Edificio A", "PB", "Junto a la biblioteca", 35, "Proyector")),
        grupos = listOf(GrupoRespaldo("5A", "Android", salonId = 7, salonNombre = "Aula 102 (Edificio A)", dias = 5, horaInicio = 480, horaFin = 600)),
        notas = listOf(NotaRespaldo(7, "Llevar adaptador HDMI", 123L))
    )

    @Test
    fun exportarYVolverACargarDaLosMismosDatos() {
        val texto = Respaldo.json.encodeToString(Respaldo.serializer(), ejemplo)
        val leido = Respaldo.json.decodeFromString(Respaldo.serializer(), texto)
        assertEquals(ejemplo, leido)
    }

    @Test
    fun elArchivoNoIncluyeContrasenas() {
        val texto = Respaldo.json.encodeToString(Respaldo.serializer(), ejemplo)
        assertFalse(texto.contains("contrasena"))
        assertFalse(texto.contains("email"))
    }

    @Test
    fun ignoraCamposDesconocidosDeVersionesFuturas() {
        val texto = """{"app":"Aulateca","version":2,"exportado":"x","algoNuevo":true,"salones":[],"grupos":[],"notas":[]}"""
        assertEquals("Aulateca", Respaldo.json.decodeFromString(Respaldo.serializer(), texto).app)
    }

    @Test(expected = SerializationException::class)
    fun rechazaUnArchivoQueNoEsRespaldo() {
        Respaldo.json.decodeFromString(Respaldo.serializer(), "esto no es json")
    }
}
