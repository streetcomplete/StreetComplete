package de.westnordost.streetcomplete.screens.main.messages

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/** Dialog that shows a message that the user has X unread messages in his OSM inbox */
@Composable
fun UnreadMessagesDialog(
    unreadMessageCount: Int,
    onDismissRequest: () -> Unit,
    onClickOpenMessages: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density
    val envelope = remember { Animatable(-1f) }
    val envelopeOpen = remember { Animatable(0f) }
    val content = remember { Animatable(0f) }

    LaunchedEffect(unreadMessageCount) {
        // TODO soundFx.play(R.raw.sliding_envelope) - should be provided via composition locals
        //      but that only becomes convenient if there are not entry points to compose all over the place
        envelope.animateTo(0f, tween(600))
        delay(150)
        launch { envelopeOpen.animateTo(1f, tween(300)) }
        launch { content.animateTo(1f, tween(300, 150)) }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(null, null) { onDismissRequest() },
            contentAlignment = Alignment.Center
        ) {
            Envelope(
                opening = envelopeOpen.value,
                modifier = modifier.graphicsLayer {
                    val e = envelope.value
                    rotationZ = e * 40f
                    rotationY = e * 45f
                    alpha = 1f - 0.8f * abs(e)
                    translationX = e * 400 * density
                    translationY = -e * 60 * density
                }
            ) {
                UnreadMessagesContent(
                    unreadMessageCount = unreadMessageCount,
                    onClickOpenMessages = { onDismissRequest(); onClickOpenMessages() },
                    modifier = Modifier.graphicsLayer {
                        val c = content.value
                        val scale = 0.8f + 0.2f * c
                        scaleX = scale
                        scaleY = scale
                        alpha = 0.4f + c * 0.6f
                        translationY = (140f * (1f - c)) * density
                    }
                    .shadow(24.dp)
                )
            }
        }
    }
}
@Composable
private fun Envelope(
    opening: Float,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Image(painterResource(R.drawable.mail_back), null)
        if (opening > 0.5f) Image(openMailPainter(progress = opening), null)
        content()
        Image(painterResource(R.drawable.mail_front), null)
        if (opening <= 0.5f) Image(openMailPainter(progress = opening), null)
    }
}

@Composable
private fun UnreadMessagesContent(
    unreadMessageCount: Int,
    onClickOpenMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.width(240.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.unread_messages_message, unreadMessageCount),
                textAlign = TextAlign.Center,
            )
            Button(onClick = onClickOpenMessages) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painterResource(R.drawable.ic_open_in_browser_24dp), null)
                    Text(stringResource(R.string.unread_messages_button))
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewUnreadMessagesDialog() {
    UnreadMessagesDialog(
        unreadMessageCount = 9,
        onDismissRequest = {},
        onClickOpenMessages = {}
    )
}
