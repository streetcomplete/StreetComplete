package de.westnordost.streetcomplete.quests.parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class AddParkingTypeForm : ImageListQuestAnswerFragment() {

    override fun getItems() = arrayOf(
        Item("surface", R.drawable.parking_type_surface, R.string.quest_parkingType_surface),
        Item("underground", R.drawable.parking_type_underground, R.string.quest_parkingType_underground),
        Item("multi-storey", R.drawable.parking_type_multistorey, R.string.quest_parkingType_multiStorage)
    )

    override fun getItemsPerRow() = 3
}
