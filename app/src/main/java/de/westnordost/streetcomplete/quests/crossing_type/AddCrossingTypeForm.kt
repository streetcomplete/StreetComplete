package de.westnordost.streetcomplete.quests.crossing_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.*
import de.westnordost.streetcomplete.view.image_select.Item

class AddCrossingTypeForm : AImageListQuestAnswerFragment<CrossingType, CrossingType>() {

    override val items = listOf(
        Item(TRAFFIC_SIGNALS, R.drawable.crossing_type_signals, R.string.quest_crossing_type_signals),
        Item(MARKED, R.drawable.crossing_type_zebra, R.string.quest_crossing_type_marked),
        Item(UNMARKED, R.drawable.crossing_type_unmarked, R.string.quest_crossing_type_unmarked)
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<CrossingType>) {
        applyAnswer(selectedItems.single())
    }
}
