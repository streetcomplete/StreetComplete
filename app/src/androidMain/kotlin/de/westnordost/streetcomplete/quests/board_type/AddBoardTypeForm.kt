package de.westnordost.streetcomplete.quests.board_type

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.board_type.BoardType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_board_type_geology
import de.westnordost.streetcomplete.resources.quest_board_type_history
import de.westnordost.streetcomplete.resources.quest_board_type_nature
import de.westnordost.streetcomplete.resources.quest_board_type_notice_board
import de.westnordost.streetcomplete.resources.quest_board_type_plants
import de.westnordost.streetcomplete.resources.quest_board_type_public_transport
import de.westnordost.streetcomplete.resources.quest_board_type_sport
import de.westnordost.streetcomplete.resources.quest_board_type_wildlife
import de.westnordost.streetcomplete.ui.common.TextItem

class AddBoardTypeForm : AListQuestForm<BoardTypeAnswer>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_board_type_map) { confirmOnMap() }
    )

    override val items: List<TextItem<BoardTypeAnswer>> = listOf(
        TextItem(HISTORY, Res.string.quest_board_type_history),
        TextItem(GEOLOGY, Res.string.quest_board_type_geology),
        TextItem(PLANTS, Res.string.quest_board_type_plants),
        TextItem(WILDLIFE, Res.string.quest_board_type_wildlife),
        TextItem(NATURE, Res.string.quest_board_type_nature),
        TextItem(PUBLIC_TRANSPORT, Res.string.quest_board_type_public_transport),
        TextItem(SPORT, Res.string.quest_board_type_sport),
        TextItem(NOTICE, Res.string.quest_board_type_notice_board),
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
