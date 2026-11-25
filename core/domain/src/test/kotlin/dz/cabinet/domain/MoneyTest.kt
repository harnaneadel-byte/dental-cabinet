package dz.cabinet.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MoneyTest {
    @Test
    fun `addition produces correct sum`() {
        val a = Money(100L)
        val b = Money(200L)
        assertEquals(Money(300L), a + b)
    }

    @Test
    fun `subtraction prevents negative result`() {
        val a = Money(50L)
        val b = Money(100L)
        assertThrows(ArithmeticException::class.java) {
            a - b
        }
    }

    @Test
    fun `multiplication overflows on large factor`() {
        val big = Money(Long.MAX_VALUE / 2)
        assertThrows(ArithmeticException::class.java) {
            big * 3
        }
    }

    @Test
    fun `negative amount is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            Money(-1L)
        }
    }
}
