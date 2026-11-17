package com.brendigo.systivra.ui

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

/** The original SVG uses normalized x/y coordinates, not a pixel diagonal. */
class SvgGradientProjectionTest {
    private fun fraction(point: Offset, width: Float, height: Float): Float {
        val end = svgDiagonalGradientEnd(width, height)
        val lengthSquared = end.x * end.x + end.y * end.y
        return (point.x * end.x + point.y * end.y) / lengthSquared
    }

    @Test fun portraitReferenceUsesHalfGradientAtOppositeCorners() {
        val width = 390f
        val height = 844f
        assertEquals(0f, fraction(Offset.Zero, width, height), 0.0001f)
        assertEquals(0.5f, fraction(Offset(width, 0f), width, height), 0.0001f)
        assertEquals(0.5f, fraction(Offset(0f, height), width, height), 0.0001f)
        assertEquals(1f, fraction(Offset(width, height), width, height), 0.0001f)
    }

    @Test fun normalizedGradientWorksOnWidePhonesAndTablets() {
        for ((width, height) in listOf(360f to 800f, 800f to 1280f, 1280f to 800f)) {
            assertEquals(0.5f, fraction(Offset(width, 0f), width, height), 0.0001f)
            assertEquals(0.5f, fraction(Offset(0f, height), width, height), 0.0001f)
            assertEquals(1f, fraction(Offset(width, height), width, height), 0.0001f)
        }
    }

    @Test fun zeroBoundsDoNotCreateInfiniteShaderCoordinates() {
        assertEquals(Offset.Zero, svgDiagonalGradientEnd(0f, 844f))
        assertEquals(Offset.Zero, svgDiagonalGradientEnd(390f, 0f))
    }
}
