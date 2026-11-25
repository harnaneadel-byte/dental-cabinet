package dz.cabinet.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AuditUseCaseTest {

    @Test
    fun `validateOperation marks Ok outcome as valid`() {
        val result = AuditUseCase.validateOperation(
            operation = "insert_patient",
            result = Outcome.Ok("patient-1")
        )
        assertTrue(result.isValid)
        assertEquals(ComplianceStatus.COMPLIANT, result.event.complianceStatus)
        assertTrue(result.recommendations.isEmpty())
    }

    @Test
    fun `validateOperation flags overflow rejection as fatal and invalid`() {
        val result = AuditUseCase.validateOperation(
            operation = "apply_payment",
            result = Outcome.Rejected(ErrorCode.OVERFLOW)
        )
        assertFalse(result.isValid)
        assertEquals(ComplianceStatus.NON_COMPLIANT_FATAL, result.event.complianceStatus)
        assertTrue(result.recommendations.any { it.contains("Arithmetic overflow") })
    }

    @Test
    fun `generateReport formats counts and recent events`() {
        val e1 = AuditUseCase.recordEvent("dr.bouzid", "create_plan", Outcome.Ok("ok"))
        val e2 = AuditUseCase.recordEvent("dr.bouzid", "apply_payment", Outcome.Rejected(ErrorCode.INSUFFICIENT_CREDIT))
        val report = AuditUseCase.generateReport(listOf(e1, e2))

        assertTrue(report.contains("AUDIT REPORT"))
        assertTrue(report.contains("Total Events: 2"))
        assertTrue(report.contains("Compliant: 1"))
        assertTrue(report.contains("Non-Compliant: 1"))
        assertTrue(report.contains("INSUFFICIENT_CREDIT"))
    }
}
