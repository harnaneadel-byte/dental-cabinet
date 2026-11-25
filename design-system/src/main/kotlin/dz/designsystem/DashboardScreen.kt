package dz.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dz.designsystem.components.PatientCard
import dz.designsystem.components.TreatmentCard
import dz.designsystem.theme.DentalDashboardTheme
import kotlinx.coroutines.launch

enum class DashboardSection(val label: String) {
    OVERVIEW("Tableau de bord"),
    PATIENTS("Patients"),
    AGENDA("Agenda"),
    DENTAL_CHART("Schéma dentaire"),
    TREATMENT_PLANS("Plans de traitement"),
    PAYMENTS("Encaissements"),
    ADMINISTRATION("Administration")
}

data class DashboardPatientItem(
    val id: Long,
    val name: String,
    val scheduledAppointments: Int
)

data class DashboardTreatmentItem(
    val id: Long,
    val patientName: String,
    val name: String,
    val status: String,
    val duration: String,
    val priceDa: Long
)

data class DashboardReceiptItem(
    val id: Long,
    val patientName: String,
    val label: String,
    val amountDa: Long
)

data class DashboardUserItem(
    val username: String,
    val roleLabel: String,
    val isAdmin: Boolean,
    val canEditPatients: Boolean,
    val canEditTreatments: Boolean,
    val canRecordPayments: Boolean
)

data class DashboardUiState(
    val currentUsername: String = "admin",
    val isAdmin: Boolean = true,
    val canEditPatients: Boolean = true,
    val canEditTreatments: Boolean = true,
    val canRecordPayments: Boolean = true,
    val selectedSection: DashboardSection = DashboardSection.OVERVIEW,
    val searchQuery: String = "",
    val patients: List<DashboardPatientItem> = listOf(
        DashboardPatientItem(1L, "Amine Benali", 2),
        DashboardPatientItem(2L, "Yasmine Khelifi", 1),
        DashboardPatientItem(3L, "Karim Mansouri", 3),
        DashboardPatientItem(4L, "Meriem Boudiaf", 1)
    ),
    val treatments: List<DashboardTreatmentItem> = listOf(
        DashboardTreatmentItem(1L, "Amine Benali", "Détartrage complet", "Pending", "30 min", 3500L),
        DashboardTreatmentItem(2L, "Yasmine Khelifi", "Traitement endodontique", "In Progress", "45 min", 12000L),
        DashboardTreatmentItem(3L, "Karim Mansouri", "Pose implant dentaire", "Pending", "60 min", 45000L),
        DashboardTreatmentItem(4L, "Meriem Boudiaf", "Composite esthétique", "Completed", "35 min", 5500L)
    ),
    val receipts: List<DashboardReceiptItem> = listOf(
        DashboardReceiptItem(1L, "Meriem Boudiaf", "Composite esthétique", 5500L),
        DashboardReceiptItem(2L, "Yasmine Khelifi", "Acompte endodontie", 8000L),
        DashboardReceiptItem(3L, "Karim Mansouri", "Acompte implant", 20000L),
        DashboardReceiptItem(4L, "Amine Benali", "Consultation + Radio", 14500L)
    ),
    val users: List<DashboardUserItem> = listOf(
        DashboardUserItem("admin", "ADMIN", isAdmin = true, canEditPatients = true, canEditTreatments = true, canRecordPayments = true)
    ),
    val statusMessage: String? = null
)

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    uiState: DashboardUiState = DashboardUiState(),
    onSelectSection: (DashboardSection) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onAddPatient: (String) -> Unit = {},
    onStartTreatment: (Long) -> Unit = {},
    onRecordPayment: (String, String, Long) -> Unit = { _, _, _ -> },
    onCreateOrUpdateUser: (username: String, password: String?, isAdmin: Boolean, canEditPatients: Boolean, canEditTreatments: Boolean, canRecordPayments: Boolean) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteUser: (String) -> Unit = {},
    onClearStatusMessage: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var showAddPatientDialog by remember { mutableStateOf(false) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var showCreateUserDialog by remember { mutableStateOf(false) }
    var selectedPatientDetail by remember { mutableStateOf<DashboardPatientItem?>(null) }

    DentalDashboardTheme {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val isWide = maxWidth >= 840.dp
            if (isWide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    DashboardSidebar(
                        currentUsername = uiState.currentUsername,
                        isAdmin = uiState.isAdmin,
                        selectedSection = uiState.selectedSection,
                        onSelectSection = onSelectSection,
                        onLogout = onLogout,
                        modifier = Modifier.width(260.dp).fillMaxHeight()
                    )

                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        DashboardHeader(
                            currentUsername = uiState.currentUsername,
                            isAdmin = uiState.isAdmin,
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = onSearchQueryChange,
                            showMenuButton = false,
                            onMenuClick = {},
                            onLogout = onLogout,
                            modifier = Modifier.fillMaxWidth().height(64.dp)
                        )
                        DashboardMainContent(
                            uiState = uiState,
                            isCompact = false,
                            onStartTreatment = onStartTreatment,
                            onViewPatientDetail = { selectedPatientDetail = it },
                            onOpenAddPatient = { showAddPatientDialog = true },
                            onOpenAddPayment = { showAddPaymentDialog = true },
                            onOpenCreateUser = { showCreateUserDialog = true },
                            onUpdateUserPermissions = { user, editPatients, editTreatments, recordPayments ->
                                onCreateOrUpdateUser(user.username, null, user.isAdmin, editPatients, editTreatments, recordPayments)
                            },
                            onDeleteUser = onDeleteUser,
                            onSelectSection = onSelectSection,
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        )
                    }

                    DashboardRightPanel(
                        canEditPatients = uiState.canEditPatients,
                        canRecordPayments = uiState.canRecordPayments,
                        isAdmin = uiState.isAdmin,
                        onOpenAddPatient = { showAddPatientDialog = true },
                        onOpenAddPayment = { showAddPaymentDialog = true },
                        onNavigateSection = onSelectSection,
                        modifier = Modifier.width(280.dp).fillMaxHeight()
                    )
                }
            } else {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.width(280.dp),
                            drawerContainerColor = Color(0xFF1E293B)
                        ) {
                            DashboardSidebar(
                                currentUsername = uiState.currentUsername,
                                isAdmin = uiState.isAdmin,
                                selectedSection = uiState.selectedSection,
                                onSelectSection = { section ->
                                    onSelectSection(section)
                                    scope.launch { drawerState.close() }
                                },
                                onLogout = {
                                    scope.launch { drawerState.close() }
                                    onLogout()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        DashboardHeader(
                            currentUsername = uiState.currentUsername,
                            isAdmin = uiState.isAdmin,
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = onSearchQueryChange,
                            showMenuButton = true,
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onLogout = onLogout,
                            modifier = Modifier.fillMaxWidth()
                        )
                        DashboardMainContent(
                            uiState = uiState,
                            isCompact = true,
                            onStartTreatment = onStartTreatment,
                            onViewPatientDetail = { selectedPatientDetail = it },
                            onOpenAddPatient = { showAddPatientDialog = true },
                            onOpenAddPayment = { showAddPaymentDialog = true },
                            onOpenCreateUser = { showCreateUserDialog = true },
                            onUpdateUserPermissions = { user, editPatients, editTreatments, recordPayments ->
                                onCreateOrUpdateUser(user.username, null, user.isAdmin, editPatients, editTreatments, recordPayments)
                            },
                            onDeleteUser = onDeleteUser,
                            onSelectSection = onSelectSection,
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (showAddPatientDialog) {
            AddPatientDialog(
                onDismiss = { showAddPatientDialog = false },
                onConfirm = { name ->
                    onAddPatient(name)
                    showAddPatientDialog = false
                }
            )
        }

        if (showAddPaymentDialog) {
            AddPaymentDialog(
                onDismiss = { showAddPaymentDialog = false },
                onConfirm = { patientName, label, amount ->
                    onRecordPayment(patientName, label, amount)
                    showAddPaymentDialog = false
                }
            )
        }

        if (showCreateUserDialog) {
            CreateUserDialog(
                onDismiss = { showCreateUserDialog = false },
                onConfirm = { username, password, isAdminRole ->
                    onCreateOrUpdateUser(username, password, isAdminRole, true, true, true)
                    showCreateUserDialog = false
                }
            )
        }

        selectedPatientDetail?.let { patient ->
            AlertDialog(
                onDismissRequest = { selectedPatientDetail = null },
                title = { Text(patient.name) },
                text = {
                    Text("${patient.scheduledAppointments} rendez-vous planifiés pour ce patient.")
                },
                confirmButton = {
                    TextButton(onClick = { selectedPatientDetail = null }) {
                        Text("Fermer")
                    }
                }
            )
        }

        uiState.statusMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = onClearStatusMessage,
                title = { Text("Cabinet Dentaire") },
                text = { Text(msg) },
                confirmButton = {
                    TextButton(onClick = onClearStatusMessage) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun DashboardSidebar(
    currentUsername: String = "admin",
    isAdmin: Boolean = true,
    selectedSection: DashboardSection = DashboardSection.OVERVIEW,
    onSelectSection: (DashboardSection) -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sidebarBg = Color(0xFF1E293B)
    val activeAccent = Color(0xFF3B82F6)

    Column(
        modifier = modifier.background(sidebarBg).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MedicalServices, contentDescription = "Cabinet Dentaire", tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Cabinet Dentaire", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = if (isAdmin) "$currentUsername • Administrateur" else "$currentUsername • Praticien",
                    color = Color(0xFFD4AF38),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        SidebarItem(
            label = DashboardSection.OVERVIEW.label,
            icon = Icons.Default.Dashboard,
            isActive = selectedSection == DashboardSection.OVERVIEW,
            activeColor = activeAccent,
            onClick = { onSelectSection(DashboardSection.OVERVIEW) }
        )
        SidebarItem(
            label = DashboardSection.PATIENTS.label,
            icon = Icons.Default.People,
            isActive = selectedSection == DashboardSection.PATIENTS,
            activeColor = activeAccent,
            onClick = { onSelectSection(DashboardSection.PATIENTS) }
        )
        SidebarItem(
            label = DashboardSection.AGENDA.label,
            icon = Icons.Default.CalendarToday,
            isActive = selectedSection == DashboardSection.AGENDA,
            activeColor = activeAccent,
            onClick = { onSelectSection(DashboardSection.AGENDA) }
        )

        Text("Clinique", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp))
        SidebarItem(
            label = DashboardSection.DENTAL_CHART.label,
            icon = Icons.Default.Brush,
            isActive = selectedSection == DashboardSection.DENTAL_CHART,
            activeColor = activeAccent,
            onClick = { onSelectSection(DashboardSection.DENTAL_CHART) }
        )
        SidebarItem(
            label = DashboardSection.TREATMENT_PLANS.label,
            icon = Icons.Default.Description,
            isActive = selectedSection == DashboardSection.TREATMENT_PLANS,
            activeColor = activeAccent,
            onClick = { onSelectSection(DashboardSection.TREATMENT_PLANS) }
        )

        Text("Paiements", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp))
        SidebarItem(
            label = DashboardSection.PAYMENTS.label,
            icon = Icons.Default.AccountBalanceWallet,
            isActive = selectedSection == DashboardSection.PAYMENTS,
            activeColor = activeAccent,
            onClick = { onSelectSection(DashboardSection.PAYMENTS) }
        )

        if (isAdmin) {
            Text("Système", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp))
            SidebarItem(
                label = DashboardSection.ADMINISTRATION.label,
                icon = Icons.Default.AdminPanelSettings,
                isActive = selectedSection == DashboardSection.ADMINISTRATION,
                activeColor = activeAccent,
                onClick = { onSelectSection(DashboardSection.ADMINISTRATION) }
            )
        }

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = onLogout,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF5D5C7)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .heightIn(min = 48.dp)
                .testTag("sidebar_logout_button")
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Déconnexion", modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Déconnexion", fontSize = 13.sp)
        }

        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WifiOff, contentDescription = "Mode hors ligne", tint = Color.Green, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Mode hors ligne", color = Color.White, fontSize = 12.sp)
                    Text("Données locales uniquement", color = Color.Gray, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun SidebarItem(
    label: String,
    icon: ImageVector,
    isActive: Boolean = false,
    activeColor: Color = Color.Transparent,
    onClick: () -> Unit = {}
) {
    Surface(
        color = if (isActive) activeColor else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun DashboardHeader(
    currentUsername: String = "admin",
    isAdmin: Boolean = true,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    showMenuButton: Boolean = false,
    onMenuClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, color = Color.White, shadowElevation = 1.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showMenuButton) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.testTag("drawer_menu_button")
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Ouvrir le menu")
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Rechercher un patient...", fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Rechercher", modifier = Modifier.size(18.dp)) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 400.dp)
                    .testTag("patient_search_input"),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(Modifier.width(8.dp))

            AssistChip(
                onClick = {},
                label = { Text(if (isAdmin) "Admin" else "Praticien", maxLines = 1) },
                leadingIcon = {
                    Icon(
                        if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = if (isAdmin) Color(0xFF8B6914) else Color(0xFF1E293B)
                )
            )

            if (showMenuButton) {
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.testTag("header_logout_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Déconnexion")
                }
            } else {
                Spacer(Modifier.width(8.dp))
                Text(currentUsername, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun DashboardMainContent(
    uiState: DashboardUiState,
    isCompact: Boolean,
    onStartTreatment: (Long) -> Unit,
    onViewPatientDetail: (DashboardPatientItem) -> Unit,
    onOpenAddPatient: () -> Unit,
    onOpenAddPayment: () -> Unit,
    onOpenCreateUser: () -> Unit,
    onUpdateUserPermissions: (DashboardUserItem, Boolean, Boolean, Boolean) -> Unit,
    onDeleteUser: (String) -> Unit,
    onSelectSection: (DashboardSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredPatients = remember(uiState.patients, uiState.searchQuery) {
        if (uiState.searchQuery.isBlank()) uiState.patients
        else uiState.patients.filter { it.name.contains(uiState.searchQuery, ignoreCase = true) }
    }
    val totalReceiptsDa = remember(uiState.receipts) {
        uiState.receipts.sumOf { it.amountDa }
    }
    val totalAppointments = remember(uiState.patients) {
        uiState.patients.sumOf { it.scheduledAppointments }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Bonjour ${uiState.currentUsername},",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (uiState.isAdmin) {
                            "Accès Administrateur (tous droits d'édition) — ${uiState.selectedSection.label}."
                        } else {
                            "Voici la situation de votre cabinet — ${uiState.selectedSection.label}."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF475569)
                    )
                }
                if (!isCompact) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.MedicalServices,
                                contentDescription = "Cabinet",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
        }

        // Stats Grid / Row
        if (isCompact) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Patients actifs", "${uiState.patients.size}", "dossiers locaux", Modifier.weight(1f))
                StatCard("Rendez-vous", "$totalAppointments", "planifiés", Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Encaissements", "$totalReceiptsDa DA", "session ouverte", Modifier.weight(1f))
                StatCard("Traitements", "${uiState.treatments.size}", "${uiState.treatments.count { it.status == "Pending" }} en attente", Modifier.weight(1f))
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Patients actifs", "${uiState.patients.size}", "dossiers locaux", Modifier.weight(1f))
                StatCard("Rendez-vous", "$totalAppointments", "planifiés", Modifier.weight(1f))
                StatCard("Encaissements", "$totalReceiptsDa DA", "session ouverte", Modifier.weight(1f))
                StatCard("Traitements", "${uiState.treatments.size}", "${uiState.treatments.count { it.status == "Pending" }} en attente", Modifier.weight(1f))
            }
        }

        when (uiState.selectedSection) {
            DashboardSection.PATIENTS -> {
                SectionHeader(
                    title = "Patients (${filteredPatients.size})",
                    action = if (uiState.canEditPatients) onOpenAddPatient else null,
                    actionLabel = "Nouveau patient"
                )
                if (filteredPatients.isEmpty()) {
                    EmptyStateCard("Aucun patient ne correspond à la recherche.")
                } else {
                    filteredPatients.forEach { patient ->
                        PatientCard(
                            patientName = patient.name,
                            scheduledAppointments = patient.scheduledAppointments,
                            onViewDetail = { onViewPatientDetail(patient) }
                        )
                    }
                }
            }

            DashboardSection.TREATMENT_PLANS, DashboardSection.AGENDA, DashboardSection.DENTAL_CHART -> {
                SectionHeader(uiState.selectedSection.label)
                uiState.treatments.forEach { treatment ->
                    TreatmentCard(
                        treatmentName = "${treatment.name} (${treatment.patientName})",
                        status = treatment.status,
                        duration = "${treatment.duration} • ${treatment.priceDa} DA",
                        onStartTreatment = {
                            if (uiState.canEditTreatments) onStartTreatment(treatment.id)
                        }
                    )
                }
            }

            DashboardSection.PAYMENTS -> {
                SectionHeader(
                    title = "Encaissements (${uiState.receipts.size})",
                    action = if (uiState.canRecordPayments) onOpenAddPayment else null,
                    actionLabel = "Nouvel encaissement"
                )
                DashboardReceiptsCard("Journal de caisse ($totalReceiptsDa DA)", uiState.receipts, Modifier.fillMaxWidth())
            }

            DashboardSection.ADMINISTRATION -> {
                if (uiState.isAdmin) {
                    SectionHeader(
                        title = "Utilisateurs & Droits (${uiState.users.size})",
                        action = onOpenCreateUser,
                        actionLabel = "Nouvel utilisateur"
                    )
                    uiState.users.forEach { user ->
                        AdminUserCard(
                            user = user,
                            onTogglePatients = { onUpdateUserPermissions(user, it, user.canEditTreatments, user.canRecordPayments) },
                            onToggleTreatments = { onUpdateUserPermissions(user, user.canEditPatients, it, user.canRecordPayments) },
                            onTogglePayments = { onUpdateUserPermissions(user, user.canEditPatients, user.canEditTreatments, it) },
                            onDelete = { onDeleteUser(user.username) }
                        )
                    }
                } else {
                    EmptyStateCard("Accès réservé à l'administrateur.")
                }
            }

            DashboardSection.OVERVIEW -> {
                if (isCompact) {
                    DashboardTreatmentsCard(
                        title = "Rendez-vous & Soins du jour",
                        treatments = uiState.treatments,
                        canEditTreatments = uiState.canEditTreatments,
                        onStartTreatment = onStartTreatment,
                        modifier = Modifier.fillMaxWidth()
                    )
                    DashboardReceiptsCard(
                        title = "Encaissements récents",
                        receipts = uiState.receipts,
                        modifier = Modifier.fillMaxWidth()
                    )
                    DashboardRightPanel(
                        canEditPatients = uiState.canEditPatients,
                        canRecordPayments = uiState.canRecordPayments,
                        isAdmin = uiState.isAdmin,
                        onOpenAddPatient = onOpenAddPatient,
                        onOpenAddPayment = onOpenAddPayment,
                        onNavigateSection = onSelectSection,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DashboardTreatmentsCard(
                            title = "Rendez-vous & Soins du jour",
                            treatments = uiState.treatments,
                            canEditTreatments = uiState.canEditTreatments,
                            onStartTreatment = onStartTreatment,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardReceiptsCard(
                            title = "Encaissements récents",
                            receipts = uiState.receipts,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminUserCard(
    user: DashboardUserItem,
    onTogglePatients: (Boolean) -> Unit,
    onToggleTreatments: (Boolean) -> Unit,
    onTogglePayments: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(user.username, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                    Text(
                        text = if (user.isAdmin) "Administrateur — Tous les droits d'édition" else "Praticien",
                        fontSize = 12.sp,
                        color = if (user.isAdmin) Color(0xFF8B6914) else Color.Gray
                    )
                }
                if (!user.isAdmin) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer utilisateur", tint = Color(0xFFB32630))
                    }
                }
            }

            if (!user.isAdmin) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Éditer patients", fontSize = 13.sp, color = Color(0xFF1E293B))
                    Switch(checked = user.canEditPatients, onCheckedChange = onTogglePatients)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Éditer traitements", fontSize = 13.sp, color = Color(0xFF1E293B))
                    Switch(checked = user.canEditTreatments, onCheckedChange = onToggleTreatments)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Enregistrer encaissements", fontSize = 13.sp, color = Color(0xFF1E293B))
                    Switch(checked = user.canRecordPayments, onCheckedChange = onTogglePayments)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: (() -> Unit)? = null,
    actionLabel: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
        if (action != null && actionLabel != null) {
            Button(onClick = action, modifier = Modifier.testTag("section_primary_action")) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(24.dp),
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun StatCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text(subtitle, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun DashboardTreatmentsCard(
    title: String,
    treatments: List<DashboardTreatmentItem>,
    canEditTreatments: Boolean = true,
    onStartTreatment: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            Spacer(Modifier.height(12.dp))
            treatments.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dotColor = when (item.status) {
                        "Completed" -> Color(0xFF6DA56A)
                        "In Progress" -> Color(0xFFD4AF38)
                        else -> Color(0xFF3B82F6)
                    }
                    Box(Modifier.size(8.dp).background(dotColor, RoundedCornerShape(4.dp)))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                        Text("${item.patientName} • ${item.duration} • ${item.priceDa} DA", fontSize = 11.sp, color = Color.Gray)
                    }
                    if (item.status != "Completed" && canEditTreatments) {
                        TextButton(onClick = { onStartTreatment(item.id) }) {
                            Text(if (item.status == "Pending") "Démarrer" else "Terminer", fontSize = 12.sp)
                        }
                    } else if (item.status == "Completed") {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Terminé", tint = Color(0xFF6DA56A), modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun DashboardReceiptsCard(
    title: String,
    receipts: List<DashboardReceiptItem>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            Spacer(Modifier.height(12.dp))
            receipts.forEach { receipt ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFD4AF38), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(receipt.patientName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                        Text(receipt.label, fontSize = 11.sp, color = Color.Gray)
                    }
                    Text("${receipt.amountDa} DA", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF6DA56A))
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun DashboardRightPanel(
    canEditPatients: Boolean = true,
    canRecordPayments: Boolean = true,
    isAdmin: Boolean = true,
    onOpenAddPatient: () -> Unit = {},
    onOpenAddPayment: () -> Unit = {},
    onNavigateSection: (DashboardSection) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Novembre 2026", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Session caisse active", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF1E293B))
                    Text("Devis & encaissements en Dinar Algérien (DA)", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Actions rapides", modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(Modifier.height(12.dp))

            if (canEditPatients) {
                Button(
                    onClick = onOpenAddPatient,
                    modifier = Modifier.fillMaxWidth().testTag("new_patient_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Nouveau patient")
                }
            }

            if (canRecordPayments) {
                OutlinedButton(
                    onClick = onOpenAddPayment,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("new_payment_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Enregistrer encaissement")
                }
            }

            OutlinedButton(
                onClick = { onNavigateSection(DashboardSection.PATIENTS) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Voir les patients")
            }

            OutlinedButton(
                onClick = { onNavigateSection(DashboardSection.TREATMENT_PLANS) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Plans de traitement")
            }

            if (isAdmin) {
                OutlinedButton(
                    onClick = { onNavigateSection(DashboardSection.ADMINISTRATION) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("admin_panel_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Gérer utilisateurs & droits")
                }
            }
        }
    }
}

@Composable
private fun AddPatientDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau patient") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom complet") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("new_patient_name_input")
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_patient")
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun AddPaymentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long) -> Unit
) {
    var patientName by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    val parsedAmount = amountText.toLongOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvel encaissement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = patientName,
                    onValueChange = { patientName = it },
                    label = { Text("Patient") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Acte / Motif") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Montant (DA)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (patientName.isNotBlank() && label.isNotBlank() && parsedAmount != null && parsedAmount > 0L) {
                        onConfirm(patientName, label, parsedAmount)
                    }
                },
                enabled = patientName.isNotBlank() && label.isNotBlank() && (parsedAmount ?: 0L) > 0L
            ) {
                Text("Valider")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun CreateUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (username: String, password: String, isAdmin: Boolean) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isAdminRole by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvel utilisateur") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nom d'utilisateur (min 3)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe (min 6)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Rôle Administrateur")
                    Switch(checked = isAdminRole, onCheckedChange = { isAdminRole = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(username.trim(), password, isAdminRole) },
                enabled = username.trim().length >= 3 && password.length >= 6
            ) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
