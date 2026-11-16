package com.brendigo.systivra.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
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
            title, modifier = Modifier.semantics { heading() },
            fontSize = 30.sp, fontWeight = FontWeight.ExtraBold,
            maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle, fontSize = 22.sp, fontWeight = FontWeight.Bold,
            maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Text(
            caption, fontSize = 13.sp, color = Muted,
            maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(10.dp))
    }
}
