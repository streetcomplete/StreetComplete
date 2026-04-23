package de.westnordost.streetcomplete.quests.board_type

import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.ACheckboxGroupQuestForm
import de.westnordost.streetcomplete.quests.ARadioGroupQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import org.jetbrains.compose.resources.stringResource

class AddBoardTypeForm : ACheckboxGroupQuestForm<BoardType, BoardTypeAnswer>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_board_type_map) { confirmOnMap() }
    )

    override val items = BoardType.entries

    @Composable override fun BoxScope.ItemContent(item: BoardType) {
        Text(stringResource(item.text))
    }

    override fun onClickOk(items: Set<BoardType>) {
        applyAnswer(BoardTypeAnswer.BoardTypes(items))
    }

    private fun confirmOnMap() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_board_type_map_title)
            .setMessage(R.string.quest_board_type_map_description)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> applyAnswer(BoardTypeAnswer.NoBoardJustMap) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
