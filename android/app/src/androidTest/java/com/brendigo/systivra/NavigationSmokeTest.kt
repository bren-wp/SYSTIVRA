package com.brendigo.systivra

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
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
        rule.onNodeWithTag("dashboard-scroll").assertExists()
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
        rule.onNodeWithTag("settings-scroll")
            .performScrollToNode(hasText("About SYSTIVRA"))
        rule.onNodeWithText("About SYSTIVRA").performClick()
        rule.onNodeWithText("Version and licenses").assertExists()
        rule.onNodeWithText("Source code").assertExists()
        rule.onNodeWithText("MIT license").assertExists()
        rule.onNodeWithText("Privacy").performClick()
        rule.onNodeWithText("Privacy Center").assertExists()
    }

    @Test fun nestedBackStackReturnsToHardwareExplorer() {
        rule.waitForIdle()
        rule.onNodeWithTag("dashboard-scroll")
            .performScrollToNode(hasText("Hardware explorer", ignoreCase = true))
        rule.onNodeWithText("Hardware explorer", ignoreCase = true).performClick()
        rule.onNodeWithText("Explore components").assertExists()
        rule.onNodeWithText("CPU and SoC").performClick()
        rule.onNodeWithText("Platform processor capabilities").assertExists()
        rule.onNodeWithText("‹").performClick()
        rule.onNodeWithText("Explore components").assertExists()
    }
}
