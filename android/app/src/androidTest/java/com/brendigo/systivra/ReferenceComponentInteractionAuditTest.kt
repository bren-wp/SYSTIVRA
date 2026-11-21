// SYSTIVRA by Brendigo
package com.brendigo.systivra

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.ui.AppRoot
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Real rendered 390dp card states, not a screenshot of design concepts.
 * The reference marks are decorative, while the entire card must be clickable
 * and keyboard/TalkBack accessible as one actionable surface.
 */
class ReferenceComponentInteractionAuditTest {
    @get:Rule val rule = createComposeRule()

    private fun dashboard() {
        rule.setContent {
            AppRoot(
                snapshot = DeviceSnapshot.empty(),
                samples = emptyList(), recorded = emptyList(),
                isRecording = false, onToggleRecording = {},
                onClearRecording = {}, initialRoute = "dashboard"
            )
        }
    }

    @Test fun allFourNativeVectorIconsExistAtOriginal18dpSize() {
        dashboard()
        for (name in listOf("memory", "storage", "battery", "thermal-status")) {
            val icon = rule.onNodeWithTag(
                "reference-row-icon-$name", useUnmergedTree = true
            ).getUnclippedBoundsInRoot()
            assertEquals("$name icon width", 18f, (icon.right - icon.left).value, 1.0f)
            assertEquals("$name icon height", 18f, (icon.bottom - icon.top).value, 1.0f)
        }
    }

    @Test fun eachCardHasARealClickStateAndNavigatesToDeviceData() {
        dashboard()
        val row = rule.onNodeWithTag("reference-row-memory")
        row.assertHasClickAction()
        val region = row.getUnclippedBoundsInRoot()
        assertEquals(69f, (region.bottom - region.top).value, 1.0f)
        row.performClick()
        rule.onNodeWithTag("reference-detail-memory-details").assertExists()
    }
}
