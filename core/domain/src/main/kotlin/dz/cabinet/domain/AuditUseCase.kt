package dz.cabinet.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class ComplianceStatus {
    COMPLIANT,
    REQUIRES_REVIEW,
    NON_COMPLIANT,
    NON_COMPLIANT_FATAL
}

data class AuditEvent(
    val id: String,
    val timestamp: Long,
    val operator: String,
    val operation: String,
    val result: Outcome<String>,
    val details: String? = null,
    val complianceStatus: ComplianceStatus = ComplianceStatus.REQUIRES_REVIEW
)

data class AuditFinding(
    val rule: String,
    val severity: String,
    val message: String,
    val isResolved: Boolean
)

data class AuditResult(
    val event: AuditEvent,
    val findings: List<AuditFinding>,
    val isValid: Boolean,
    val recommendations: List<String>
)

object AuditUseCase {

    fun recordEvent(
        operator: String,
        operation: String,
        result: Outcome<String>,
        details: String? = null,
        compliance: ComplianceStatus = when (result) {
            is Outcome.Ok -> ComplianceStatus.COMPLIANT
            is Outcome.Rejected -> if (result.code == ErrorCode.OVERFLOW || result.code == ErrorCode.FATAL_ERROR) {
                ComplianceStatus.NON_COMPLIANT_FATAL
            } else {
                ComplianceStatus.NON_COMPLIANT
            }
        }
    ): AuditEvent {
        return AuditEvent(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            operator = operator.ifBlank { "system" },
            operation = operation,
            result = result,
            details = details,
            complianceStatus = compliance
        )
    }

    fun validateOperation(
        operation: String,
        result: Outcome<String>,
        context: Map<String, Any?>? = null
    ): AuditResult {
        val event = recordEvent("system", operation, result)
        val findings = mutableListOf<AuditFinding>()

        when (result) {
            is Outcome.Ok -> {
                findings.add(
                    AuditFinding(
                        rule = "positive_outcome",
                        severity = "info",
                        message = "Operation completed successfully",
                        isResolved = true
                    )
                )
            }
            is Outcome.Rejected -> {
                findings.add(
                    AuditFinding(
                        rule = "operation_failure",
                        severity = "error",
                        message = "Operation failed: ${result.code.name}",
                        isResolved = false
                    )
                )

                when (result.code) {
                    ErrorCode.INVALID_AMOUNT -> {
                        findings.add(
                            AuditFinding(
                                rule = "amount_validation",
                                severity = "error",
                                message = "Amount validation failed - check DA arithmetic",
                                isResolved = false
                            )
                        )
                    }
                    ErrorCode.OVERFLOW -> {
                        findings.add(
                            AuditFinding(
                                rule = "overflow_check",
                                severity = "fatal",
                                message = "Arithmetic overflow detected - requires investigation",
                                isResolved = false
                            )
                        )
                    }
                    ErrorCode.INSUFFICIENT_CREDIT -> {
                        findings.add(
                            AuditFinding(
                                rule = "credit_check",
                                severity = "warning",
                                message = "Insufficient credit - review before proceeding",
                                isResolved = false
                            )
                        )
                    }
                    else -> Unit
                }
            }
        }

        findings.add(
            AuditFinding(
                rule = "baseline_check",
                severity = "info",
                message = "Audit trail entry created for operation compliance tracking",
                isResolved = true
            )
        )

        return AuditResult(
            event = event,
            findings = findings.toList(),
            isValid = findings.none { (it.severity == "error" || it.severity == "fatal") && !it.isResolved },
            recommendations = buildRecommendations(findings)
        )
    }

    private fun buildRecommendations(findings: List<AuditFinding>): List<String> {
        return findings.mapNotNull { finding ->
            when {
                (finding.severity == "error" || finding.severity == "fatal") && !finding.isResolved ->
                    "Resolve: ${finding.message}"
                finding.severity == "warning" && !finding.isResolved ->
                    "Review: ${finding.message}"
                else -> null
            }
        }
    }

    fun generateReport(events: List<AuditEvent>): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val sb = StringBuilder()
        sb.append("=").append("-".repeat(59)).append("\n")
        sb.append("AUDIT REPORT").append("\n")
        sb.append("=").append("-".repeat(59)).append("\n")
        sb.append("Total Events: ").append(events.size).append("\n")
        sb.append("Generated: ").append(formatter.format(Date(System.currentTimeMillis()))).append("\n")
        sb.append("-").append("-".repeat(59)).append("\n")

        val compliantCount = events.count { it.complianceStatus == ComplianceStatus.COMPLIANT }
        val nonCompliantCount = events.count { it.complianceStatus == ComplianceStatus.NON_COMPLIANT }
        val fatalCount = events.count { it.complianceStatus == ComplianceStatus.NON_COMPLIANT_FATAL }

        sb.append("Compliant: $compliantCount\n")
        sb.append("Non-Compliant: $nonCompliantCount\n")
        sb.append("Fatal: $fatalCount\n")
        sb.append("-").append("-".repeat(59)).append("\n")
        sb.append("Recent Events:\n")

        for (event in events.takeLast(10)) {
            val outcomeTag = when (event.result) {
                is Outcome.Ok -> " [OK]"
                is Outcome.Rejected -> " [${event.result.code.name}]"
            }
            sb.append("  [${event.timestamp}] ${event.operator} - ${event.operation}")
                .append(outcomeTag).append("\n")
        }

        sb.append("=").append("-".repeat(59)).append("\n")
        return sb.toString()
    }
}
