package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp

/** Content area for the quest answer, i.e. [content] and/or button bar answers */
@Composable
fun QuestAnswerContent(
    modifier: Modifier = Modifier,
    answers: List<AnswerItem> = emptyList(),
    otherAnswers: @Composable (() -> List<AnswerItem>)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    ProvideTextStyle(MaterialTheme.typography.body1) {
        Column(modifier) {
            if (content != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(contentPadding)
                        .clipToBounds(),
                    contentAlignment = Alignment.Center,
                    content = content
                )
            }
            if (content != null && (answers.isNotEmpty() || otherAnswers != null)) {
                Divider()
            }
            if (answers.isNotEmpty() || otherAnswers != null) {
                QuestAnswerButtonBar(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    answers = answers,
                    otherAnswers = otherAnswers
                )
            }
        }
    }
}
