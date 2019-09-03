package de.westnordost.streetcomplete.quests.board_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AGroupedImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class AddBoardTypeForm : AImageListQuestAnswerFragment<String, String>() {
    override val items: List<Item<String>> = listOf(
        Item("history", R.drawable.bicycle_parking_type_stand, R.string.quest_board_type_history),
        Item("geology", R.drawable.bicycle_parking_type_stand, R.string.quest_board_type_geology),
        Item("plants", R.drawable.bicycle_parking_type_stand, R.string.quest_board_type_plants),
        Item("wildlife", R.drawable.bicycle_parking_type_stand, R.string.quest_board_type_wildlife),
        Item("nature", R.drawable.bicycle_parking_type_stand, R.string.quest_board_type_nature),
        Item("public_transport", R.drawable.bicycle_parking_type_stand, R.string.quest_board_type_public_transport),
        Item("notice", R.drawable.bicycle_parking_type_stand, R.string.quest_board_type_notice_board)
    )

    //override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems.single())
    }
}
