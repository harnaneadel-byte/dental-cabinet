package dz.cabinet.domain

import org.koin.dsl.module
import org.koin.java.KoinApplication
import org.koin.module

val domainModule = module {
    // Existing use cases
    factory { GetItemsByCategoryUseCase() }
    factory { MoneyUseCase() }
    factory { RateUseCase() }
    factory { CommissionUseCase() }
    factory { PlanCreditUseCase() }
    factory { CancellationUseCase() }
    factory { SettlementUseCase() }
    factory { CashUseCase() }
    factory { ExpenseUseCase() }
    factory { ClinicalEligibilityUseCase() }

    // New use case
    factory { AuditUseCase() }

    // Domain value objects
    singleton { Da() }
    singleton { Outcome() }
    singleton { ErrorCode() }
}