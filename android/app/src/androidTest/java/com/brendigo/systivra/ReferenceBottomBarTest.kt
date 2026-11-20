package com.brendigo.systivra

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.ui.AppRoot
import com.brendigo.systivra.ui.ReferenceBottomBar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** A text label must never visually land in a neighbouring tab's click target. */
class ReferenceBottomBarTest {
    @get:Rule val rule = createComposeRule()

    @Test fun labelsRemainWithinTheirOwnClickableRegionsOnNarrowPhones() {
        rule.setContent {
            Box(Modifier.width(320.dp).height(720.dp)) {
                AppRoot(
                    snapshot = DeviceSnapshot.empty(),
                    samples = emptyList(),
                    recorded = emptyList(),
                    isRecording = false,
                    onToggleRecording = {},
                    onClearRecording = {}
                )
            }
        }

        val ids = listOf("dashboard", "live-monitor", "diagnostics-center", "settings")
        var previousRight = 0f
        ids.forEachIndexed { index, id ->
            val hit = rule.onNodeWithTag("reference-bottom-tab-$id").getUnclippedBoundsInRoot()
            val label = rule.onNodeWithTag(
                "reference-bottom-label-$id", useUnmergedTree = true
            ).getUnclippedBoundsInRoot()
            assertTrue("$id label left edge escapes its tab", label.left.value >= hit.left.value - 0.5f)
            assertTrue("$id label right edge escapes its tab", label.right.value <= hit.right.value + 0.5f)
            if (index > 0) {
                assertEquals("$id tab must touch its neighbour", previousRight, hit.left.value, 0.7f)
            }
            previousRight = hit.right.value
        }
        assertEquals(320f, previousRight, 1f)

        // The same region tested geometrically must remain fully interactive.
        rule.onNodeWithTag("reference-bottom-tab-live-monitor").performClick()
        rule.onNodeWithTag("live-monitor-scroll").assertExists()
    }
    @Test fun labelCentersFollow390DpSvgNavigationAnchors() {
        rule.setContent {
            Box(Modifier.width(390.dp).height(720.dp)) {
                AppRoot(
                    snapshot = DeviceSnapshot.empty(),
                    samples = emptyList(),
                    recorded = emptyList(),
                    isRecording = false,
                    onToggleRecording = {},
                    onClearRecording = {}
                )
            }
        }

        // SVG x=45/149/246/326 are the glyph starts at the original viewport.
        // Text boxes are centered at these optical anchors, not translated
        // over a neighbouring hit target using Modifier.offset.
        val anchors = listOf(
            "dashboard" to 75f,
            "live-monitor" to 170f,
            "diagnostics-center" to 260f,
            "settings" to 341f
        )
        anchors.forEach { (id, centerX) ->
            val label = rule.onNodeWithTag(
                "reference-bottom-label-$id", useUnmergedTree = true
            ).getUnclippedBoundsInRoot()
            assertEquals("$id SVG center anchor", centerX,
                (label.left.value + label.right.value) / 2f, 1f)
        }
    }


    @Test fun tabsExposeActualSelectedStateToAndroidAccessibility() {
        rule.setContent {
            Box(Modifier.fillMaxSize()) {
                AppRoot(
                    snapshot = DeviceSnapshot.empty(),
                    samples = emptyList(),
                    recorded = emptyList(),
                    isRecording = false,
                    onToggleRecording = {},
                    onClearRecording = {}
                )
            }
        }
        rule.onNodeWithTag("reference-bottom-tab-dashboard").assertIsSelected()
        rule.onNodeWithTag("reference-bottom-tab-settings").assertIsNotSelected()
        rule.onNodeWithTag("reference-bottom-tab-settings").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("reference-bottom-tab-settings").assertIsSelected()
        rule.onNodeWithTag("reference-bottom-tab-dashboard").assertIsNotSelected()
    }

    @Test fun fullAppBottomBarIsAnchoredAndAlwaysLeavesItsSystemSafetyGutter() {
        var navigationInsetDp = 0f
        rule.setContent {
            val density = LocalDensity.current
            navigationInsetDp = with(density) {
                WindowInsets.navigationBars.getBottom(density).toDp().value
            }
            Box(Modifier.fillMaxSize()) {
                AppRoot(
                    snapshot = DeviceSnapshot.empty(),
                    samples = emptyList(),
                    recorded = emptyList(),
                    isRecording = false,
                    onToggleRecording = {},
                    onClearRecording = {}
                )
            }
        }
        val root = rule.onRoot().getUnclippedBoundsInRoot()
        val host = rule.onNodeWithTag("reference-bottom-safe-area").getUnclippedBoundsInRoot()
        val tab = rule.onNodeWithTag("reference-bottom-tab-dashboard").getUnclippedBoundsInRoot()
        assertEquals("Bottom bar must be anchored at the viewport bottom",
            root.bottom.value, host.bottom.value, 1f)
        assertTrue("No app tab may enter the Android system control zone",
            (root.bottom - tab.bottom).value >= navigationInsetDp + 8f - 1f)
        assertEquals("Tab target retains SVG's 62dp geometry",
            62f, (tab.bottom - tab.top).value, 1f)
    }

    @Test fun navigationTabsReserveTheActualAndroidSystemBottomInset() {
        var navigationInsetDp = 0f
        val entries = listOf(
            Triple("dashboard", "Dashboard", "▣"),
            Triple("live-monitor", "Monitor", "⌁"),
            Triple("diagnostics-center", "Tests", "◇"),
            Triple("settings", "More", "☷")
        )
        rule.setContent {
            val density = LocalDensity.current
            navigationInsetDp = with(density) {
                WindowInsets.navigationBars.getBottom(density).toDp().value
            }
            Box(Modifier.fillMaxSize()) {
                ReferenceBottomBar(
                    entries = entries,
                    selectedTab = "dashboard",
                    onNavigate = {}
                )
            }
        }
        val wholeBar = rule.onNodeWithTag("reference-bottom-safe-area")
            .getUnclippedBoundsInRoot()
        val tab = rule.onNodeWithTag("reference-bottom-tab-dashboard")
            .getUnclippedBoundsInRoot()
        assertEquals("Reference tap height must not shrink", 62f,
            (tab.bottom - tab.top).value, 1f)
        assertTrue("Tab must not extend below its safe-area host",
            wholeBar.bottom.value >= tab.bottom.value)
        assertTrue("System navigation and an 8dp visual gutter must be below the tab targets",
            (wholeBar.bottom - tab.bottom).value >= navigationInsetDp + 8f - 1f)
    }
}
