package dz.cabinet.domain

import java.util.UUID
import kotlin.math.roundToLong

enum class ErrorCode {
    INVALID_AMOUNT,
    OVERFLOW,
    INSUFFICIENT_CREDIT,
    NOT_FOUND,
    ALREADY_EXISTS,
    CONFLICT,
    FATAL_ERROR
}

sealed interface Outcome<out T> {
    data class Ok<T>(val value: T) : Outcome<T>
    data class Rejected(val code: ErrorCode) : Outcome<Nothing>
}

object Da {
    operator fun invoke(amount: Long): Money = Money(amount)
}

data class Money(val amount: Long) {
    init {
        require(amount >= 0) { "Amount must be non-negative, got $amount" }
    }

    operator fun plus(other: Money): Money {
        val sum = try {
            Math.addExact(this.amount, other.amount)
        } catch (e: ArithmeticException) {
            throw ArithmeticException("DA overflow: ${this.amount} + ${other.amount}")
        }
        return Money(sum)
    }

    operator fun minus(other: Money): Money {
        val diff = this.amount - other.amount
        if (diff < 0L) throw ArithmeticException("Negative DA result: ${this.amount} - ${other.amount}")
        return Money(diff)
    }

    operator fun times(factor: Int): Money {
        if (factor < 0) throw IllegalArgumentException("Factor must be non-negative, got $factor")
        if (factor == 0 || this.amount == 0L) return Money(0L)
        val product = try {
            Math.multiplyExact(this.amount, factor.toLong())
        } catch (e: ArithmeticException) {
            throw ArithmeticException("DA overflow on multiplication: ${this.amount} * $factor")
        }
        return Money(product)
    }

    override fun toString(): String = "$amount DA"
}

data class Rate(
    val percent: Double,
    val description: String = ""
) {
    init {
        require(percent in 0.0..100.0) { "Rate percent must be between 0.0 and 100.0, got $percent" }
    }
}

data class Commission(
    val fixedAmount: Money,
    val rate: Rate? = null
) {
    val commissionAmount: Money
        get() {
            if (rate == null) return Money(0L)
            val cut = (fixedAmount.amount * (rate.percent / 100.0)).roundToLong()
                .coerceIn(0L, fixedAmount.amount)
            return Money(cut)
        }

    val netPrice: Money
        get() = fixedAmount - commissionAmount
}

data class Settlement(
    val totalAmount: Money,
    val paidAmount: Money
) {
    val balance: Money
        get() = totalAmount - paidAmount

    val isFullyPaid: Boolean
        get() = balance.amount == 0L

    init {
        require(totalAmount.amount >= paidAmount.amount) { "Paid amount cannot exceed total" }
    }

    fun applyPayment(payment: Money): Outcome<Settlement> {
        if (payment.amount == 0L) return Outcome.Rejected(ErrorCode.INVALID_AMOUNT)
        val newPaid = try {
            paidAmount + payment
        } catch (e: ArithmeticException) {
            return Outcome.Rejected(ErrorCode.OVERFLOW)
        }
        if (newPaid.amount > totalAmount.amount) {
            return Outcome.Rejected(ErrorCode.OVERFLOW)
        }
        return Outcome.Ok(copy(paidAmount = newPaid))
    }
}

data class Cancellation(
    val originalAmount: Money,
    val reservationRemaining: Money
) {
    init {
        require(reservationRemaining.amount <= originalAmount.amount) {
            "Reservation remaining cannot exceed original amount"
        }
    }

    val releasedAmount: Money
        get() = originalAmount - reservationRemaining
}

data class ClinicalEligibility(
    val inactivityBlocks: List<String> = emptyList(),
    val implantRules: Map<String, Boolean> = emptyMap()
) {
    val isEligible: Boolean
        get() = inactivityBlocks.isEmpty()

    fun isImplantEligible(procedureKey: String): Boolean =
        isEligible && (implantRules[procedureKey] ?: false)
}

data class FinancialStatus(
    val creditAvailable: Boolean,
    val eligibility: ClinicalEligibility
) {
    val canProceedWithCare: Boolean
        get() = creditAvailable && eligibility.isEligible
}

data class PlanCredit(
    val receiptId: String,
    val allocatedAmount: Money
) {
    init {
        require(receiptId.isNotBlank()) { "Receipt ID must not be blank" }
    }
}

data class DocumentNumber(
    val uuid: UUID,
    val year: Int,
    val counter: Int = 0
) {
    init {
        require(year >= 2000) { "Document year must be >= 2000, got $year" }
        require(counter >= 0) { "Document counter must be non-negative, got $counter" }
    }

    fun formatted(): String = "DOC-$year-${counter.toString().padStart(4, '0')}"
}

data class OperationIdentity(
    val intent: String,
    val payloadKey: String
) {
    init {
        require(intent.isNotBlank()) { "Intent must not be blank" }
        require(payloadKey.isNotBlank()) { "Payload key must not be blank" }
    }

    fun resolveConflict(other: OperationIdentity): Outcome<OperationIdentity> = when {
        intent == other.intent && payloadKey == other.payloadKey -> Outcome.Ok(this)
        intent == other.intent -> Outcome.Rejected(ErrorCode.CONFLICT)
        else -> Outcome.Ok(other)
    }
}

data class Expense(
    val description: String,
    val amount: Money,
    val paymentStatus: String = "pending"
) {
    init {
        require(description.isNotBlank()) { "Expense description must not be blank" }
    }
}

data class FinancialFinalization(
    val terms: String,
    val finalizedAt: Long
) {
    init {
        require(terms.isNotBlank()) { "Finalization terms must not be blank" }
        require(finalizedAt > 0L) { "Finalization timestamp must be positive" }
    }
}

data class Cash(
    val sessionId: String,
    val contributions: List<Money> = emptyList(),
    val refunds: List<Money> = emptyList()
) {
    init {
        require(sessionId.isNotBlank()) { "Session ID must not be blank" }
    }

    val totalContributions: Money
        get() = contributions.fold(Money(0L)) { acc, item -> acc + item }

    val totalRefunds: Money
        get() = refunds.fold(Money(0L)) { acc, item -> acc + item }

    val netCash: Money
        get() = totalContributions - totalRefunds
}
