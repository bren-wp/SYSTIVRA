package com.brendigo.systivra.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.LinearGradientShader

val Background = Color(0xFF0B121E)
val Panel = Color(0xFF152235)
val Raised = Color(0xFF1A2D47)
val Cyan = Color(0xFF29C7FC)
val Purple = Color(0xFF8B5CF6)
val Mint = Color(0xFF32E5BC)
val Muted = Color(0xFF9CAFBE)

/**
 * SVG objectBoundingBox coordinates normalize X and Y independently:
 * t = (x / width + y / height) / 2 for a gradient (0,0) -> (1,1).
 *
 * Compose's default diagonal gradient uses physical pixel distances instead.
 * On portrait screens that renders the bottom-left about 80% toward the dark
 * stop rather than the reference's 50%, changing most background pixels.
 * This projection produces SVG-matching interpolation for any viewport size.
 */
internal fun svgDiagonalGradientEnd(width: Float, height: Float): Offset {
    if (width <= 0f || height <= 0f) return Offset.Zero
    val w = width.toDouble()
    val h = height.toDouble()
    val denominator = w * w + h * h
    return Offset(
        (2.0 * w * h * h / denominator).toFloat(),
        (2.0 * h * w * w / denominator).toFloat()
    )
}

internal val ReferenceBackgroundBrush: Brush = object : ShaderBrush() {
    override fun createShader(size: Size): Shader = LinearGradientShader(
        from = Offset.Zero,
        to = svgDiagonalGradientEnd(size.width, size.height),
        colors = listOf(Color(0xFF0D1F3F), Color(0xFF08111D))
    )
}


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
