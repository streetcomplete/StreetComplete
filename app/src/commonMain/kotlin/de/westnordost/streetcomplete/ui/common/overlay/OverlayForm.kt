package de.westnordost.streetcomplete.ui.common.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_generic_otherAnswers2
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import de.westnordost.streetcomplete.ui.common.FloatingOkButton
import de.westnordost.streetcomplete.ui.common.MoreIcon
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.speech_bubble.SpeechBubbleNoArrow
import de.westnordost.streetcomplete.ui.theme.Dimensions
import de.westnordost.streetcomplete.ui.theme.speechBubbleCornerRadius
import de.westnordost.streetcomplete.ui.theme.titleMedium
import de.westnordost.streetcomplete.ui.theme.titleSmall
import org.jetbrains.compose.resources.stringResource

/** A generic overlay form containing the center-aligned [content], padded with [contentPadding].
 *  Above it, an optional bubble with a [label] (in which the element is usually named).
 *
 *  Below the content, there's an empty bar that contains only a "more" icon button on the start
 *  that, when tapped, opens a dropdown menu containing [otherAnswers].
 *
 *  Floating in the lower end corner, an OK button for confirmation. [okIsVisible] should be true
 *  when the form is complete, while [okIsEnabled] should be true when any changes have been made.
 *  */
@Composable
fun OverlayForm(
    okIsVisible: Boolean,
    okIsEnabled: Boolean,
    onClickOk: () -> Unit,
    modifier: Modifier = Modifier,
    label: AnnotatedString? = null,
    otherAnswers: Answers = Answers(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val windowInfo = LocalWindowInfo.current

    val elevation = 4.dp

    Box(modifier = modifier.sizeIn(maxWidth = Dimensions.getMaxQuestFormWidth(windowInfo))) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .safeDrawingPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (label != null) {
                SpeechBubbleNoArrow(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    elevation = elevation,
                ) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.titleMedium,
                        LocalContentAlpha provides ContentAlpha.medium
                    ) {
                        Text(label)
                    }
                }
            }

            OverlayAnswerBubble(
                modifier = Modifier.fillMaxWidth(),
                elevation = elevation,
                otherAnswers = otherAnswers.answers,
                contentPadding = contentPadding,
                content = content
            )
        }
        FloatingOkButton(
            visible = okIsVisible,
            enabled = okIsEnabled,
            onClick = onClickOk,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .safeDrawingPadding()
                .padding(8.dp)
        )
    }
}

@Composable
private fun OverlayAnswerBubble(
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
    otherAnswers: List<Answer> = emptyList(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    SpeechBubbleNoArrow(
        modifier = modifier,
        elevation = elevation,
        contentPadding = PaddingValues.Zero
    ) {
        ProvideTextStyle(MaterialTheme.typography.body1) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(contentPadding),
                    contentAlignment = Alignment.Center,
                    content = content
                )
                Divider()
                MoreButton(answers = otherAnswers)
            }
        }
    }
}

@Composable
private fun MoreButton(
    answers: List<Answer>,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier) {
        IconButton(onClick = { expanded = true }) {
            MoreIcon()
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (answer in answers) {
                DropdownMenuItem(onClick = { expanded = false; answer.action() }) {
                    Text(answer.text)
                }
            }
        }
    }
}

@Preview
@Composable
private fun OverlayFormPreview() {
    OverlayForm(
        okIsVisible = true,
        okIsEnabled = false,
        onClickOk = {},
        label = AnnotatedString("some text"),
        otherAnswers = Answers(
            Answer("Can't say") {},
            Answer("Can say") {},
        )
    ) {
        Text(LoremIpsum(50).values.joinToString(" "))
    }
}
