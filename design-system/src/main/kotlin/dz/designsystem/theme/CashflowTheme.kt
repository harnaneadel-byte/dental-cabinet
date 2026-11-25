package dz.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFD4AF38),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8C572),
    onPrimaryContainer = Color(0xFF8B6914),
    secondary = Color(0xFF6DA56A),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFBCD6BC),
    surface = Color(0xFFFDFDFD),
    onSurface = Color(0xFF212121),
    background = Color(0xFFF5F0E8),
    onBackground = Color(0xFF212121),
    error = Color(0xFFB32630)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD4AF38),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF8B6914),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF6DA56A),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF5D8A5A),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFE8E2DC),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE8E2DC),
    error = Color(0xFFB32630)
)

@Composable
fun DentalDashboardTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
