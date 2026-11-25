package dz.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dz.designsystem.theme.DentalDashboardTheme

@Composable
fun DashboardScreen() {
    DentalDashboardTheme {
        Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // 1. Sidebar (Dark)
            DashboardSidebar(modifier = Modifier.width(280.dp).fillMaxHeight())

            // 2. Main Content Area
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                DashboardHeader(modifier = Modifier.fillMaxWidth().height(64.dp))
                DashboardMainContent(modifier = Modifier.weight(1f).fillMaxWidth())
            }

            // 3. Right Sidebar (Light)
            DashboardRightPanel(modifier = Modifier.width(300.dp).fillMaxHeight())
        }
    }
}

@Composable
fun DashboardSidebar(modifier: Modifier = Modifier) {
    val sidebarBg = Color(0xFF1E293B) // Dark blue/slate from the image
    val activeAccent = Color(0xFF3B82F6) // Bright blue accent

    Column(
        modifier = modifier.background(sidebarBg).padding(16.dp)
    ) {
        // Logo & Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Cabinet Dentaire", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Dr. Bouzid Salah Eddine", color = Color.LightGray, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(32.dp))

        // Navigation Items
        SidebarItem("Tableau de bord", Icons.Default.Dashboard, isActive = true, activeColor = activeAccent)
        SidebarItem("Patients", Icons.Default.People)
        SidebarItem("Agenda", Icons.Default.CalendarToday)

        Text("Clinique", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp))
        SidebarItem("Schéma dentaire", Icons.Default.Brush) // Placeholder icon
        SidebarItem("Plans de traitement", Icons.Default.Description)

        Text("Paiements", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp))
        SidebarItem("Encaissements", Icons.Default.AccountBalanceWallet)

        Spacer(Modifier.weight(1f))

        // Footer: Offline mode
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color.Green, modifier = Modifier.size(16.dp))
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
fun SidebarItem(label: String, icon: ImageVector, isActive: Boolean = false, activeColor: Color = Color.Transparent) {
    Surface(
        color = if (isActive) activeColor else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun DashboardHeader(modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White, shadowElevation = 1.dp) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Bar
            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(400.dp).height(36.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Text("Rechercher un patient...", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp))
                    Spacer(Modifier.weight(1f))
                    Text("Ctrl + K", color = Color.LightGray, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.weight(1f))

            // User Info & Status
            AssistChip(
                onClick = {},
                label = { Text("Hors ligne") },
                leadingIcon = { Icon(Icons.Default.WifiOff, null, modifier = Modifier.size(16.dp)) },
                colors = AssistChipDefaults.assistChipColors(labelColor = Color.Red)
            )

            IconButton(onClick = {}) {
                BadgedBox(badge = { Badge() }) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                }
            }

            Spacer(Modifier.width(8.dp))
            Text("Dr. Bouzid Salah Eddine", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
fun DashboardMainContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        // Hero Banner
        Surface(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        ) {
             Row(Modifier.padding(24.dp)) {
                 Column(Modifier.weight(1f)) {
                     Text("Bonjour Dr. Bouzid,", style = MaterialTheme.typography.headlineMedium, color = Color.DarkGray)
                     Text("Voici la situation de votre cabinet aujourd'hui.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                 }
                 // Image placeholder
                 Box(Modifier.size(120.dp).background(Color.LightGray, RoundedCornerShape(8.dp)))
             }
        }

        Spacer(Modifier.height(16.dp))

        // Stats Row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("Patients actifs", "1 248", "+12 ce mois", Modifier.weight(1f))
            StatCard("Rendez-vous", "8", "/ 12 planifiés", Modifier.weight(1f))
            StatCard("Encaissements", "48 000 DZD", "aujourd'hui", Modifier.weight(1f))
            StatCard("Plans en cours", "97", "14 sans prix", Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        // Tables Row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardTableCard("Rendez-vous du jour", Modifier.weight(1f))
            DashboardTableCard("Encaissements récents", Modifier.weight(1f))
        }
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
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun DashboardTableCard(title: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            // Placeholder for list/table
            repeat(4) {
                Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(Color.Blue, RoundedCornerShape(4.dp)))
                    Spacer(Modifier.width(12.dp))
                    Text("Item details here", fontSize = 13.sp)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.CheckCircle, null, tint = Color.Green, modifier = Modifier.size(16.dp))
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun DashboardRightPanel(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(Color.White).padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Octobre 2026", fontWeight = FontWeight.Bold)
        // Calendar Placeholder
        Box(Modifier.fillMaxWidth().height(200.dp).background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)))

        Spacer(Modifier.height(24.dp))

        Text("Actions rapides", modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        Button(onClick = {}, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Nouveau patient")
        }

        repeat(3) {
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(8.dp)) {
                Text("Action option")
            }
        }
    }
}
