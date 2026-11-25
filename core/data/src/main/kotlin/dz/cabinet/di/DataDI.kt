package dz.cabinet.data

import android.content.Context
import android.content.SharedPreferences
import dz.cabinet.domain.AuditEvent
import dz.cabinet.domain.AuditUseCase
import dz.cabinet.domain.Cash
import dz.cabinet.domain.Da
import dz.cabinet.domain.EditPermissions
import dz.cabinet.domain.ErrorCode
import dz.cabinet.domain.Money
import dz.cabinet.domain.Outcome
import dz.cabinet.domain.PasswordHash
import dz.cabinet.domain.PasswordHasher
import dz.cabinet.domain.UserAccountSummary
import dz.cabinet.domain.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.util.UUID

sealed interface AuthState {
    data class NeedsAdminSetup(val suggestedUsername: String = "admin") : AuthState
    data class LoggedOut(val defaultUsername: String = "admin") : AuthState
    data class Authenticated(val user: UserAccountSummary) : AuthState
}

interface AuthRepository {
    val authState: Flow<AuthState>
    val users: Flow<List<UserAccountSummary>>
    fun configureBootstrapAdmin(bootstrapPassword: String)
    suspend fun setupAdminAccount(username: String, password: String): Outcome<UserAccountSummary>
    suspend fun login(username: String, password: String): Outcome<UserAccountSummary>
    suspend fun createOrUpdateUser(
        username: String,
        password: String?,
        role: UserRole,
        permissions: EditPermissions
    ): Outcome<UserAccountSummary>
    suspend fun deleteUser(username: String): Outcome<Unit>
    suspend fun logout()
}

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

private data class StoredUserRecord(
    val username: String,
    val role: UserRole,
    val permissions: EditPermissions,
    val saltBase64: String,
    val hashBase64: String
) {
    fun toSummary(): UserAccountSummary = UserAccountSummary(
        username = username,
        role = role,
        permissions = permissions
    )
}

val dataModule = module {
    single<AuthRepository> { LocalAuthRepository(androidContext()) }
    single<PatientRepository> { InMemoryPatientRepository() }
    single<TreatmentRepository> { InMemoryTreatmentRepository() }
    single<CashSessionRepository> { InMemoryCashSessionRepository() }
}

class LocalAuthRepository(context: Context) : AuthRepository {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cabinet_auth_prefs", Context.MODE_PRIVATE)
    private val mutex = Mutex()

    private val accounts = linkedMapOf<String, StoredUserRecord>().apply {
        loadRecords().forEach { put(it.username.lowercase(), it) }
    }

    private val _users = MutableStateFlow(accounts.values.map { it.toSummary() })
    override val users: Flow<List<UserAccountSummary>> = _users.asStateFlow()

    private val _authState = MutableStateFlow(computeUnauthenticatedState())
    override val authState: Flow<AuthState> = _authState.asStateFlow()

    private fun computeUnauthenticatedState(): AuthState {
        val adminRecord = accounts[ADMIN_USERNAME]
        return if (accounts.isEmpty() || adminRecord == null) {
            AuthState.NeedsAdminSetup(ADMIN_USERNAME)
        } else {
            AuthState.LoggedOut(adminRecord.username)
        }
    }

    override fun configureBootstrapAdmin(bootstrapPassword: String) {
        val candidate = bootstrapPassword.trim()
        if (candidate.isBlank() || candidate == PLACEHOLDER_PASSWORD) return
        val chars = candidate.toCharArray()
        val hash = try {
            PasswordHasher.hashPassword(chars)
        } finally {
            chars.fill('\u0000')
        }
        val record = StoredUserRecord(
            username = ADMIN_USERNAME,
            role = UserRole.ADMIN,
            permissions = EditPermissions.admin(),
            saltBase64 = hash.saltBase64,
            hashBase64 = hash.hashBase64
        )
        accounts[ADMIN_USERNAME] = record
        persistRecords()
        _users.value = accounts.values.map { it.toSummary() }
        if (_authState.value !is AuthState.Authenticated) {
            _authState.value = AuthState.LoggedOut(ADMIN_USERNAME)
        }
    }

    override suspend fun setupAdminAccount(username: String, password: String): Outcome<UserAccountSummary> = mutex.withLock {
        if (accounts.containsKey(ADMIN_USERNAME)) {
            return Outcome.Rejected(ErrorCode.ALREADY_EXISTS)
        }
        val cleanUser = username.trim().ifBlank { ADMIN_USERNAME }
        if (cleanUser.length < 3 || password.length < 6) {
            return Outcome.Rejected(ErrorCode.INVALID_AMOUNT)
        }

        val pwdChars = password.toCharArray()
        val passwordHash = try {
            withContext(Dispatchers.Default) {
                PasswordHasher.hashPassword(pwdChars)
            }
        } finally {
            pwdChars.fill('\u0000')
        }

        val adminRecord = StoredUserRecord(
            username = cleanUser,
            role = UserRole.ADMIN,
            permissions = EditPermissions.admin(),
            saltBase64 = passwordHash.saltBase64,
            hashBase64 = passwordHash.hashBase64
        )
        accounts[cleanUser.lowercase()] = adminRecord
        persistRecords()
        _users.value = accounts.values.map { it.toSummary() }
        val summary = adminRecord.toSummary()
        AuditUseCase.recordEvent(cleanUser, "setup_admin_account", Outcome.Ok("admin_created"))
        _authState.value = AuthState.Authenticated(summary)
        Outcome.Ok(summary)
    }

    override suspend fun login(username: String, password: String): Outcome<UserAccountSummary> = mutex.withLock {
        val cleanUser = username.trim()
        if (cleanUser.isBlank() || password.isBlank()) {
            return Outcome.Rejected(ErrorCode.INVALID_AMOUNT)
        }
        val record = accounts[cleanUser.lowercase()] ?: run {
            AuditUseCase.recordEvent("unknown", "login_attempt", Outcome.Rejected(ErrorCode.NOT_FOUND))
            return Outcome.Rejected(ErrorCode.NOT_FOUND)
        }

        val pwdChars = password.toCharArray()
        val verified = try {
            withContext(Dispatchers.Default) {
                PasswordHasher.verifyPassword(
                    pwdChars,
                    PasswordHash(record.saltBase64, record.hashBase64)
                )
            }
        } finally {
            pwdChars.fill('\u0000')
        }

        if (!verified) {
            AuditUseCase.recordEvent(record.username, "login_attempt", Outcome.Rejected(ErrorCode.CONFLICT))
            return Outcome.Rejected(ErrorCode.CONFLICT)
        }

        val summary = record.toSummary()
        AuditUseCase.recordEvent(record.username, "login_success", Outcome.Ok(record.role.name))
        _authState.value = AuthState.Authenticated(summary)
        Outcome.Ok(summary)
    }

    override suspend fun createOrUpdateUser(
        username: String,
        password: String?,
        role: UserRole,
        permissions: EditPermissions
    ): Outcome<UserAccountSummary> = mutex.withLock {
        val currentAuth = _authState.value as? AuthState.Authenticated
            ?: return Outcome.Rejected(ErrorCode.CONFLICT)
        if (!currentAuth.user.permissions.canManageUsers) {
            return Outcome.Rejected(ErrorCode.CONFLICT)
        }

        val cleanUser = username.trim()
        if (cleanUser.length < 3) return Outcome.Rejected(ErrorCode.INVALID_AMOUNT)
        val key = cleanUser.lowercase()
        val existing = accounts[key]

        val resolvedHash = if (!password.isNullOrEmpty()) {
            if (password.length < 6) return Outcome.Rejected(ErrorCode.INVALID_AMOUNT)
            val chars = password.toCharArray()
            try {
                withContext(Dispatchers.Default) {
                    PasswordHasher.hashPassword(chars)
                }
            } finally {
                chars.fill('\u0000')
            }
        } else if (existing != null) {
            PasswordHash(existing.saltBase64, existing.hashBase64)
        } else {
            return Outcome.Rejected(ErrorCode.INVALID_AMOUNT)
        }

        val enforcedPermissions = if (key == ADMIN_USERNAME || role == UserRole.ADMIN) {
            EditPermissions.admin()
        } else {
            permissions
        }
        val enforcedRole = if (key == ADMIN_USERNAME) UserRole.ADMIN else role

        val updated = StoredUserRecord(
            username = existing?.username ?: cleanUser,
            role = enforcedRole,
            permissions = enforcedPermissions,
            saltBase64 = resolvedHash.saltBase64,
            hashBase64 = resolvedHash.hashBase64
        )
        accounts[key] = updated
        persistRecords()
        _users.value = accounts.values.map { it.toSummary() }

        val summary = updated.toSummary()
        if (currentAuth.user.username.equals(summary.username, ignoreCase = true)) {
            _authState.value = AuthState.Authenticated(summary)
        }
        AuditUseCase.recordEvent(currentAuth.user.username, "manage_user_${summary.username}", Outcome.Ok(summary.role.name))
        Outcome.Ok(summary)
    }

    override suspend fun deleteUser(username: String): Outcome<Unit> = mutex.withLock {
        val currentAuth = _authState.value as? AuthState.Authenticated
            ?: return Outcome.Rejected(ErrorCode.CONFLICT)
        if (!currentAuth.user.permissions.canManageUsers) {
            return Outcome.Rejected(ErrorCode.CONFLICT)
        }
        val key = username.trim().lowercase()
        if (key == ADMIN_USERNAME || key == currentAuth.user.username.lowercase()) {
            return Outcome.Rejected(ErrorCode.CONFLICT)
        }
        if (accounts.remove(key) == null) {
            return Outcome.Rejected(ErrorCode.NOT_FOUND)
        }
        persistRecords()
        _users.value = accounts.values.map { it.toSummary() }
        AuditUseCase.recordEvent(currentAuth.user.username, "delete_user_$key", Outcome.Ok("deleted"))
        Outcome.Ok(Unit)
    }

    override suspend fun logout() = mutex.withLock {
        val current = (_authState.value as? AuthState.Authenticated)?.user?.username
        if (current != null) {
            AuditUseCase.recordEvent(current, "logout", Outcome.Ok("signed_out"))
        }
        _authState.value = computeUnauthenticatedState()
    }

    private fun loadRecords(): List<StoredUserRecord> {
        val raw = prefs.getString(KEY_ACCOUNTS_JSON, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val role = runCatching { UserRole.valueOf(obj.getString("role")) }.getOrDefault(UserRole.PRACTITIONER)
                    val perms = EditPermissions(
                        canEditPatients = obj.optBoolean("canEditPatients", true),
                        canEditTreatments = obj.optBoolean("canEditTreatments", true),
                        canRecordPayments = obj.optBoolean("canRecordPayments", true),
                        canManageUsers = obj.optBoolean("canManageUsers", role == UserRole.ADMIN)
                    )
                    add(
                        StoredUserRecord(
                            username = obj.getString("username"),
                            role = role,
                            permissions = perms,
                            saltBase64 = obj.getString("salt"),
                            hashBase64 = obj.getString("hash")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun persistRecords() {
        val array = JSONArray()
        accounts.values.forEach { rec ->
            array.put(
                JSONObject().apply {
                    put("username", rec.username)
                    put("role", rec.role.name)
                    put("canEditPatients", rec.permissions.canEditPatients)
                    put("canEditTreatments", rec.permissions.canEditTreatments)
                    put("canRecordPayments", rec.permissions.canRecordPayments)
                    put("canManageUsers", rec.permissions.canManageUsers)
                    put("salt", rec.saltBase64)
                    put("hash", rec.hashBase64)
                }
            )
        }
        prefs.edit().putString(KEY_ACCOUNTS_JSON, array.toString()).apply()
    }

    private companion object {
        const val ADMIN_USERNAME = "admin"
        const val PLACEHOLDER_PASSWORD = "ADMIN_BOOTSTRAP_PASSWORD_DEFAULT_VALUE"
        const val KEY_ACCOUNTS_JSON = "auth_accounts_v2"
    }
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
        AuditUseCase.recordEvent("admin", "open_cash_session", Outcome.Ok("SESSION-2026-11"))
    )

    private val _receipts = MutableStateFlow(receiptStore.toList())
    override val receipts: Flow<List<CashReceiptEntity>> = _receipts.asStateFlow()

    private val _auditEvents = MutableStateFlow(auditStore.toList())
    override val auditEvents: Flow<List<AuditEvent>> = _auditEvents.asStateFlow()

    private var nextId = 5L

    override suspend fun recordReceipt(patientName: String, label: String, amount: Money): Outcome<CashReceiptEntity> = mutex.withLock {
        if (patientName.isBlank() || label.isBlank() || amount.amount <= 0L) {
            val rejected = Outcome.Rejected(ErrorCode.INVALID_AMOUNT)
            auditStore.add(AuditUseCase.recordEvent("cabinet", "record_receipt", rejected))
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
        auditStore.add(AuditUseCase.recordEvent("cabinet", "record_receipt", Outcome.Ok("receipt-${entity.id}")))
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
