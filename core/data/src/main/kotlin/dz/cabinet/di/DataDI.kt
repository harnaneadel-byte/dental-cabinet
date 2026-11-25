package dz.cabinet.data

import org.koin.dsl.module

interface PatientRepository {
    suspend fun insertPatient(id: Long, name: String, uuid: String): Outcome<Unit>
    suspend fun getPatient(id: Long): Outcome<PatientEntity>
    suspend fun getAllPatients(): List<PatientEntity>
}

data class PatientEntity(
    val id: Long,
    val name: String,
    val uuid: String,
    val createdAt: Long,
    val updatedAt: Long
)

val dataModule = module {
    factory<PatientRepository> { InMemoryPatientRepository() }
}

class InMemoryPatientRepository : PatientRepository {
    private val store = mutableMapOf<Long, PatientEntity>()
    private var nextId = 1L

    override suspend fun insertPatient(id: Long, name: String, uuid: String): Outcome<Unit> {
        if (name.isBlank()) return Outcome.Rejected(ErrorCode.INVALID_AMOUNT)
        store[id] = PatientEntity(id, name, uuid, System.currentTimeMillis(), System.currentTimeMillis())
        return Outcome.Ok(Unit)
    }

    override suspend fun getPatient(id: Long): Outcome<PatientEntity> =
        store[id]?.let { Outcome.Ok(it) } ?: Outcome.Rejected(ErrorCode.NOT_FOUND)

    override suspend fun getAllPatients(): List<PatientEntity> = store.values.toList()
}
