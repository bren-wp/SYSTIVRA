package com.brendigo.systivra

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.ui.RecordingSessionScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RecordingDeletionConfirmationTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun deletionRequiresExplicitConfirmationAndCanBeCancelled() {
        var deletions = 0
        rule.setContent {
            RecordingSessionScreen(
                readings = listOf(DeviceSnapshot.empty()),
                active = false,
                ready = true,
                storageError = null,
                toggle = {},
                clear = { deletions++ },
                open = {}
            )
        }

        rule.onNodeWithText("Delete all saved recordings").performClick()
        rule.onNodeWithText("Delete all recordings?").assertExists()
        rule.runOnIdle { assertEquals(0, deletions) }

        rule.onNodeWithText("Cancel").performClick()
        rule.runOnIdle { assertEquals(0, deletions) }

        rule.onNodeWithText("Delete all saved recordings").performClick()
        rule.onNodeWithText("Delete all").performClick()
        rule.runOnIdle { assertEquals(1, deletions) }
    }
}
