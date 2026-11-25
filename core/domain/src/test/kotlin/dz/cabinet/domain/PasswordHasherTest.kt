package dz.cabinet.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PasswordHasherTest {

    @Test
    fun `hashPassword generates unique salts and verifies matching password`() {
        val secret = "Cabinet2026!".toCharArray()
        val hash1 = PasswordHasher.hashPassword(secret.copyOf())
        val hash2 = PasswordHasher.hashPassword(secret.copyOf())

        assertNotEquals(hash1.saltBase64, hash2.saltBase64)
        assertNotEquals(hash1.hashBase64, hash2.hashBase64)
        assertTrue(PasswordHasher.verifyPassword("Cabinet2026!".toCharArray(), hash1))
        assertTrue(PasswordHasher.verifyPassword("Cabinet2026!".toCharArray(), hash2))
    }

    @Test
    fun `verifyPassword rejects wrong or empty password`() {
        val hash = PasswordHasher.hashPassword("CorrectHorse1".toCharArray())
        assertFalse(PasswordHasher.verifyPassword("WrongPassword".toCharArray(), hash))
        assertFalse(PasswordHasher.verifyPassword(CharArray(0), hash))
    }

    @Test
    fun `hashPassword rejects empty password`() {
        assertThrows(IllegalArgumentException::class.java) {
            PasswordHasher.hashPassword(CharArray(0))
        }
    }
}
