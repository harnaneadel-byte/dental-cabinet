package dz.cabinet.domain

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

    fun plus(other: Money): Money {
        val sum = this.amount + other.amount
        if (sum < 0L) throw ArithmeticException("DA overflow: ${this.amount} + ${other.amount}")
        return Money(sum)
    }

    fun minus(other: Money): Money {
        val diff = this.amount - other.amount
        require(diff >= 0L) { "Negative DA result: ${this.amount} - ${other.amount}" }
        return Money(diff)
    }

    fun times(factor: Int): Money {
        val product = this.amount * factor.toLong()
        if (product / factor.toLong() != this.amount) throw ArithmeticException("DA overflow on multiplication")
        return Money(product)
    }

    override fun toString(): String = "$amount DA"
}

class Rate(
    val percent: Double,
    private val description: String = ""
)

class Commission(
    val fixedAmount: Money,
    val rate: Rate? = null
) {
    val netPrice: Money get() = TODO("Net price calculation pending")
}

class Settlement(
    val totalAmount: Money,
    val paidAmount: Money
) {
    val balance: Money
        get() = totalAmount.minus(paidAmount)

    init {
        require(totalAmount.amount >= paidAmount.amount) { "Paid amount cannot exceed total" }
    }
}

class Cancellation(
    val originalAmount: Money,
    val reservationRemaining: Money
)

class FinancialStatus(
    val creditAvailable: Boolean,
    val eligibility: ClinicalEligibility
)

class ClinicalEligibility(
    val inactivityBlocks: List<String> = emptyList(),
    val implantRules: Map<String, Boolean> = emptyMap()
)

class PlanCredit(
    val receiptId: String,
    val allocatedAmount: Money
)

class DocumentNumber(
    val uuid: java.util.UUID,
    val year: Int,
    val counter: Int = 0
)

class OperationIdentity(
    val intent: String,
    val payloadKey: String
)

class Expense(
    val description: String,
    val amount: Money,
    val paymentStatus: String = "pending"
)

class FinancialFinalization(
    val terms: String,
    val finalizedAt: Long
)

class Cash(
    val sessionId: String,
    val contributions: List<Money> = emptyList(),
    val refunds: List<Money> = emptyList()
) {
    val netCash: Money by lazy {
        val inTotal = contributions.fold(Money(0L)) { a, b -> a.plus(b) }
        val outTotal = refunds.fold(Money(0L)) { a, b -> a.plus(b) }
        inTotal.minus(outTotal)
    }
}
