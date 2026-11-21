package com.brendigo.systivra

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.ui.AppRoot
import org.junit.Rule
import org.junit.Test

/** Reference-first routes must still open genuine functional Android destinations. */
class ReferenceEntrancesTest {
    @get:Rule val rule = createComposeRule()

    private fun launch(route: String) {
        rule.setContent {
            AppRoot(
                snapshot = DeviceSnapshot.empty(),
                samples = emptyList(),
                recorded = emptyList(),
                isRecording = false,
                onToggleRecording = {},
                onClearRecording = {},
                initialRoute = route
            )
        }
    }

    @Test fun referenceOverviewHasItsOwnFocusedReadingsScreen() {
        launch("gpu-details")
        rule.onNodeWithTag("reference-hero").performClick()
        rule.onNodeWithTag("readings-browser-gpu-details").assertExists()
        rule.onNodeWithTag("metric-search-gpu-details").assertExists()
        rule.onNodeWithText("GPU Details readings").assertExists()
    }

    @Test fun referenceLandingHeroOpensTheRelatedActionInsteadOfUnrelatedDeviceDetails() {
        launch("network-details")
        rule.onNodeWithTag("reference-hero").performClick()
        rule.onNodeWithText("Network status").assertExists()
    }

    @Test fun reportsHeroIsAWorkingShortcutToTheCsvExportControls() {
        launch("reports-export")
        rule.onNodeWithTag("reference-hero").performClick()
        rule.onNodeWithTag("report-export-format-csv").assertIsDisplayed()
    }

    @Test fun reportSvgPdfActionFocusesItsActualPdfExportControl() {
        launch("reports-export")
        rule.onNodeWithTag("reference-landing-reports-export").assertExists()
        rule.onNodeWithText("Export your data").assertExists()
        // No legacy back row above the original SVG heading.
        rule.onNodeWithText("‹").assertDoesNotExist()
        rule.onNodeWithText("PDF").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("report-export-format-pdf").assertIsDisplayed()
        rule.onNodeWithText("Create PDF document").assertIsDisplayed()
    }

    @Test fun exportControlsShowOnlyOneFocusedFormatAndSwitchWithoutScrolling() {
        launch("reports-export")
        rule.onNodeWithText("PDF").performClick()
        rule.onNodeWithTag("report-format-selector").assertIsDisplayed()
        rule.onNodeWithTag("report-export-format-pdf").assertIsDisplayed()
        rule.onNodeWithTag("report-export-format-csv").assertDoesNotExist()
        rule.onNodeWithTag("report-export-format-share").assertDoesNotExist()

        rule.onNodeWithTag("report-format-tab-json").performClick()
        rule.onNodeWithTag("report-export-format-json").assertIsDisplayed()
        rule.onNodeWithTag("report-export-format-pdf").assertDoesNotExist()
        rule.onNodeWithText("Save JSON report").assertIsDisplayed()

        rule.onNodeWithTag("report-format-tab-share").performClick()
        rule.onNodeWithTag("report-export-format-share").assertIsDisplayed()
        rule.onNodeWithTag("report-export-format-json").assertDoesNotExist()
    }

    @Test fun emptyExportOffersDirectRouteToRecordingInsteadOfDeadButton() {
        launch("reports-export")
        rule.onNodeWithText("CSV").performClick()
        rule.onNodeWithText("No recorded samples").assertIsDisplayed()
        rule.onNodeWithTag("report-start-recording").performClick()
        rule.onNodeWithTag("reference-landing-recording-session").assertExists()
    }

    @Test fun reportSvgJsonActionFocusesItsActualJsonExportControl() {
        launch("reports-export")
        rule.onNodeWithText("JSON").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("report-export-format-json").assertIsDisplayed()
        rule.onNodeWithText("Save JSON report").assertIsDisplayed()
    }

    @Test fun reportSvgSharesheetActionFocusesSharingControls() {
        launch("reports-export")
        rule.onNodeWithText("Android Sharesheet").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("report-export-format-share").assertIsDisplayed()
        rule.onNodeWithText("Open Android Sharesheet").assertIsDisplayed()
    }

    @Test fun networkLandingShowsRealNetworkReadingsOnTap() {
        launch("network-details")
        rule.onNodeWithTag("reference-landing-network-details").assertExists()
        rule.onNodeWithText("Connection overview").assertExists()
        rule.onNodeWithText("Network state").performClick()
        rule.onNodeWithText("Network status").assertExists()
        rule.onNodeWithText("Reads connection state only. No internet request or speed test is performed.").assertExists()
    }

    @Test fun diagnosticLandingShowsOnlySavedOrUnavailableOutcomes() {
        launch("diagnostic-results")
        rule.onNodeWithTag("reference-landing-diagnostic-results").assertExists()
        rule.onNodeWithText("Passed").performClick()
        rule.onNodeWithText("Diagnostic Results · Passed").assertExists()
    }

    @Test fun overlaySizingAndPositionCanActuallyBeChangedInApp() {
        launch("overlay-configuration")
        rule.onNodeWithTag("reference-landing-overlay-configuration").assertExists()
        rule.onNodeWithText("Size").performClick()
        rule.onNodeWithTag("hud-preview-controls").assertExists()
        rule.onNodeWithTag("hud-preview-controls")
            .performScrollToNode(hasTestTag("hud-position-top-right"))
        rule.onNodeWithTag("hud-position-top-right").performClick()
        rule.onNodeWithText("Preview position: Top right").assertExists()
    }

    @Test fun alertsLandingPreservesWorkingBatteryThresholdControls() {
        launch("alerts")
        rule.onNodeWithTag("reference-landing-alerts").assertExists()
        rule.onNodeWithText("Battery level").performClick()
        rule.onNodeWithText("Live Alerts").assertExists()
        rule.onNodeWithText("Battery threshold:", substring = true).assertExists()
    }
}
