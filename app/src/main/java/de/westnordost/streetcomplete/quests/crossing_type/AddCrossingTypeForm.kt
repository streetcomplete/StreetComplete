package de.westnordost.streetcomplete.quests.crossing_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.MARKED
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.MARKED_RAISED
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.TRAFFIC_SIGNALS
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.UNMARKED
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.UNMARKED_RAISED
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.ZEBRA
import de.westnordost.streetcomplete.view.image_select.Item

class AddCrossingTypeForm : AImageListQuestAnswerFragment<CrossingType, CrossingType>() {

    override val items = mutableListOf(
        Item(TRAFFIC_SIGNALS, R.drawable.crossing_type_signals, R.string.quest_crossing_type_signals_controlled),
        Item(MARKED, R.drawable.crossing_type_zebra, R.string.quest_crossing_type_marked),
        Item(ZEBRA, R.drawable.crossing_type_zebra, R.string.quest_crossing_type_zebra),
        Item(UNMARKED, R.drawable.crossing_type_unmarked, R.string.quest_crossing_type_unmarked)
    )

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        if (osmElement!!.tags["traffic_calming"] == null)
            items.addAll(listOf(
                Item(MARKED_RAISED, R.drawable.crossing_type_zebra, R.string.quest_crossing_type_marked_raised),
                Item(UNMARKED_RAISED, R.drawable.crossing_type_unmarked, R.string.quest_crossing_type_unmarked_raised),
            ))
    }

    override val itemsPerRow = 3

    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<CrossingType>) {
        applyAnswer(selectedItems.single())
    }
}
