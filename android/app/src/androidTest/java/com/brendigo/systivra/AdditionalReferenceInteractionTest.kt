package com.brendigo.systivra

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.brendigo.systivra.ui.AppTheme
import com.brendigo.systivra.ui.PermissionReferenceScreen
import com.brendigo.systivra.ui.RecordingReferenceScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AdditionalReferenceInteractionTest {
    @get:Rule val rule = createComposeRule()

    @Test fun userStartsActualPrivateRecordingFromReferenceRow() {
        var toggles = 0
        rule.setContent {
            AppTheme {
                RecordingReferenceScreen(
                    recorded = emptyList(), sessions = emptyList(),
                    recording = false, ready = true, storageError = null,
                    toggle = { toggles++ }, open = {}
                )
            }
        }
        rule.onNodeWithText("Session capture").assertExists()
        rule.onNodeWithText("Start").performClick()
        rule.runOnIdle { assertEquals(1, toggles) }
    }

    @Test fun userStopsActualRunningRecordingViaPauseAction() {
        var toggles = 0
        rule.setContent {
            AppTheme {
                RecordingReferenceScreen(
                    recorded = emptyList(), sessions = emptyList(),
                    recording = true, ready = true, storageError = null,
                    toggle = { toggles++ }, open = {}
                )
            }
        }
        rule.onNodeWithText("Pause").performClick()
        rule.runOnIdle { assertEquals(1, toggles) }
    }

    @Test fun permissionCardReportsAndroidManifestInsteadOfInventingPermission() {
        rule.setContent {
            AppTheme {
                PermissionReferenceScreen(open = {})
            }
        }
        rule.onNodeWithText("Access overview").assertExists()
        rule.onNodeWithText("Camera").performClick()
        rule.onNodeWithTag("permission-reference-data")
            .assert(hasAnyDescendant(hasText("Camera is not requested by this Android build.")))
    }
}
