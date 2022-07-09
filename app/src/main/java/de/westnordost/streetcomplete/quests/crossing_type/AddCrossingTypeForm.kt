package de.westnordost.streetcomplete.quests.crossing_type

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.MARKED
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.RAISED
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.TRAFFIC_SIGNALS
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.TRAFFIC_SIGNALS_ZEBRA
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.UNMARKED
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.ZEBRA
import de.westnordost.streetcomplete.view.image_select.ImageSelectAdapter
import de.westnordost.streetcomplete.view.image_select.Item

class AddCrossingTypeForm : AImageListQuestForm<CrossingType, CrossingType>() {

    val crossingItems = listOf(
        Item(TRAFFIC_SIGNALS, R.drawable.crossing_type_signals, R.string.quest_crossing_type_signals_controlled),
        Item(TRAFFIC_SIGNALS_ZEBRA, R.drawable.crossing_type_signals_zebra, R.string.quest_crossing_type_signals_controlled_zebra),
        Item(ZEBRA, R.drawable.crossing_type_zebra, R.string.quest_crossing_type_zebra),
        Item(MARKED, R.drawable.crossing_type_marked, R.string.quest_crossing_type_marked),
        Item(UNMARKED, R.drawable.crossing_type_unmarked, R.string.quest_crossing_type_unmarked)
    )

    val raisedItem = Item(RAISED, R.drawable.traffic_calming_table, R.string.quest_crossing_type_raised)

    override val items = crossingItems + raisedItem

    override val maxSelectableItems = 3 // see onIndexSelected for actual limit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // remove zebra ref and table on some resurvey answers?
        // table yes, but zebra rather not...

        imageSelector.listeners.add(object : ImageSelectAdapter.OnItemSelectionListener {
            override fun onIndexSelected(index: Int) {
                val value = imageSelector.items[index].value!!

                // only one crossing type allowed
                if (value in crossingItems.map { it.value!! }) {
                    // we selected a crossing item -> deselect other selected crossing items
                    imageSelector.selectedIndices.forEach {
                        if (it != index && imageSelector.items[it].value != RAISED)
                            imageSelector.deselect(it)
                    }
                }
            }

            override fun onIndexDeselected(index: Int) {}
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (osmElement!!.tags["traffic_calming"] == "table")
            imageSelector.select(imageSelector.items.indexOf(raisedItem))
    }

    override val itemsPerRow = 3

    override fun isFormComplete() = imageSelector.selectedIndices.isNotEmpty()
        && (!imageSelector.selectedItems.contains(raisedItem) || imageSelector.selectedIndices.size == 2)

    override fun onClickOk(selectedItems: List<CrossingType>) {
        val l = selectedItems.toMutableList()
        if (selectedItems.contains(RAISED)) {
            l.remove(RAISED)
            l.single().raised = true
        }
        applyAnswer(l.single())
    }
}
