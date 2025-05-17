package de.westnordost.streetcomplete.quests.board_type

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.board_type.BoardType.*

class AddBoardTypeForm : AListQuestForm<BoardTypeAnswer>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_board_type_map) { confirmOnMap() }
    )

    override val items: List<TextItem<BoardTypeAnswer>> = listOf(
        TextItem(HISTORY, R.string.quest_board_type_history),
        TextItem(GEOLOGY, R.string.quest_board_type_geology),
        TextItem(PLANTS, R.string.quest_board_type_plants),
        TextItem(WILDLIFE, R.string.quest_board_type_wildlife),
        TextItem(NATURE, R.string.quest_board_type_nature),
        TextItem(PUBLIC_TRANSPORT, R.string.quest_board_type_public_transport),
        TextItem(SPORT, R.string.quest_board_type_sport),
        TextItem(NOTICE, R.string.quest_board_type_notice_board),
    )

    private fun confirmOnMap() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_board_type_map_title)
            .setMessage(R.string.quest_board_type_map_description)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> applyAnswer(NoBoardJustMap) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
