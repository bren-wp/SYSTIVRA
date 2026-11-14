package com.systivra.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationSmokeTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun dashboardRendersWithoutLogin() {
        rule.onNodeWithText("SYSTIVRA").assertExists()
        rule.onNodeWithText("Device Overview").assertExists()
        rule.onNodeWithText("Hardware explorer", ignoreCase = true).assertExists()
    }

    @Test fun canNavigateToCpuDetailsAndBack() {
        rule.onNodeWithText("Hardware explorer", ignoreCase = true).performClick()
        rule.onNodeWithText("Hardware Explorer").assertExists()
        rule.onNodeWithText("CPU").performClick()
        rule.onNodeWithText("Platform processor capabilities").assertExists()
        rule.onNodeWithText("‹").performClick()
        rule.onNodeWithText("Hardware Explorer").assertExists()
    }
}
