package com.brendigo.systivra.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

val Background = Color(0xFF0B121E)
val Panel = Color(0xFF152235)
val Raised = Color(0xFF1A2D47)
val Cyan = Color(0xFF29C7FC)
val Purple = Color(0xFF8B5CF6)
val Mint = Color(0xFF32E5BC)
val Muted = Color(0xFF9CAFBE)

private val palette = darkColorScheme(
    primary = Cyan, onPrimary = Background,
    secondary = Purple, tertiary = Mint,
    background = Background, onBackground = Color(0xFFF4F8FC),
    surface = Panel, onSurface = Color(0xFFF4F8FC),
    surfaceVariant = Raised
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = palette) {
        // Transparent Scaffold/Canvas containers inherit LocalContentColor.
        // Without an explicit dark foreground, default Text can render black.
        CompositionLocalProvider(LocalContentColor provides palette.onBackground) {
            content()
        }
    }
}
