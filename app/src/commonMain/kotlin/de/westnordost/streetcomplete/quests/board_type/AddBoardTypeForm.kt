package de.westnordost.streetcomplete.quests.board_type

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.CheckboxGroupQuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddBoardTypeForm(
    on: (QuestAction<BoardTypeAnswer>) -> Unit
) {
    var selectedOptions by rememberSerializable { mutableStateOf(emptySet<BoardType>()) }
    var confirmIsMap by remember { mutableStateOf(false) }

    CheckboxGroupQuestForm(
        items = BoardType.entries,
        itemContent = { Text(stringResource(it.text)) },
        on = {
            on(when (it) {
                is Answer<Set<BoardType>> -> Answer(BoardTypeAnswer.BoardTypes(it.value))
                is Action -> it
            })
        },
        otherAnswers = listOf(
            AnswerItem(stringResource(Res.string.quest_board_type_map)) { confirmIsMap = true }
        )
    )

    if (confirmIsMap) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmIsMap = false },
            onConfirmed = { on(Answer(BoardTypeAnswer.NoBoardJustMap)) },
            titleText = stringResource(Res.string.quest_board_type_map_title),
            text = { Text(stringResource(Res.string.quest_board_type_map_description)) },
        )
    }
}
