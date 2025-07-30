package de.westnordost.streetcomplete.screens.main.messages

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.BubblePile
import de.westnordost.streetcomplete.ui.common.dialogs.AlertDialogLayout
import de.westnordost.streetcomplete.ui.ktx.proportionalAbsoluteOffset
import kotlinx.coroutines.delay

/** Dialog that tells the user that he can turn off some boring quests in the setting. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestSelectionHintDialog(
    onDismissRequest: () -> Unit,
    onClickOpenSettings: () -> Unit,
    allQuestIconIds: List<Int>,
) {
    val avalanche = remember { Animatable(1.1f) }
    val content = remember { Animatable(0f) }

    LaunchedEffect(allQuestIconIds) {
        avalanche.animateTo(0f, tween(2000, easing = LinearEasing))
        delay(150)
        content.animateTo(1f, tween(300))
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clickable(null, null) { onDismissRequest() }
        ) {
            val bubbleCount = (maxHeight.value * 0.1f).toInt()
            val bubbleSize = maxWidth * 0.25f
            BubblePile(
                count = bubbleCount,
                allIconsIds = allQuestIconIds,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .offset(y = bubbleSize * 0.75f)
                    .proportionalAbsoluteOffset(y = avalanche.value)
                    .graphicsLayer(
                        alpha = (1f - avalanche.value).coerceIn(0f, 1f),
                        rotationX = 5f,
                        scaleX = 1.5f,
                        scaleY = 1.25f,
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    ),
                bubbleSize = bubbleSize
            )
            AlertDialogLayout(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = ((1 - content.value) * 32).dp)
                    .width(280.dp)
                    .alpha(content.value)
                    .shadow(24.dp),
                title = { Text(stringResource(R.string.quest_selection_hint_title)) },
                content = {
                    Text(
                        text = stringResource(R.string.quest_selection_hint_message),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                },
                buttons = {
                    TextButton(onClick = { onDismissRequest(); onClickOpenSettings() }) {
                        Text(stringResource(R.string.quest_streetName_cantType_open_settings))
                    }
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            )
        }
    }
}
