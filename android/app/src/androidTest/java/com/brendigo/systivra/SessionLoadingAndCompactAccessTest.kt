// SYSTIVRA by Brendigo
package com.brendigo.systivra

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertHasClickAction
import android.util.Log
import org.junit.Assert.assertEquals
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
        var openedPrivacy = 0
        rule.setContent {
            AppTheme {
                Box(Modifier.height(420.dp)) {
                    Settings(open = { if (it == "privacy-center") openedPrivacy++ })
                }
            }
        }
        // Deliberately use a finite number of actual swipes on the same
        // Column that owns the verticalScroll modifier. Its semantic
        // performScrollTo operation deadlocked the test on API 33.
        Log.i("SYSTIVRA_SCROLL_QA", "settings: mounted; swipe start")
        repeat(2) {
            rule.onNodeWithTag("settings-page").performTouchInput { swipeUp() }
        }
        Log.i("SYSTIVRA_SCROLL_QA", "settings: swipes finished; checking privacy")
        rule.onNodeWithTag("reference-row-privacy")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
        rule.runOnIdle { assertEquals(1, openedPrivacy) }
        Log.i("SYSTIVRA_SCROLL_QA", "settings: privacy action invoked")
    }
}
