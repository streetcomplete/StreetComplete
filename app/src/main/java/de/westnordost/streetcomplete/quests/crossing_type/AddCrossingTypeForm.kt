package de.westnordost.streetcomplete.quests.crossing_type

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.RAISED
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.TRAFFIC_SIGNALS_ZEBRA
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.ZEBRA
import de.westnordost.streetcomplete.view.image_select.ImageSelectAdapter

class AddCrossingTypeForm : AImageListQuestForm<CrossingType, CrossingType>() {

    val crossingItems = CrossingType.values().filterNot { it == RAISED }.map { it.asItem() }

    val raisedItem = RAISED.asItem()

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
        if (element.tags["traffic_calming"] == "table")
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
