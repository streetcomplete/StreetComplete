package de.westnordost.streetcomplete.quests.crossing_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class AddCrossingTypeForm : AImageListQuestAnswerFragment<String, String>() {

    override val items = listOf(
        Item("traffic_signals", R.drawable.crossing_type_signals, R.string.quest_crossing_type_signals),
        Item("uncontrolled", R.drawable.crossing_type_zebra, R.string.quest_crossing_type_uncontrolled),
        Item("unmarked", R.drawable.crossing_type_unmarked, R.string.quest_crossing_type_unmarked)
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems.single())
    }
}
