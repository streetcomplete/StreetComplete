package de.westnordost.streetcomplete.quests.board_type

import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AMultipleSelectGroupQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import org.jetbrains.compose.resources.stringResource

class AddBoardTypeForm : AMultipleSelectGroupQuestForm<BoardTypeAnswer, BoardType>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_board_type_map) { confirmOnMap() }
    )

    override val items = BoardType.entries

    @Composable override fun BoxScope.ItemContent(item: BoardTypeAnswer) {
        Text(stringResource(item.text))
    }

    private fun confirmOnMap() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_board_type_map_title)
            .setMessage(R.string.quest_board_type_map_description)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> applyAnswer(setOf(
                BoardTypeAnswer.NoBoardJustMap
            )) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override val buttonPanelAnswers: List<AnswerItem> get() = emptyList()
}
