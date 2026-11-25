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
import androidx.compose.material3.icons.filled.Description
import androidx.compose.material3.icons.filled.CalendarMonth

@Composable
fun TreatmentCard(
    treatmentName: String,
    status: String,
    duration: String,
    onStartTreatment: () -> Unit,
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
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = treatmentName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodySmall,
                            color = when (status) {
                                "Completed" -> MaterialTheme.colorScheme.success
                                "Pending" -> MaterialTheme.colorScheme.warning
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row {
                        Icon(
                            imageVector = CalendarMonth,
                            contentDescription = "Duration",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(text = duration, style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(
                        onClick = onStartTreatment,
                        modifier = Modifier.size(80.dp, 32.dp),
                        style = OutlinedButtonStyle.Normal,
                        text = { Text("Start") }
                    )
                }
            )
        )
    }
}