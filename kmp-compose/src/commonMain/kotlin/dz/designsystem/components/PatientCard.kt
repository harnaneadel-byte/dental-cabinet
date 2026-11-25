package dz.designsystem

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VectorIcon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.icons.filled.MedicalServices

@Composable
fun PatientCard(
    patientName: String,
    scheduledAppointments: Int,
    onViewDetail: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardElevationMedium,
        colors = CardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp) {
                    Text(
                        text = patientName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row {
                        Icon(
                            imageVector = MedicalServices,
                            contentDescription = "Medical services",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "$scheduledAppointments appointments scheduled",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    ElevatedButton(
                        onClick = onViewDetail,
                        modifier = Modifier.size(80.dp, 36.dp),
                        text = { Text("View Detail") }
                    )
                }
            )
        )
    }
}