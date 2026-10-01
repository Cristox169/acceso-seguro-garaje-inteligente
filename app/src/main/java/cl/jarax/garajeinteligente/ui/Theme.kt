package cl.jarax.garajeinteligente.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val Ink = Color(0xFF0A1116)
internal val Panel = Color(0xFF121E24)
internal val PanelRaised = Color(0xFF18272D)
internal val Mint = Color(0xFF69E2B2)
internal val MintSoft = Color(0xFFB8F3DB)
internal val TextMain = Color(0xFFF3F7F5)
internal val TextMuted = Color(0xFF9AADB1)
internal val Red = Color(0xFFFF6969)
internal val Amber = Color(0xFFFFD071)

private val GarageColors = darkColorScheme(
    primary = Mint,
    onPrimary = Ink,
    secondary = MintSoft,
    background = Ink,
    surface = Panel,
    surfaceVariant = PanelRaised,
    onBackground = TextMain,
    onSurface = TextMain,
    onSurfaceVariant = TextMuted,
    error = Red,
)

@Composable
fun AccesoSeguroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GarageColors,
        content = content,
    )
}
