// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import com.brendigo.systivra.ui.LocalizedText as Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Read one real result where it was tapped, without forcing the user to scroll
 * past the original reference layout. Keeps Android's accessible dialog focus.
 */
@Composable
internal fun ReferenceQuickResultDialog(
    testTag: String,
    title: String,
    value: String,
    source: String?,
    onDismiss: () -> Unit,
    onCopy: (() -> Unit)? = null
) {
    AlertDialog(
        modifier = Modifier.testTag(testTag),
        onDismissRequest = onDismiss,
        containerColor = Raised,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { Text(title) },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(value, color = if (value == "Unavailable") Muted else TextPrimary)
                if (!source.isNullOrBlank()) Text("Source: $source", color = Muted)
                if (onCopy != null) {
                    TextButton(onClick = onCopy) { Text("Copy measurement") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
