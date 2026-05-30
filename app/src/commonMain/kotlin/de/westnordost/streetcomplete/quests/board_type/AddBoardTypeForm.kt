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
import de.westnordost.streetcomplete.data.osm.osmquests.AltAnswer
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.CheckboxGroup
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.CheckboxGroupQuestForm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddBoardTypeForm(
    onAnswer: (QuestAnswer<BoardTypeAnswer>) -> Unit
) {
    var selectedOptions by rememberSerializable { mutableStateOf(emptySet<BoardType>()) }
    var confirmIsMap by remember { mutableStateOf(false) }

    CheckboxGroupQuestForm(
        items = BoardType.entries,
        itemContent = { Text(stringResource(it.text)) },
        onAnswer = {
            onAnswer(when (it) {
                is Answer<Set<BoardType>> -> Answer(BoardTypeAnswer.BoardTypes(it.value))
                is AltAnswer -> it
            })
        },
        otherAnswers = listOf(
            AnswerItem(stringResource(Res.string.quest_board_type_map)) { confirmIsMap = true }
        )
    )

    if (confirmIsMap) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmIsMap = false },
            onConfirmed = { onAnswer(Answer(BoardTypeAnswer.NoBoardJustMap)) },
            titleText = stringResource(Res.string.quest_board_type_map_title),
            text = { Text(stringResource(Res.string.quest_board_type_map_description)) },
        )
    }
}
