// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * WCAG 2.2 normal-text 4.5:1 checks for every major SYSTIVRA Material3 role.
 * This protects against black-on-midnight backgrounds on controls and dialogs.
 * Intentional dark text on bright cyan/mint is tested separately.
 */
class ThemeContrastTest {
    private fun ratio(foreground: Color, background: Color): Float {
        val l1 = foreground.luminance()
        val l2 = background.luminance()
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    private fun assertReadable(label: String, ink: Color, surface: Color) {
        val contrast = ratio(ink, surface)
        assertTrue("$label contrast is $contrast, requires 4.5:1", contrast >= 4.5f)
    }

    @Test fun allDarkSurfaceTextMeetsNormalTextContrast() {
        val c = SystivraDarkColorScheme
        assertReadable("main foreground", c.onBackground, c.background)
        assertReadable("card content", c.onSurface, c.surface)
        assertReadable("secondary card captions", c.onSurfaceVariant, c.surfaceVariant)
        assertReadable("primary container", c.onPrimaryContainer, c.primaryContainer)
        assertReadable("secondary container", c.onSecondaryContainer, c.secondaryContainer)
        assertReadable("dialog surface low", c.onSurface, c.surfaceContainerLow)
        assertReadable("dialog surface high", c.onSurface, c.surfaceContainerHigh)
        assertReadable("dialog surface highest", c.onSurface, c.surfaceContainerHighest)
        assertReadable("error surface", c.onErrorContainer, c.errorContainer)
        assertReadable("inverse surface", c.inverseOnSurface, c.inverseSurface)
        assertReadable("SVG primary heading", TextPrimary, Background)
        assertReadable("SVG muted subtitles", Muted, Panel)
        assertReadable("SVG inactive bottom labels", Muted, Color(0xFF0D192B))
    }

    @Test fun buttonAndChipLabelsMeetNormalTextContrast() {
        val c = SystivraDarkColorScheme
        assertReadable("cyan filled buttons", c.onPrimary, c.primary)
        assertReadable("violet filled buttons", c.onSecondary, c.secondary)
        assertReadable("mint filled controls", c.onTertiary, c.tertiary)
        assertReadable("error destructive buttons", c.onError, c.error)
    }

    @Test fun noDarkInkIsAssignedToDarkContainerRoles() {
        val c = SystivraDarkColorScheme
        for ((name, ink) in listOf(
            "background" to c.onBackground,
            "surface" to c.onSurface,
            "surfaceVariant" to c.onSurfaceVariant,
            "primaryContainer" to c.onPrimaryContainer,
            "secondaryContainer" to c.onSecondaryContainer,
            "errorContainer" to c.onErrorContainer
        )) {
            assertTrue("$name must use light text rather than unreadable black", ink.luminance() > 0.35f)
        }
    }
}
