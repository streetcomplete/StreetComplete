package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import de.westnordost.streetcomplete.ui.common.MessagesIcon

@Composable
fun MessagesButton(
    onClick: () -> Unit,
    messagesCount: Int,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        MapButton(onClick = onClick) { MessagesIcon() }
        Box(Modifier.align(Alignment.TopEnd)) {
            NotificationBox {
                Text(messagesCount.toString(), textAlign = TextAlign.Center)
            }
        }
    }
}
