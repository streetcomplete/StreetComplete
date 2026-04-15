package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.speech_bubble.SpeechBubbleNoArrow
import de.westnordost.streetcomplete.ui.ktx.fadingVerticalScrollEdges

/** Speech bubble for the quest answer, i.e. content and/or button bar answers */
@Composable
fun QuestAnswerBubble(
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
    answers: List<Answer> = emptyList(),
    otherAnswers: List<Answer> = emptyList(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    SpeechBubbleNoArrow(
        modifier = modifier,
        elevation = elevation,
        contentPadding = PaddingValues.Zero
    ) {
        ProvideTextStyle(MaterialTheme.typography.body1) {
            Column(Modifier
                .fadingVerticalScrollEdges(scrollState, 32.dp)
                .verticalScroll(scrollState),
            ) {
                if (content != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(contentPadding),
                        contentAlignment = Alignment.Center,
                        content = content
                    )
                }
                if (content != null && (answers.isNotEmpty() || otherAnswers.isNotEmpty())) {
                    Divider()
                }
                if (answers.isNotEmpty() || otherAnswers.isNotEmpty()) {
                    QuestAnswerButtonBar(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        answers = answers,
                        otherAnswers = otherAnswers
                    )
                }
            }
        }
    }
}
