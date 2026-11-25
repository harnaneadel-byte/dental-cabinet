package dz.cabinet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dz.cabinet.data.AuthRepository
import dz.cabinet.data.AuthState
import dz.cabinet.data.CashSessionRepository
import dz.cabinet.data.PatientRepository
import dz.cabinet.data.TreatmentRepository
import dz.cabinet.domain.Da
import dz.cabinet.domain.EditPermissions
import dz.cabinet.domain.ErrorCode
import dz.cabinet.domain.Outcome
import dz.cabinet.domain.UserRole
import dz.designsystem.DashboardPatientItem
import dz.designsystem.DashboardReceiptItem
import dz.designsystem.DashboardScreen
import dz.designsystem.DashboardSection
import dz.designsystem.DashboardTreatmentItem
import dz.designsystem.DashboardUiState
import dz.designsystem.DashboardUserItem
import dz.designsystem.LoginScreen
import dz.designsystem.theme.DentalDashboardTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val authRepository: AuthRepository by inject()
    private val patientRepository: PatientRepository by inject()
    private val treatmentRepository: TreatmentRepository by inject()
    private val cashSessionRepository: CashSessionRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val authState by authRepository.authState.collectAsState(initial = AuthState.NeedsAdminSetup())
            val users by authRepository.users.collectAsState(initial = emptyList())
            val patients by patientRepository.patients.collectAsState(initial = emptyList())
            val treatments by treatmentRepository.treatments.collectAsState(initial = emptyList())
            val receipts by cashSessionRepository.receipts.collectAsState(initial = emptyList())
            val scope = rememberCoroutineScope()

            var authLoading by remember { mutableStateOf(false) }
            var authError by remember { mutableStateOf<String?>(null) }

            var selectedSection by remember { mutableStateOf(DashboardSection.OVERVIEW) }
            var searchQuery by remember { mutableStateOf("") }
            var statusMessage by remember { mutableStateOf<String?>(null) }

            if (authState is AuthState.Authenticated && selectedSection != DashboardSection.OVERVIEW) {
                BackHandler {
                    selectedSection = DashboardSection.OVERVIEW
                }
            }

            DentalDashboardTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    when (val currentAuth = authState) {
                        is AuthState.NeedsAdminSetup -> {
                            LoginScreen(
                                modifier = Modifier.padding(padding),
                                isFirstRunSetup = true,
                                initialUsername = currentAuth.suggestedUsername,
                                isLoading = authLoading,
                                errorMessage = authError,
                                onSubmitSetup = { username, password ->
                                    authLoading = true
                                    authError = null
                                    scope.launch {
                                        val res = authRepository.setupAdminAccount(username, password)
                                        authLoading = false
                                        if (res is Outcome.Rejected) {
                                            authError = when (res.code) {
                                                ErrorCode.INVALID_AMOUNT -> "Identifiant (min 3) ou mot de passe (min 6) invalide."
                                                ErrorCode.ALREADY_EXISTS -> "Un compte administrateur est déjà configuré."
                                                else -> "Impossible de configurer le compte administrateur."
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        is AuthState.LoggedOut -> {
                            LoginScreen(
                                modifier = Modifier.padding(padding),
                                isFirstRunSetup = false,
                                initialUsername = currentAuth.defaultUsername,
                                isLoading = authLoading,
                                errorMessage = authError,
                                onSubmitLogin = { username, password ->
                                    authLoading = true
                                    authError = null
                                    scope.launch {
                                        val res = authRepository.login(username, password)
                                        authLoading = false
                                        if (res is Outcome.Rejected) {
                                            authError = "Nom d'utilisateur ou mot de passe incorrect."
                                        }
                                    }
                                }
                            )
                        }

                        is AuthState.Authenticated -> {
                            val currentUser = currentAuth.user
                            val uiState = DashboardUiState(
                                currentUsername = currentUser.username,
                                isAdmin = currentUser.isAdmin,
                                canEditPatients = currentUser.permissions.canEditPatients,
                                canEditTreatments = currentUser.permissions.canEditTreatments,
                                canRecordPayments = currentUser.permissions.canRecordPayments,
                                selectedSection = selectedSection,
                                searchQuery = searchQuery,
                                patients = patients.map {
                                    DashboardPatientItem(
                                        id = it.id,
                                        name = it.name,
                                        scheduledAppointments = it.scheduledAppointments
                                    )
                                },
                                treatments = treatments.map {
                                    DashboardTreatmentItem(
                                        id = it.id,
                                        patientName = it.patientName,
                                        name = it.name,
                                        status = it.status,
                                        duration = it.duration,
                                        priceDa = it.price.amount
                                    )
                                },
                                receipts = receipts.map {
                                    DashboardReceiptItem(
                                        id = it.id,
                                        patientName = it.patientName,
                                        label = it.label,
                                        amountDa = it.amount.amount
                                    )
                                },
                                users = users.map {
                                    DashboardUserItem(
                                        username = it.username,
                                        roleLabel = it.role.name,
                                        isAdmin = it.isAdmin,
                                        canEditPatients = it.permissions.canEditPatients,
                                        canEditTreatments = it.permissions.canEditTreatments,
                                        canRecordPayments = it.permissions.canRecordPayments
                                    )
                                },
                                statusMessage = statusMessage
                            )

                            DashboardScreen(
                                modifier = Modifier.padding(padding),
                                uiState = uiState,
                                onSelectSection = { selectedSection = it },
                                onSearchQueryChange = { searchQuery = it },
                                onAddPatient = { name ->
                                    if (!currentUser.permissions.canEditPatients) {
                                        statusMessage = "Action non autorisée."
                                    } else {
                                        scope.launch {
                                            when (val res = patientRepository.addPatient(name)) {
                                                is Outcome.Ok -> statusMessage = "Patient ${res.value.name} ajouté."
                                                is Outcome.Rejected -> statusMessage = "Erreur: ${res.code.name}"
                                            }
                                        }
                                    }
                                },
                                onStartTreatment = { id ->
                                    if (!currentUser.permissions.canEditTreatments) {
                                        statusMessage = "Action non autorisée."
                                    } else {
                                        scope.launch {
                                            val current = treatments.find { it.id == id }
                                            val res = if (current?.status == "In Progress") {
                                                treatmentRepository.completeTreatment(id)
                                            } else {
                                                treatmentRepository.startTreatment(id)
                                            }
                                            if (res is Outcome.Rejected) {
                                                statusMessage = "Erreur: ${res.code.name}"
                                            }
                                        }
                                    }
                                },
                                onRecordPayment = { patientName, label, amount ->
                                    if (!currentUser.permissions.canRecordPayments) {
                                        statusMessage = "Action non autorisée."
                                    } else {
                                        scope.launch {
                                            when (val res = cashSessionRepository.recordReceipt(patientName, label, Da(amount))) {
                                                is Outcome.Ok -> statusMessage = "Encaissement de ${res.value.amount} enregistré."
                                                is Outcome.Rejected -> statusMessage = "Encaissement refusé: ${res.code.name}"
                                            }
                                        }
                                    }
                                },
                                onCreateOrUpdateUser = { username, password, isAdminRole, editPatients, editTreatments, recordPayments ->
                                    scope.launch {
                                        val role = if (isAdminRole) UserRole.ADMIN else UserRole.PRACTITIONER
                                        val perms = EditPermissions(
                                            canEditPatients = editPatients,
                                            canEditTreatments = editTreatments,
                                            canRecordPayments = recordPayments,
                                            canManageUsers = isAdminRole
                                        )
                                        when (val res = authRepository.createOrUpdateUser(username, password, role, perms)) {
                                            is Outcome.Ok -> statusMessage = "Compte ${res.value.username} mis à jour."
                                            is Outcome.Rejected -> statusMessage = "Erreur: ${res.code.name}"
                                        }
                                    }
                                },
                                onDeleteUser = { username ->
                                    scope.launch {
                                        when (val res = authRepository.deleteUser(username)) {
                                            is Outcome.Ok -> statusMessage = "Compte $username supprimé."
                                            is Outcome.Rejected -> statusMessage = "Suppression refusée: ${res.code.name}"
                                        }
                                    }
                                },
                                onClearStatusMessage = { statusMessage = null },
                                onLogout = {
                                    authError = null
                                    selectedSection = DashboardSection.OVERVIEW
                                    scope.launch { authRepository.logout() }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    DentalDashboardTheme(darkTheme = true) {
        LoginScreen(isFirstRunSetup = true, initialUsername = "admin")
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    DentalDashboardTheme {
        DashboardScreen()
    }
}