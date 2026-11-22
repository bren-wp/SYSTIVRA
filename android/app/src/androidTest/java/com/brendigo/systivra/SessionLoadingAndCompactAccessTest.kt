// SYSTIVRA by Brendigo
package com.brendigo.systivra

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
 import androidx.compose.ui.unit.dp
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.ui.AppRoot
import com.brendigo.systivra.ui.AppTheme
import com.brendigo.systivra.ui.Settings
import org.junit.Rule
import org.junit.Test

class SessionLoadingAndCompactAccessTest {
    @get:Rule val rule = createComposeRule()

    @Test fun pendingRecordingNeverOffersExportOfPreviousSamples() {
        rule.setContent {
            AppRoot(
                snapshot = DeviceSnapshot.empty(),
                samples = emptyList(),
                recorded = listOf(DeviceSnapshot.empty().copy(time = 100L)),
                isRecording = false, onToggleRecording = {}, onClearRecording = {},
                initialRoute = "reports-export-controls", selectedSessionId = 42L,
                selectedSessionSamples = listOf(DeviceSnapshot.empty().copy(time = 100L)),
                selectedSessionLoading = true
            )
        }
        rule.onNodeWithTag("selected-session-loading").assertExists()
        rule.onNodeWithTag("report-export-focused-screen").assertDoesNotExist()
    }

    @Test fun settingsPrivacyActionIsReachableInShortViewport() {
        rule.setContent {
            AppTheme {
                Box(Modifier.height(420.dp)) { Settings(open = {}) }
            }
        }
        // The Settings content is a vertically-scrollable Column. Scrolling
        // to its tagged child must not invoke LazyList-only semantics.
        rule.onNodeWithTag("reference-row-privacy").performScrollTo()
        rule.onNodeWithTag("reference-row-privacy").assertIsDisplayed()
    }
}
