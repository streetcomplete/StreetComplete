package de.westnordost.streetcomplete.quests.board_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AGroupedImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class AddBoardTypeForm : AGroupedImageListQuestAnswerFragment<String, String>() {
    override val topItems: List<Item<String>> = listOf(
        Item("history", R.drawable.bicycle_parking_type_stand, R.string.quest_board_type_history),
        Item("notice", R.drawable.bicycle_parking_type_stand, R.string.quest_board_type_notice_board),
        Item("public_transport", R.drawable.bicycle_parking_type_stand, R.string.quest_board_type_public_transport)
    )

    override val allItems: List<Item<String>> = listOf(
            Item("nature", R.drawable.bicycle_parking_type_stand, R.string.quest_board_type_nature, null, listOf(
                    Item("geology", R.drawable.bicycle_parking_type_stand, R.string.quest_board_type_geology),
                    Item("plants", R.drawable.bicycle_parking_type_stand, R.string.quest_board_type_plants),
                    Item("wildlife", R.drawable.bicycle_parking_type_stand, R.string.quest_board_type_wildlife)
            )
    ))

    override fun onClickOk(value: String) {
        applyAnswer(value)
    }

    //override val itemsPerRow = 3
}
