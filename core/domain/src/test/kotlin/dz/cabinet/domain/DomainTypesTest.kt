package dz.cabinet.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class DomainTypesTest {

    @Test
    fun `Da helper creates exact Money amount`() {
        assertEquals(Money(2500L), Da(2500L))
    }

    @Test
    fun `Commission computes net price with rate`() {
        val commission = Commission(fixedAmount = Da(10_000L), rate = Rate(20.0, "Practitioner share"))
        assertEquals(Da(2_000L), commission.commissionAmount)
        assertEquals(Da(8_000L), commission.netPrice)
    }

    @Test
    fun `Commission without rate keeps full fixed amount`() {
        val commission = Commission(fixedAmount = Da(5_000L))
        assertEquals(Da(5_000L), commission.netPrice)
    }

    @Test
    fun `Settlement tracks balance and rejects overpayment`() {
        val settlement = Settlement(totalAmount = Da(12_000L), paidAmount = Da(4_000L))
        assertEquals(Da(8_000L), settlement.balance)

        val partial = settlement.applyPayment(Da(5_000L))
        assertTrue(partial is Outcome.Ok)
        assertEquals(Da(3_000L), (partial as Outcome.Ok).value.balance)

        val overpay = settlement.applyPayment(Da(9_000L))
        assertEquals(Outcome.Rejected(ErrorCode.OVERFLOW), overpay)
    }

    @Test
    fun `Cancellation calculates released amount correctly`() {
        val cancellation = Cancellation(originalAmount = Da(15_000L), reservationRemaining = Da(3_000L))
        assertEquals(Da(12_000L), cancellation.releasedAmount)
    }

    @Test
    fun `FinancialStatus requires both credit and clinical eligibility`() {
        val blocked = FinancialStatus(
            creditAvailable = true,
            eligibility = ClinicalEligibility(inactivityBlocks = listOf("Patient inactive"))
        )
        assertFalse(blocked.canProceedWithCare)

        val eligible = FinancialStatus(
            creditAvailable = true,
            eligibility = ClinicalEligibility(implantRules = mapOf("implant_molar" to true))
        )
        assertTrue(eligible.canProceedWithCare)
        assertTrue(eligible.eligibility.isImplantEligible("implant_molar"))
    }

    @Test
    fun `DocumentNumber formats year and zero-padded counter`() {
        val doc = DocumentNumber(uuid = UUID.randomUUID(), year = 2026, counter = 42)
        assertEquals("DOC-2026-0042", doc.formatted())
    }

    @Test
    fun `OperationIdentity detects idempotent vs conflicting payload`() {
        val op1 = OperationIdentity(intent = "record_payment", payloadKey = "pay-001")
        val same = OperationIdentity(intent = "record_payment", payloadKey = "pay-001")
        val conflict = OperationIdentity(intent = "record_payment", payloadKey = "pay-002")

        assertTrue(op1.resolveConflict(same) is Outcome.Ok)
        assertEquals(Outcome.Rejected(ErrorCode.CONFLICT), op1.resolveConflict(conflict))
    }

    @Test
    fun `Cash netCash calculates contributions minus refunds`() {
        val cash = Cash(
            sessionId = "SESSION-2026-01",
            contributions = listOf(Da(10_000L), Da(8_000L)),
            refunds = listOf(Da(2_500L))
        )
        assertEquals(Da(18_000L), cash.totalContributions)
        assertEquals(Da(2_500L), cash.totalRefunds)
        assertEquals(Da(15_500L), cash.netCash)
    }

    @Test
    fun `Rate outside 0 to 100 throws IllegalArgumentException`() {
        assertThrows(IllegalArgumentException::class.java) {
            Rate(110.0)
        }
    }
}
