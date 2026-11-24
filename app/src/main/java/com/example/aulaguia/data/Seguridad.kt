package com.example.aulaguia.data

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Las contraseñas se guardan cifradas con PBKDF2 y una sal aleatoria,
 * nunca como texto plano. Formato guardado: pbkdf2$iteraciones$sal$hash
 */
object Seguridad {
    private const val PREFIJO = "pbkdf2"
    private const val ITERACIONES = 60_000
    private const val BITS = 256

    fun cifrar(contrasena: String): String {
        val sal = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = derivar(contrasena, sal, ITERACIONES)
        val b64 = Base64.getEncoder()
        return "$PREFIJO$$ITERACIONES$${b64.encodeToString(sal)}$${b64.encodeToString(hash)}"
    }

    fun verificar(contrasena: String, guardada: String): Boolean {
        val partes = guardada.split('$')
        if (partes.size != 4 || partes[0] != PREFIJO) {
            // Usuarios creados con la versión anterior (texto plano)
            return MessageDigest.isEqual(contrasena.toByteArray(), guardada.toByteArray())
        }
        val iteraciones = partes[1].toIntOrNull() ?: return false
        val b64 = Base64.getDecoder()
        val esperado = b64.decode(partes[3])
        return MessageDigest.isEqual(derivar(contrasena, b64.decode(partes[2]), iteraciones), esperado)
    }

    fun estaCifrada(guardada: String) = guardada.startsWith("$PREFIJO$")

    private fun derivar(contrasena: String, sal: ByteArray, iteraciones: Int): ByteArray {
        val spec = PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
