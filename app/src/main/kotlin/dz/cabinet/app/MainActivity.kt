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
import dz.cabinet.data.CashSessionRepository
import dz.cabinet.data.PatientRepository
import dz.cabinet.data.TreatmentRepository
import dz.cabinet.domain.Da
import dz.cabinet.domain.Outcome
import dz.designsystem.DashboardPatientItem
import dz.designsystem.DashboardReceiptItem
import dz.designsystem.DashboardScreen
import dz.designsystem.DashboardSection
import dz.designsystem.DashboardTreatmentItem
import dz.designsystem.DashboardUiState
import dz.designsystem.theme.DentalDashboardTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val patientRepository: PatientRepository by inject()
    private val treatmentRepository: TreatmentRepository by inject()
    private val cashSessionRepository: CashSessionRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val patients by patientRepository.patients.collectAsState(initial = emptyList())
            val treatments by treatmentRepository.treatments.collectAsState(initial = emptyList())
            val receipts by cashSessionRepository.receipts.collectAsState(initial = emptyList())
            val scope = rememberCoroutineScope()

            var selectedSection by remember { mutableStateOf(DashboardSection.OVERVIEW) }
            var searchQuery by remember { mutableStateOf("") }
            var statusMessage by remember { mutableStateOf<String?>(null) }

            if (selectedSection != DashboardSection.OVERVIEW) {
                BackHandler {
                    selectedSection = DashboardSection.OVERVIEW
                }
            }

            val uiState = DashboardUiState(
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
                statusMessage = statusMessage
            )

            DentalDashboardTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    DashboardScreen(
                        modifier = Modifier.padding(padding),
                        uiState = uiState,
                        onSelectSection = { selectedSection = it },
                        onSearchQueryChange = { searchQuery = it },
                        onAddPatient = { name ->
                            scope.launch {
                                when (val res = patientRepository.addPatient(name)) {
                                    is Outcome.Ok -> statusMessage = "Patient ${res.value.name} ajouté."
                                    is Outcome.Rejected -> statusMessage = "Erreur: ${res.code.name}"
                                }
                            }
                        },
                        onStartTreatment = { id ->
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
                        },
                        onRecordPayment = { patientName, label, amount ->
                            scope.launch {
                                when (val res = cashSessionRepository.recordReceipt(patientName, label, Da(amount))) {
                                    is Outcome.Ok -> statusMessage = "Encaissement de ${res.value.amount} enregistré."
                                    is Outcome.Rejected -> statusMessage = "Encaissement refusé: ${res.code.name}"
                                }
                            }
                        },
                        onClearStatusMessage = { statusMessage = null }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    DentalDashboardTheme {
        DashboardScreen()
    }
}