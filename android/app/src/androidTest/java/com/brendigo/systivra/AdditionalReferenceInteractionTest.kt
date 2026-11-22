// SYSTIVRA by Brendigo
package com.brendigo.systivra

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    @Test fun recordingLastActionIsReachableOnSmallPhoneWithoutClipping() {
        rule.setContent {
            AppTheme {
                Box(Modifier.width(320.dp).height(480.dp)) {
                    RecordingReferenceScreen(
                        recorded = emptyList(), sessions = emptyList(),
                        recording = false, ready = true, storageError = null,
                        toggle = {}, open = {}
                    )
                }
            }
        }
        rule.onNodeWithTag("recording-reference-scroll")
            .performScrollToNode(hasTestTag("reference-row-storage-policy"))
        rule.onNodeWithTag("reference-row-storage-policy").performClick()
        rule.onNodeWithTag("recording-reference-data")
            .assert(hasAnyDescendant(hasText("Maximum 900 samples", substring = true)))
    }

    @Test fun permissionLastActionRemainsReachableOnShortPhone() {
        rule.setContent {
            AppTheme {
                Box(Modifier.width(320.dp).height(480.dp)) {
                    PermissionReferenceScreen(open = {})
                }
            }
        }
        rule.onNodeWithTag("permission-reference-scroll")
            .performScrollToNode(hasTestTag("reference-row-microphone"))
        rule.onNodeWithTag("reference-row-microphone").performClick()
        rule.onNodeWithTag("permission-reference-data")
            .assert(hasAnyDescendant(hasText("Microphone is not requested by this Android build.")))
    }

    @Test fun recordingReferenceDoesNotMoveWhenItFitsTheOriginalViewport() {
        rule.setContent {
            AppTheme {
                Box(Modifier.width(390.dp).height(844.dp)) {
                    RecordingReferenceScreen(
                        recorded = emptyList(), sessions = emptyList(),
                        recording = false, ready = true, storageError = null,
                        toggle = {}, open = {}
                    )
                }
            }
        }
        val before = rule.onNodeWithTag("reference-row-storage-policy").getUnclippedBoundsInRoot()
        rule.onNodeWithTag("recording-reference-scroll").performTouchInput { swipeUp() }
        rule.waitForIdle()
        val after = rule.onNodeWithTag("reference-row-storage-policy").getUnclippedBoundsInRoot()
        assertEquals("Do not scroll source-layout pages when every card fits",
            before.top.value, after.top.value, 1f)
        assertEquals(before.bottom.value, after.bottom.value, 1f)
    }

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

    @Test fun recordingDetailsOpenImmediatelyWithoutForcedScroll() {
        rule.setContent {
            AppTheme {
                RecordingReferenceScreen(
                    recorded = emptyList(), sessions = emptyList(),
                    recording = false, ready = true, storageError = null,
                    toggle = {}, open = {}
                )
            }
        }
        rule.onNodeWithText("Duration").performClick()
        rule.onNodeWithTag("recording-reference-data")
            .assert(hasAnyDescendant(hasText("No saved session available")))
        rule.onNodeWithText("Close").performClick()
        rule.onNodeWithTag("recording-reference-data").assertDoesNotExist()
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
