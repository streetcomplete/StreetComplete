package de.westnordost.streetcomplete.quests.recycling

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class AddRecyclingTypeForm : ImageListQuestAnswerFragment() {

    override fun getItems() = arrayOf(
        Item("overground", R.drawable.recycling_container, R.string.overground_recycling_container),
        Item("underground", R.drawable.recycling_container_underground, R.string.underground_recycling_container),
        Item("centre", R.drawable.recycling_centre, R.string.recycling_centre)
    )

    override fun getItemsPerRow() = 3
}
