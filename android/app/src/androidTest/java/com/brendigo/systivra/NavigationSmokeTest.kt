// SYSTIVRA by Brendigo
package com.brendigo.systivra

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationSmokeTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun dashboardRendersWithoutAccountOrOnboarding() {
        rule.waitForIdle()
        rule.onNodeWithText("SYSTIVRA").assertExists()
        rule.onNodeWithTag("dashboard-page").assertExists()
    }

    @Test fun settingsReferenceRowsOpenRealControls() {
        rule.waitForIdle()
        rule.onNodeWithText("More").performClick()
        rule.onNodeWithText("Preferences").assertExists()
        rule.onNodeWithText("Update interval").performClick()
        rule.onNodeWithText("Close").assertExists()
        rule.onNodeWithText("Close").performClick()
        rule.onNodeWithText("Privacy").performClick()
        rule.onNodeWithText("Privacy Center").assertExists()
    }

    @Test fun aboutReferenceRowsProvideRealNavigation() {
        rule.waitForIdle()
        rule.onNodeWithText("More").performClick()
        rule.onNodeWithTag("reference-hero").performClick()
        rule.onNodeWithTag("settings-about-shortcut").performClick()
        rule.onNodeWithText("Version and licenses").assertExists()
        rule.onNodeWithText("Source code").assertExists()
        rule.onNodeWithText("MIT license").assertExists()
        rule.onNodeWithText("Privacy").performClick()
        rule.onNodeWithText("Privacy Center").assertExists()
    }

    @Test fun componentTestReferenceActionsOpenWorkingInteractiveControls() {
        rule.waitForIdle()
        rule.onNodeWithText("Tests").performClick()
        rule.onNodeWithText("Display").performClick()
        rule.onNodeWithText("Interactive testing").assertExists()
        rule.onNodeWithText("Run test").performClick()
        rule.onNodeWithText("TAP HERE").performClick()
        rule.onNodeWithText("Touches registered: 1").assertExists()
    }

    @Test fun deviceDetectionReferenceActionsOpenRealHardwareReadings() {
        rule.waitForIdle()
        rule.onNodeWithText("More").performClick()
        rule.onNodeWithTag("reference-hero").performClick()
        rule.onNodeWithTag("settings-all-tools").performClick()
        rule.onNodeWithTag("screen-catalog-page").assertExists()
        rule.onNodeWithTag("catalog-search").performTextInput("Device Detection")
        rule.onNodeWithText("Open Device Detection").performClick()
        rule.onNodeWithText("Hardware capabilities").assertExists()
        rule.onNodeWithText("Memory and storage").performClick()
        rule.onNodeWithText("Device RAM and internal storage").assertExists()
    }

    @Test fun focusedDashboardOpensSearchableCatalogInsteadOfLongToolList() {
        rule.waitForIdle()
        rule.onNodeWithTag("reference-hero").performClick()
        rule.onNodeWithTag("dashboard-all-tools").performClick()
        rule.onNodeWithTag("catalog-search").assertExists()
        rule.onNodeWithTag("catalog-search").performTextInput("charging")
        rule.onNodeWithText("Charging Analysis").assertExists()
        rule.onNodeWithText("GPU Details").assertDoesNotExist()
    }

    @Test fun catalogCategoryChipsFindFunctionsWithoutLongScrolling() {
        rule.waitForIdle()
        rule.onNodeWithTag("reference-hero").performClick()
        rule.onNodeWithTag("dashboard-all-tools").performClick()
        rule.onNodeWithTag("screen-catalog-page").assertExists()
        rule.onNodeWithTag("catalog-category-records").performClick()
        rule.onNodeWithTag("catalog-search").performTextInput("Reports")
        rule.onNodeWithText("Reports & Export").assertExists()
        rule.onNodeWithText("CPU Details").assertDoesNotExist()
        rule.onNodeWithTag("catalog-search").performTextClearance()
        rule.onNodeWithTag("catalog-category-hardware").performClick()
        rule.onNodeWithTag("catalog-search").performTextInput("CPU")
        rule.onNodeWithText("CPU Details").assertExists()
        rule.onNodeWithText("Reports & Export").assertDoesNotExist()
        rule.onNodeWithTag("catalog-search").performTextClearance()
        rule.onNodeWithTag("catalog-search").performTextInput("nonexistent-xyz")
        rule.onNodeWithTag("catalog-clear-filters").performClick()
        rule.onNodeWithText("No tools match. Change category or search.").assertDoesNotExist()
    }

    @Test fun nestedBackStackReturnsToHardwareExplorer() {
        rule.waitForIdle()
        rule.onNodeWithTag("reference-hero").performClick()
        rule.onNodeWithTag("dashboard-hardware-shortcut").performClick()
        rule.onNodeWithText("Explore components").assertExists()
        rule.onNodeWithText("CPU and SoC").performClick()
        rule.onNodeWithText("Processor overview").assertExists()
        rule.onNodeWithText("Architecture").assertExists()
        // Reference-detail screens use Android system Back; they intentionally
        // do not render the legacy inline back arrow in the design viewport.
        androidx.test.espresso.Espresso.pressBack()
        rule.onNodeWithText("Explore components").assertExists()
    }
}
