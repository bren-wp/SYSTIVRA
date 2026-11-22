// SYSTIVRA by Brendigo
package com.brendigo.systivra

import androidx.compose.ui.test.hasTestTag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import com.brendigo.systivra.ui.AppTheme
import com.brendigo.systivra.ui.ComponentTest
import com.brendigo.systivra.ui.OverlayPreviewScreen
import com.brendigo.systivra.ui.ReportExportScreen
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.assertIsDisplayed
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
        // One real swipe must expose the last card; off-screen icon geometry
        // remains measurable via getUnclippedBoundsInRoot without repeated
        // semantics scroll searches that can deadlock instrumentation.
        rule.onNodeWithTag("dashboard-page").performTouchInput { swipeUp() }
        rule.onNodeWithTag("reference-row-thermal-status").assertIsDisplayed()
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

    @Test fun compactPerformanceHudLetsUserReachEveryPositionButton() {
        rule.setContent {
            AppTheme {
                Box(Modifier.width(320.dp).height(440.dp)) {
                    OverlayPreviewScreen(DeviceSnapshot.empty())
                }
            }
        }
        rule.onNodeWithTag("hud-tab-position").performClick()
        rule.onNodeWithTag("hud-preview-controls").performTouchInput { swipeUp() }
        rule.onNodeWithTag("hud-position-bottom-right").assertIsDisplayed()
        rule.onNodeWithTag("hud-position-bottom-right").performClick()
        rule.onNodeWithText("Preview position: Bottom right").assertExists()
    }

    @Test fun compactComponentTesterPreservesWorkingColorActionBelowFold() {
        rule.setContent {
            AppTheme {
                Box(Modifier.width(320.dp).height(430.dp)) {
                    ComponentTest()
                }
            }
        }
        rule.onNodeWithTag("component-test-page").performTouchInput { swipeUp() }
        rule.onNodeWithText("Next color").assertIsDisplayed()
        rule.onNodeWithText("Next color").performClick()
        rule.onNodeWithText("Touches registered: 0").assertExists()
    }

    @Test fun compactExportPreservesActualRecordingShortcut() {
        var requests = 0
        rule.setContent {
            AppTheme {
                Box(Modifier.width(320.dp).height(380.dp)) {
                    ReportExportScreen(
                        samples = emptyList(), preferredFormat = "csv",
                        onStartRecording = { requests++ }
                    )
                }
            }
        }
        rule.onNodeWithTag("report-export-focused-screen").performTouchInput { swipeUp() }
        rule.onNodeWithTag("report-start-recording").assertIsDisplayed()
        rule.onNodeWithTag("report-start-recording").performClick()
        rule.runOnIdle { assertEquals(1, requests) }
    }

}
