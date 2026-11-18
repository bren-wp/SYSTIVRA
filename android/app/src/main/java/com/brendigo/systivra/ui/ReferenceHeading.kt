package com.brendigo.systivra.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared first-viewport geometry from the original 390×844 vector boards.
 * Text has a separate +4dp x inset from the cards (20dp card x, 24dp text x).
 * Adjusting the header's own position preserves real scrollable content below.
 */
@Composable
internal fun ReferenceHeading(title: String, subtitle: String, caption: String) {
    Column(Modifier.padding(start = 4.dp, top = 14.dp)) {
        Text(
            title, modifier = Modifier.testTag("reference-heading-title").semantics { heading() },
            fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary,
            // Original SVGs use Arial; Android's default Roboto glyphs are
            // ~3.5% narrower for 30sp headers in real 390px screenshots.
            style = TextStyle(textGeometricTransform = TextGeometricTransform(scaleX = 1.035f)),
            maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            modifier = Modifier.offset(y = 3.dp).testTag("reference-heading-subtitle"),
            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
            style = TextStyle(textGeometricTransform = TextGeometricTransform(scaleX = 1.015f)),
            maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Text(
            caption,
            modifier = Modifier.offset(y = 2.dp).testTag("reference-heading-caption"),
            fontSize = 13.sp, color = Muted,
            // Muted Roboto captions measured ~5% wider than reference Arial.
            style = TextStyle(textGeometricTransform = TextGeometricTransform(scaleX = 0.95f)),
            maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(10.dp))
    }
}
