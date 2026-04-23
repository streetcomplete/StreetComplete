package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import de.westnordost.streetcomplete.ui.common.VerticalDivider
import org.jetbrains.compose.resources.stringResource

@Immutable
data class Answer(val text: String, val action: () -> Unit)

/** Horizontal button bar for bottom sheets that can be multi-line if it does not all fit in one
 *  line and places subtle dividers in-between the [answers]. Also, optionally [otherAnswers] will
 *  be shown in a dropdown button aligned to the start of the bar. */
@Composable
fun QuestAnswerButtonBar(
    modifier: Modifier = Modifier,
    answers: List<Answer> = emptyList(),
    otherAnswers: List<Answer> = emptyList(),
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.End),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        if (otherAnswers.isNotEmpty()) {
            OtherAnswersTextButton(answers = otherAnswers)
            Spacer(Modifier.weight(1f))
        }
        for ((index, item) in answers.withIndex()) {
            if (otherAnswers.isNotEmpty() || index != 0) {
                VerticalDivider(Modifier.height(24.dp))
            }
            TextButton(onClick = item.action) { Text(item.text) }
        }
    }
}

@Composable
private fun OtherAnswersTextButton(
    answers: List<Answer>,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier) {
        TextButton(onClick = { expanded = true }) {
            Text(stringResource(Res.string.quest_generic_otherAnswers2))
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
private fun QuestAnswerButtonBarPreview() {
    QuestAnswerButtonBar(
        answers = listOf(
            Answer("No") {},
            Answer("Perhaps") {},
            Answer("Depends how you define \"No\"") {},
            Answer("Yes") {},
        ),
        otherAnswers = listOf(
            Answer("Depends how you define \"Yes\"") {},
            Answer("Can't say") {}
        )
    )
}
