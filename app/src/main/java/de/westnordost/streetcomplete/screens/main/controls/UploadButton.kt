package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import de.westnordost.streetcomplete.ui.common.UploadIcon

@Composable
fun UploadButton(
    onClick: () -> Unit,
    unsyncedEditsCount: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(modifier) {
        MapButton(onClick = onClick, enabled = enabled) { UploadIcon() }
        if (unsyncedEditsCount > 0) {
            MapButtonNotification {
                Text(unsyncedEditsCount.toString(), textAlign = TextAlign.Center)
            }
        }
    }
}
