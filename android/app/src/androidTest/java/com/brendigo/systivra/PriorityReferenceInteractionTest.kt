package com.brendigo.systivra

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.Reading
import com.brendigo.systivra.ui.AppTheme
import com.brendigo.systivra.ui.BenchmarkController
import com.brendigo.systivra.ui.PriorityReferenceScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Real Android Compose rendering and row action checks, not conceptual SVG approval. */
class PriorityReferenceInteractionTest {
    @get:Rule val rule = createComposeRule()
    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val empty get() = DeviceSnapshot.empty()

    @Test fun benchmarkResultsUsesCorrectReferenceStructureWithNoFakeScore() {
        rule.setContent {
            AppTheme {
                PriorityReferenceScreen("benchmark-results", empty, emptyList(),
                    BenchmarkController(context), open = {})
            }
        }
        rule.onNodeWithText("Performance summary").assertExists()
        rule.onNodeWithText("Test version").assertExists()
        rule.onNodeWithText("Elapsed time").performClick()
        rule.onNodeWithTag("priority-selected-benchmark-results")
            .assert(hasAnyDescendant(hasText("Unavailable")))
    }

    @Test fun appUsageReferencePrivacyActionNavigatesToLocalPrivacy() {
        var destination: String? = null
        rule.setContent {
            AppTheme {
                PriorityReferenceScreen("app-usage", empty, emptyList(),
                    BenchmarkController(context), open = { destination = it })
            }
        }
        rule.onNodeWithText("Usage statistics").assertExists()
        rule.onNodeWithText("Permission required").assertExists()
        rule.onNodeWithText("Local statistics").assertExists()
        rule.onNodeWithText("Privacy").performClick()
        rule.runOnIdle { assertEquals("privacy-center", destination) }
    }

    @Test fun sessionSummaryCardOpensActualRecordedAverage() {
        val readings = listOf(42 to 1000L, 58 to 2000L).map { (level, time) ->
            empty.copy(time = time, battery = listOf(
                Reading("Charge level", "$level%", "Android battery broadcast")
            ))
        }
        rule.setContent {
            AppTheme {
                PriorityReferenceScreen("session-analysis", empty, readings,
                    BenchmarkController(context), open = {})
            }
        }
        rule.onNodeWithText("Session overview").assertExists()
        rule.onNodeWithText("Average").performClick()
        rule.onNodeWithTag("priority-selected-session-analysis")
            .assert(hasAnyDescendant(hasText("50.0%")))
    }

    @Test fun cpuCoreInspectorDoesNotFabricatePerCoreMetrics() {
        val device = empty.copy(cpu = listOf(
            Reading("Available processors", "8", "Runtime.availableProcessors")
        ))
        rule.setContent {
            AppTheme {
                PriorityReferenceScreen("cpu-cores", device, emptyList(),
                    BenchmarkController(context), open = {})
            }
        }
        rule.onNodeWithText("Core information").assertExists()
        rule.onNodeWithText("Core 3").performClick()
        rule.onNodeWithTag("priority-selected-cpu-cores")
            .assert(hasAnyDescendant(hasText("Unavailable")))
    }

    @Test fun benchmarkRunningHasActualCancelActionEvenWhenIdle() {
        rule.setContent {
            AppTheme {
                PriorityReferenceScreen("benchmark-running", empty, emptyList(),
                    BenchmarkController(context), open = {})
            }
        }
        rule.onNodeWithText("Test in progress").assertExists()
        rule.onNodeWithText("Stop test").performClick()
        rule.onNodeWithTag("priority-selected-benchmark-running").assertExists()
    }
}
