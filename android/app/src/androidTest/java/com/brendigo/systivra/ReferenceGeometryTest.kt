// SYSTIVRA by Brendigo
package com.brendigo.systivra

import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.ui.AppRoot
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Geometry-level emulator acceptance for the shared 390x844 SVG design primitives.
 * A green geometry check does not certify glyph rasterization, OS chrome or 1:1 pixels.
 */
class ReferenceGeometryTest {
    @get:Rule val rule = createComposeRule()

    @Test fun dashboardHeaderHeroAndRowSpacingFollowTheOriginalVectorGrid() {
        rule.setContent {
            AppRoot(
                snapshot = DeviceSnapshot.empty(),
                samples = emptyList(),
                recorded = emptyList(),
                isRecording = false,
                onToggleRecording = {},
                onClearRecording = {}
            )
        }
        val heading = rule.onNodeWithTag("reference-heading-title").getUnclippedBoundsInRoot()
        val hero = rule.onNodeWithTag("reference-hero").getUnclippedBoundsInRoot()
        val ring = rule.onNodeWithTag("reference-hero-ring", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val heroTitle = rule.onNodeWithTag("reference-hero-title", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val memory = rule.onNodeWithTag("reference-row-memory").getUnclippedBoundsInRoot()
        val storage = rule.onNodeWithTag("reference-row-storage").getUnclippedBoundsInRoot()
        val battery = rule.onNodeWithTag("reference-row-battery").getUnclippedBoundsInRoot()
        val thermal = rule.onNodeWithTag("reference-row-thermal-status").getUnclippedBoundsInRoot()

        // The reference places cards at x=20, text at x=24 and rings at x=34.
        assertEquals(24f, heading.left.value, 0.7f)
        assertEquals(20f, hero.left.value, 0.7f)
        assertEquals(14f, (ring.left - hero.left).value, 0.7f)
        assertEquals(20f, (ring.top - hero.top).value, 0.7f)
        assertEquals(88f, (ring.right - ring.left).value, 0.7f)
        assertEquals(88f, (ring.bottom - ring.top).value, 0.7f)
        assertEquals(123f, (heroTitle.left - hero.left).value, 0.7f)
        assertEquals(128f, (hero.bottom - hero.top).value, 0.7f)

        listOf(memory, storage, battery, thermal).forEach { row ->
            assertEquals(20f, row.left.value, 0.7f)
            assertEquals(69f, (row.bottom - row.top).value, 0.7f)
            assertEquals((hero.right - hero.left).value, (row.right - row.left).value, 0.7f)
        }
        // SVG action rows: 69dp height + 13dp from top-to-top in the gap.
        assertEquals(82f, (storage.top - memory.top).value, 0.7f)
        assertEquals(82f, (battery.top - storage.top).value, 0.7f)
        assertEquals(82f, (thermal.top - battery.top).value, 0.7f)
        // SVG hero ends at y=336, first row starts at 359.
        assertEquals(23f, (memory.top - hero.bottom).value, 1.2f)
    }
}
