// SYSTIVRA by Brendigo
package com.brendigo.systivra

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.brendigo.systivra.data.RecordedSession
import com.brendigo.systivra.ui.AppTheme
import com.brendigo.systivra.ui.ReferenceHistory
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HistorySessionSelectionTest {
    @get:Rule val rule = createComposeRule()

    @Test fun tappingSavedSessionSelectsItsIdBeforeOpeningLibrary() {
        var chosen: Long? = null
        var destination: String? = null
        rule.setContent {
            AppTheme {
                ReferenceHistory(
                    samples = emptyList(),
                    savedSessions = listOf(
                        RecordedSession(
                            id = 42L,
                            startedAt = 1_730_000_000_000L,
                            lastSampleAt = 1_730_000_003_000L,
                            sampleCount = 7
                        )
                    ),
                    onSelectSession = { chosen = it },
                    open = { destination = it }
                )
            }
        }
        rule.onNodeWithTag("history-page").assertExists()
        rule.onNodeWithText("All sessions").performClick()
        rule.onNodeWithTag("history-sessions-page").assertExists()
        rule.onNodeWithText("7 recorded samples").performClick()
        rule.runOnIdle {
            assertEquals(42L, chosen)
            assertEquals("recording-library", destination)
        }
    }
}
