// SYSTIVRA by Brendigo
package com.brendigo.systivra

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.brendigo.systivra.data.RecordedSession
import com.brendigo.systivra.ui.AppTheme
import com.brendigo.systivra.ui.RecordingLibraryScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RecordingLibraryPagingTest {
    @get:Rule val rule = createComposeRule()

    @Test fun sessionActionsRemainReachableWithoutAListScroll() {
        var chosen: Long? = null
        var destination: String? = null
        val sessions = (1..7).map { i ->
            RecordedSession(i.toLong(), 1730000000000L + i * 1000L,
                1730000001000L + i * 1000L, i)
        }
        rule.setContent {
            AppTheme {
                RecordingLibraryScreen(
                    sessions, false, null,
                    onSelect = { chosen = it }, onDelete = {},
                    open = { destination = it }
                )
            }
        }
        rule.onNodeWithTag("recording-library-page").assertExists()
        rule.onNodeWithTag("recording-session-1").assertExists()
        rule.onNodeWithTag("recording-session-7").assertDoesNotExist()
        repeat(6) {
            if (rule.onAllNodesWithTag("recording-session-7")
                    .fetchSemanticsNodes().isEmpty()) {
                rule.onNodeWithTag("recording-library-next").performClick()
            }
        }
        rule.onNodeWithTag("recording-session-7").performClick()
        rule.onNodeWithText("Analyze").performClick()
        rule.runOnIdle {
            assertEquals(7L, chosen)
            assertEquals("session-analysis", destination)
        }
    }

    @Test fun noDeleteWhileForegroundRecordingActive() {
        var deletions = 0
        val session = RecordedSession(5L, 1000L, 2000L, 1)
        rule.setContent {
            AppTheme {
                RecordingLibraryScreen(
                    listOf(session), true, null, onSelect = {},
                    onDelete = { deletions++ }, open = {}
                )
            }
        }
        rule.onNodeWithTag("recording-session-5").performClick()
        rule.onNodeWithText("Delete session").assertExists()
        rule.onNodeWithText("Close").performClick()
        rule.runOnIdle { assertEquals(0, deletions) }
    }
}
