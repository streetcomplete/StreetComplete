package de.westnordost.streetcomplete.quests.kerb_height

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.image_select.Item

class AddKerbHeightForm : AImageListQuestAnswerFragment<String, String>() {

    override val items = listOf(
        // TODO IMAGES
        Item("raised", R.drawable.bicycle_parking_type_stand, R.string.quest_kerb_height_raised),
        Item("lowered", R.drawable.bicycle_parking_type_stand, R.string.quest_kerb_height_lowered),
        Item("flush", R.drawable.bicycle_parking_type_stand, R.string.quest_kerb_height_flush)
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems.single())
    }
}
