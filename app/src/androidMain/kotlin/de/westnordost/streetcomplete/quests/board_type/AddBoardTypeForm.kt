package de.westnordost.streetcomplete.quests.board_type

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.CheckboxGroup
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

class AddBoardTypeForm : AbstractOsmQuestForm<BoardTypeAnswer>() {

    @Composable
    override fun Content() {
        var selectedOptions by rememberSerializable { mutableStateOf(emptySet<BoardType>()) }
        var confirmIsMap by remember { mutableStateOf(false) }

        QuestForm(
            answers = Confirm(isComplete = selectedOptions.isNotEmpty()) {
                applyAnswer(BoardTypeAnswer.BoardTypes(selectedOptions))
            },
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_board_type_map)) { confirmIsMap = true }
            )
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    LocalTextStyle provides MaterialTheme.typography.body2
                ) {
                    Text(stringResource(Res.string.quest_multiselect_hint))
                }
                CheckboxGroup(
                    options = BoardType.entries,
                    onSelectionsChange = { selectedOptions = it },
                    selectedOptions = selectedOptions,
                    itemContent = { Text(stringResource(it.text)) },
                )
            }
        }
        if (confirmIsMap) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmIsMap = false },
                onConfirmed = { applyAnswer(BoardTypeAnswer.NoBoardJustMap) },
                titleText = stringResource(Res.string.quest_board_type_map_title),
                text = { Text(stringResource(Res.string.quest_board_type_map_description)) },
            )
        }
    }
}
