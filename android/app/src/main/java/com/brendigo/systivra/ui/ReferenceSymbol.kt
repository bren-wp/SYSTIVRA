package com.brendigo.systivra.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Fixed 18 × 18dp vector glyphs for the four icons in the original SVG.
 *
 * Rendering Unicode symbols in the Android fallback font is device dependent:
 * some builds replace these with tofu squares or multicolor emoji. Drawing the
 * same geometry in Compose makes the icons deterministic at different font
 * scales and densities while keeping the original cobalt/violet backgrounds.
 *
 * Icons are decorative because the parent card's text is the accessible label.
 */
@Composable
internal fun ReferenceSymbol(symbol: String, color: Color, testTag: String) {
    if (symbol !in setOf("◇", "▤", "◉", "⌁")) {
        Text(symbol, color = color, fontSize = 18.sp)
        return
    }
    Canvas(
        // Canvas has no text or content description: it remains decorative
        // for TalkBack, while its stable tag stays visible to the unmerged
        // Compose test tree for pixel-exact bounds verification.
        Modifier.size(18.dp)
            .testTag(testTag)
    ) {
        val step = size.minDimension / 18f
        fun xy(x: Float, y: Float) = Offset(x * step, y * step)
        val stroke = 1.6.dp.toPx()
        when (symbol) {
            "◇" -> {
                val diamond = Path().apply {
                    moveTo(9f * step, 2f * step)
                    lineTo(16f * step, 9f * step)
                    lineTo(9f * step, 16f * step)
                    lineTo(2f * step, 9f * step)
                    close()
                }
                drawPath(diamond, color, style = Stroke(width = stroke, cap = StrokeCap.Round))
            }
            "▤" -> {
                drawRoundRect(
                    color = color,
                    topLeft = xy(2f, 2f),
                    size = Size(14f * step, 14f * step),
                    cornerRadius = CornerRadius(1.3f * step),
                    style = Stroke(width = stroke)
                )
                listOf(6f, 9f, 12f).forEach { y ->
                    drawLine(color, xy(5f, y), xy(13f, y), strokeWidth = stroke, cap = StrokeCap.Round)
                }
            }
            "◉" -> {
                drawCircle(color = color, radius = 6f * step, center = xy(9f, 9f),
                    style = Stroke(width = stroke))
                drawCircle(color = color, radius = 2.3f * step, center = xy(9f, 9f))
            }
            "⌁" -> {
                val path = Path().apply {
                    moveTo(2f * step, 6f * step)
                    cubicTo(5f * step, 2f * step, 7f * step, 13f * step, 10f * step, 9f * step)
                    cubicTo(13f * step, 5f * step, 14f * step, 12f * step, 16f * step, 12f * step)
                }
                drawPath(path, color, style = Stroke(width = stroke, cap = StrokeCap.Round))
            }
        }
    }
}
