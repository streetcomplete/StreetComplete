package de.westnordost.streetcomplete.quests.parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class AddParkingTypeForm : AImageListQuestAnswerFragment<String,String>() {

    override val items = listOf(
        Item("surface", R.drawable.parking_type_surface, R.string.quest_parkingType_surface),
        Item("underground", R.drawable.parking_type_underground, R.string.quest_parkingType_underground),
        Item("multi-storey", R.drawable.parking_type_multistorey, R.string.quest_parkingType_multiStorage)
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems.single())
    }
}
