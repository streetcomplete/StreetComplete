package de.westnordost.streetcomplete.quests.bike_parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class AddBikeParkingTypeForm : AImageListQuestAnswerFragment<String, String>() {

    override val items = listOf(
        Item("stands", R.drawable.bicycle_parking_type_stand, R.string.quest_bicycle_parking_type_stand),
        Item("wall_loops", R.drawable.bicycle_parking_type_wheelbenders, R.string.quest_bicycle_parking_type_wheelbender),
        Item("shed", R.drawable.bicycle_parking_type_shed, R.string.quest_bicycle_parking_type_shed),
        Item("lockers", R.drawable.bicycle_parking_type_lockers, R.string.quest_bicycle_parking_type_locker),
        Item("building", R.drawable.bicycle_parking_type_building, R.string.quest_bicycle_parking_type_building)
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems.single())
    }
}
