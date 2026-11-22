// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import com.brendigo.systivra.ui.LocalizedText as Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Original SVG navigation anchors, measured at 390dp: labels centered at
 * roughly x=75/170/260/341, with a 62dp tall #0D192B background.
 *
 * Each label is laid out INSIDE its own clickable region, rather than visually
 * translating the text into the next tab's hit target. The regions scale with
 * the available width, while text ellipsizes at high accessibility font scales.
 *
 * Android TalkBack exposes the selected tab through Compose selectable
 * semantics; the original visual colors and 390dp glyph centers are unchanged.
 *
 * The 62dp design bar sits ABOVE Android's own gesture/3-button navigation
 * zone. Android 15+/target 35+ can draw apps edge-to-edge; reserve the system
 * inset without reducing the bar's 62dp tappable height or forging system UI.
 */
@Composable
internal fun ReferenceBottomBar(
    entries: List<Triple<String, String, String>>,
    selectedTab: String,
    onNavigate: (String) -> Unit
) {
    // SVG-derived touch-region boundaries at x=0,120,216,300,390.
    // Ratios preserve the visual anchors without absolute-positioned hit targets.
    val widths = listOf(120f, 96f, 84f, 90f)
    // Separate the application's own four tabs from Android's Back/Home/Recents
    // or gesture handle. A dark 8dp visual gutter remains on pre-edge-to-edge
    // devices, while navigationBarsPadding reserves the actual system inset.
    Column(
        modifier = Modifier.fillMaxWidth()
            .testTag("reference-bottom-safe-area")
            .background(Color(0xFF08111D))
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(62.dp)
                .background(Color(0xFF0D192B)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            entries.forEachIndexed { index, (id, label, _) ->
                Box(
                    modifier = Modifier.weight(widths[index]).fillMaxHeight()
                        .testTag("reference-bottom-tab-$id")
                        .selectable(
                            selected = selectedTab == id,
                            role = Role.Tab,
                            onClick = { onNavigate(id) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        modifier = Modifier.fillMaxWidth()
                            .padding(
                                start = when (index) {
                                    0 -> 30.dp
                                    1, 2 -> 4.dp
                                    else -> 0.dp
                                },
                                end = if (index == 3) 8.dp else 0.dp
                            )
                            .testTag("reference-bottom-label-$id"),
                        fontSize = 12.sp,
                        color = if (selectedTab == id) Cyan else Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Spacer(Modifier.fillMaxWidth().height(8.dp))
    }
}
