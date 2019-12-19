package de.westnordost.streetcomplete.quests.car_wash_type

import android.os.Bundle
import android.view.View

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.ImageSelectAdapter
import de.westnordost.streetcomplete.view.Item
import de.westnordost.streetcomplete.quests.car_wash_type.AddCarWashTypeForm.CarWashOption.*

class AddCarWashTypeForm : AImageListQuestAnswerFragment<AddCarWashTypeForm.CarWashOption, CarWashType>() {

    override val items = listOf(
        Item(AUTOMATED, R.drawable.car_wash_automated, R.string.quest_carWashType_automated),
        Item(SELF_SERVICE, R.drawable.car_wash_self_service, R.string.quest_carWashType_selfService),
        Item(SERVICE, R.drawable.car_wash_service, R.string.quest_carWashType_service)
    )

    override val itemsPerRow = 3
    override val maxSelectableItems = 3

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageSelector.listeners.add(object : ImageSelectAdapter.OnItemSelectionListener {
            override fun onIndexSelected(index: Int) {
                val value = imageSelector.items[index].value!!
                // service is exclusive with everything else
                if (value == SERVICE) {
                    imageSelector.deselect(imageSelector.indexOf(AUTOMATED))
                    imageSelector.deselect(imageSelector.indexOf(SELF_SERVICE))
                } else {
                    imageSelector.deselect(imageSelector.indexOf(SERVICE))
                }
            }

            override fun onIndexDeselected(index: Int) {}
        })
    }

    override fun onClickOk(selectedItems: List<CarWashOption>) {
        applyAnswer(CarWashType(
            isSelfService = selectedItems.contains(SELF_SERVICE),
            isAutomated = selectedItems.contains(AUTOMATED)
        ))
    }

    enum class CarWashOption { AUTOMATED, SELF_SERVICE, SERVICE }
}
