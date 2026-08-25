package de.westnordost.streetcomplete.screens.main.bottom_sheet.split_way

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.bottom_sheet.scissorsPainter
import de.westnordost.streetcomplete.ui.common.UndoIcon
import de.westnordost.streetcomplete.util.sound.SoundEffectPlayer
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun SplitWayFormContent(
    onClickCancel: () -> Unit,
    canCutHere: Boolean,
    onCut: () -> Unit,
    hasCuts: Boolean,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier,
    soundEffectPlayer: SoundEffectPlayer = koinInject()
) {
    val snipAnimation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Res.string.quest_split_way_tutorial2),
                style = MaterialTheme.typography.body2,
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
            )
            Box(Modifier.fillMaxWidth()) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = hasCuts,
                    modifier = Modifier.align(Alignment.CenterStart),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    OutlinedButton(
                        onClick = {
                            onUndo()
                            soundEffectPlayer.play("plop2.wav")
                        },
                        shape = CircleShape,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        UndoIcon()
                    }
                }

                Button(
                    modifier = Modifier.align(Alignment.Center),
                    onClick = {
                        onCut()

                        scope.launch {
                            snipAnimation.animateTo(1f)
                            snipAnimation.animateTo(0f)
                        }
                        soundEffectPlayer.play("snip.wav")
                    },
                    enabled = canCutHere
                ) {
                    Image(
                        painter = scissorsPainter(snipAnimation.value),
                        contentDescription = stringResource(Res.string.split_way),
                    )
                }
            }
        }

        Divider()

        // button panel
        Row(Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
        ) {
            TextButton(onClickCancel) { Text(stringResource(Res.string.cancel)) }
        }
    }
}
