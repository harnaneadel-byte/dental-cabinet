package dz.cabinet.data

import dz.cabinet.domain.AuditEvent
import dz.cabinet.domain.AuditUseCase
import dz.cabinet.domain.Cash
import dz.cabinet.domain.Da
import dz.cabinet.domain.ErrorCode
import dz.cabinet.domain.Money
import dz.cabinet.domain.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.dsl.module
import java.util.UUID

interface PatientRepository {
    val patients: Flow<List<PatientEntity>>
    suspend fun insertPatient(id: Long, name: String, uuid: String): Outcome<Unit>
    suspend fun addPatient(name: String, scheduledAppointments: Int = 1): Outcome<PatientEntity>
    suspend fun getPatient(id: Long): Outcome<PatientEntity>
    suspend fun getAllPatients(): List<PatientEntity>
}

interface TreatmentRepository {
    val treatments: Flow<List<TreatmentEntity>>
    suspend fun getAllTreatments(): List<TreatmentEntity>
    suspend fun startTreatment(id: Long): Outcome<TreatmentEntity>
    suspend fun completeTreatment(id: Long): Outcome<TreatmentEntity>
}

interface CashSessionRepository {
    val receipts: Flow<List<CashReceiptEntity>>
    val auditEvents: Flow<List<AuditEvent>>
    suspend fun recordReceipt(patientName: String, label: String, amount: Money): Outcome<CashReceiptEntity>
    suspend fun currentCashSession(): Cash
}

data class PatientEntity(
    val id: Long,
    val name: String,
    val uuid: String,
    val scheduledAppointments: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
)

data class TreatmentEntity(
    val id: Long,
    val planId: Long,
    val patientName: String,
    val name: String,
    val status: String,
    val duration: String,
    val price: Money,
    val createdAt: Long,
    val updatedAt: Long
)

data class CashReceiptEntity(
    val id: Long,
    val patientName: String,
    val label: String,
    val amount: Money,
    val timestamp: Long
)

val dataModule = module {
    single<PatientRepository> { InMemoryPatientRepository() }
    single<TreatmentRepository> { InMemoryTreatmentRepository() }
    single<CashSessionRepository> { InMemoryCashSessionRepository() }
}

class InMemoryPatientRepository : PatientRepository {
    private val mutex = Mutex()
    private val now = System.currentTimeMillis()
    private val initialPatients = listOf(
        PatientEntity(1L, "Amine Benali", UUID.randomUUID().toString(), 2, now - 86_400_000L, now),
        PatientEntity(2L, "Yasmine Khelifi", UUID.randomUUID().toString(), 1, now - 43_200_000L, now),
        PatientEntity(3L, "Karim Mansouri", UUID.randomUUID().toString(), 3, now - 21_600_000L, now),
        PatientEntity(4L, "Meriem Boudiaf", UUID.randomUUID().toString(), 1, now - 10_800_000L, now)
    )
    private val store = linkedMapOf<Long, PatientEntity>().apply {
        initialPatients.forEach { put(it.id, it) }
    }
    private val _patients = MutableStateFlow(store.values.toList())
    override val patients: Flow<List<PatientEntity>> = _patients.asStateFlow()
    private var nextId = 5L

    override suspend fun insertPatient(id: Long, name: String, uuid: String): Outcome<Unit> = mutex.withLock {
        if (name.isBlank() || uuid.isBlank()) return Outcome.Rejected(ErrorCode.INVALID_AMOUNT)
        val ts = System.currentTimeMillis()
        store[id] = PatientEntity(id, name.trim(), uuid, 1, ts, ts)
        if (id >= nextId) nextId = id + 1L
        _patients.value = store.values.toList()
        Outcome.Ok(Unit)
    }

    override suspend fun addPatient(name: String, scheduledAppointments: Int): Outcome<PatientEntity> = mutex.withLock {
        if (name.isBlank()) return Outcome.Rejected(ErrorCode.INVALID_AMOUNT)
        val ts = System.currentTimeMillis()
        val id = nextId++
        val entity = PatientEntity(
            id = id,
            name = name.trim(),
            uuid = UUID.randomUUID().toString(),
            scheduledAppointments = scheduledAppointments.coerceAtLeast(0),
            createdAt = ts,
            updatedAt = ts
        )
        store[id] = entity
        _patients.value = store.values.toList()
        Outcome.Ok(entity)
    }

    override suspend fun getPatient(id: Long): Outcome<PatientEntity> = mutex.withLock {
        store[id]?.let { Outcome.Ok(it) } ?: Outcome.Rejected(ErrorCode.NOT_FOUND)
    }

    override suspend fun getAllPatients(): List<PatientEntity> = mutex.withLock {
        store.values.toList()
    }
}

class InMemoryTreatmentRepository : TreatmentRepository {
    private val mutex = Mutex()
    private val now = System.currentTimeMillis()
    private val store = linkedMapOf<Long, TreatmentEntity>().apply {
        put(1L, TreatmentEntity(1L, 101L, "Amine Benali", "Détartrage complet", "Pending", "30 min", Da(3_500L), now, now))
        put(2L, TreatmentEntity(2L, 102L, "Yasmine Khelifi", "Traitement endodontique", "In Progress", "45 min", Da(12_000L), now, now))
        put(3L, TreatmentEntity(3L, 103L, "Karim Mansouri", "Pose implant dentaire", "Pending", "60 min", Da(45_000L), now, now))
        put(4L, TreatmentEntity(4L, 104L, "Meriem Boudiaf", "Composite esthétique", "Completed", "35 min", Da(5_500L), now, now))
    }
    private val _treatments = MutableStateFlow(store.values.toList())
    override val treatments: Flow<List<TreatmentEntity>> = _treatments.asStateFlow()

    override suspend fun getAllTreatments(): List<TreatmentEntity> = mutex.withLock {
        store.values.toList()
    }

    override suspend fun startTreatment(id: Long): Outcome<TreatmentEntity> = mutex.withLock {
        val current = store[id] ?: return Outcome.Rejected(ErrorCode.NOT_FOUND)
        val updated = current.copy(status = "In Progress", updatedAt = System.currentTimeMillis())
        store[id] = updated
        _treatments.value = store.values.toList()
        Outcome.Ok(updated)
    }

    override suspend fun completeTreatment(id: Long): Outcome<TreatmentEntity> = mutex.withLock {
        val current = store[id] ?: return Outcome.Rejected(ErrorCode.NOT_FOUND)
        val updated = current.copy(status = "Completed", updatedAt = System.currentTimeMillis())
        store[id] = updated
        _treatments.value = store.values.toList()
        Outcome.Ok(updated)
    }
}

class InMemoryCashSessionRepository : CashSessionRepository {
    private val mutex = Mutex()
    private val now = System.currentTimeMillis()
    private val receiptStore = mutableListOf(
        CashReceiptEntity(1L, "Meriem Boudiaf", "Composite esthétique", Da(5_500L), now - 7_200_000L),
        CashReceiptEntity(2L, "Yasmine Khelifi", "Acompte endodontie", Da(8_000L), now - 3_600_000L),
        CashReceiptEntity(3L, "Karim Mansouri", "Acompte implant", Da(20_000L), now - 1_800_000L),
        CashReceiptEntity(4L, "Amine Benali", "Consultation + Radio", Da(14_500L), now - 600_000L)
    )
    private val auditStore = mutableListOf(
        AuditUseCase.recordEvent("dr.bouzid", "open_cash_session", Outcome.Ok("SESSION-2026-11"))
    )

    private val _receipts = MutableStateFlow(receiptStore.toList())
    override val receipts: Flow<List<CashReceiptEntity>> = _receipts.asStateFlow()

    private val _auditEvents = MutableStateFlow(auditStore.toList())
    override val auditEvents: Flow<List<AuditEvent>> = _auditEvents.asStateFlow()

    private var nextId = 5L

    override suspend fun recordReceipt(patientName: String, label: String, amount: Money): Outcome<CashReceiptEntity> = mutex.withLock {
        if (patientName.isBlank() || label.isBlank() || amount.amount <= 0L) {
            val rejected = Outcome.Rejected(ErrorCode.INVALID_AMOUNT)
            auditStore.add(AuditUseCase.recordEvent("dr.bouzid", "record_receipt", rejected))
            _auditEvents.value = auditStore.toList()
            return rejected
        }
        val entity = CashReceiptEntity(
            id = nextId++,
            patientName = patientName.trim(),
            label = label.trim(),
            amount = amount,
            timestamp = System.currentTimeMillis()
        )
        receiptStore.add(0, entity)
        _receipts.value = receiptStore.toList()
        auditStore.add(AuditUseCase.recordEvent("dr.bouzid", "record_receipt", Outcome.Ok("receipt-${entity.id}")))
        _auditEvents.value = auditStore.toList()
        Outcome.Ok(entity)
    }

    override suspend fun currentCashSession(): Cash = mutex.withLock {
        Cash(
            sessionId = "SESSION-2026-11",
            contributions = receiptStore.map { it.amount }
        )
    }
}
