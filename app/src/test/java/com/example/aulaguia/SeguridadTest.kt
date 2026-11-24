package com.example.aulaguia

import com.example.aulaguia.data.Seguridad
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeguridadTest {

    @Test
    fun laContrasenaNoSeGuardaEnTextoPlano() {
        val cifrada = Seguridad.cifrar("123456")
        assertFalse(cifrada.contains("123456"))
        assertTrue(Seguridad.estaCifrada(cifrada))
    }

    @Test
    fun verificaLaContrasenaCorrectaYRechazaLaIncorrecta() {
        val cifrada = Seguridad.cifrar("miClave")
        assertTrue(Seguridad.verificar("miClave", cifrada))
        assertFalse(Seguridad.verificar("otraClave", cifrada))
    }

    @Test
    fun cadaCifradoUsaUnaSalDistinta() {
        assertNotEquals(Seguridad.cifrar("igual"), Seguridad.cifrar("igual"))
    }

    @Test
    fun aceptaContrasenasDeLaVersionAnterior() {
        assertFalse(Seguridad.estaCifrada("123456"))
        assertTrue(Seguridad.verificar("123456", "123456"))
        assertFalse(Seguridad.verificar("1234567", "123456"))
    }
}
