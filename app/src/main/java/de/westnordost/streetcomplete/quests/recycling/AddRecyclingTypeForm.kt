package de.westnordost.streetcomplete.quests.recycling

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.recycling.RecyclingType.*
import de.westnordost.streetcomplete.view.Item

class AddRecyclingTypeForm : AImageListQuestAnswerFragment<RecyclingType, RecyclingType>() {

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
