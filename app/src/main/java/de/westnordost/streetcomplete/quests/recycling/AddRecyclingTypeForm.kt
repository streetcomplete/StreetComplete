package de.westnordost.streetcomplete.quests.recycling

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.recycling.RecyclingType.OVERGROUND_CONTAINER
import de.westnordost.streetcomplete.quests.recycling.RecyclingType.RECYCLING_CENTRE
import de.westnordost.streetcomplete.quests.recycling.RecyclingType.UNDERGROUND_CONTAINER
import de.westnordost.streetcomplete.view.image_select.Item

class AddRecyclingTypeForm : AImageListQuestForm<RecyclingType, RecyclingType>() {

    override val items = listOf(
        Item(OVERGROUND_CONTAINER, R.drawable.recycling_container, R.string.overground_recycling_container),
        Item(UNDERGROUND_CONTAINER, R.drawable.recycling_container_underground, R.string.underground_recycling_container),
        Item(RECYCLING_CENTRE, R.drawable.recycling_centre, R.string.recycling_centre)
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<RecyclingType>) {
        applyAnswer(selectedItems.single())
    }
}
