package dz.cabinet.domain

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class PasswordHash(
    val saltBase64: String,
    val hashBase64: String
)

object PasswordHasher {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 100_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16
    private val secureRandom = SecureRandom()

    fun hashPassword(password: CharArray): PasswordHash {
        require(password.isNotEmpty()) { "Password must not be empty" }
        val salt = ByteArray(SALT_BYTES)
        secureRandom.nextBytes(salt)
        val derived = deriveKey(password, salt)
        return PasswordHash(
            saltBase64 = Base64.getEncoder().encodeToString(salt),
            hashBase64 = Base64.getEncoder().encodeToString(derived)
        )
    }

    fun verifyPassword(password: CharArray, expected: PasswordHash): Boolean {
        if (password.isEmpty()) return false
        val salt = try {
            Base64.getDecoder().decode(expected.saltBase64)
        } catch (_: IllegalArgumentException) {
            return false
        }
        val expectedBytes = try {
            Base64.getDecoder().decode(expected.hashBase64)
        } catch (_: IllegalArgumentException) {
            return false
        }
        val actualBytes = deriveKey(password, salt)
        return try {
            MessageDigest.isEqual(actualBytes, expectedBytes)
        } finally {
            actualBytes.fill(0)
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return try {
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
