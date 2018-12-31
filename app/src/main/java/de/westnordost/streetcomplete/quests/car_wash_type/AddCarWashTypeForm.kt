package de.westnordost.streetcomplete.quests.car_wash_type

import android.os.Bundle
import android.view.View

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.ImageSelectAdapter
import de.westnordost.streetcomplete.view.Item

class AddCarWashTypeForm : ImageListQuestAnswerFragment() {

    override val items = listOf(
        Item(AUTOMATED, R.drawable.car_wash_automated, R.string.quest_carWashType_automated),
        Item(SELF_SERVICE, R.drawable.car_wash_self_service, R.string.quest_carWashType_selfService),
        Item(SERVICE, R.drawable.car_wash_service, R.string.quest_carWashType_service)
    )

    override val itemsPerRow = 3
    override val maxSelectableItems = 3

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageSelector.listeners.add(object :
            ImageSelectAdapter.OnItemSelectionListener {
            override fun onIndexSelected(index: Int) {
                // service is exclusive with everything else
                if (index == 2) {
                    imageSelector.deselect(0)
                    imageSelector.deselect(1)
                } else {
                    imageSelector.deselect(2)
                }
            }

            override fun onIndexDeselected(index: Int) {}
        })
    }

    companion object {
        const val AUTOMATED = "AUTOMATED"
        const val SELF_SERVICE = "SELF_SERVICE"
        const val SERVICE = "SERVICE"
    }
}
