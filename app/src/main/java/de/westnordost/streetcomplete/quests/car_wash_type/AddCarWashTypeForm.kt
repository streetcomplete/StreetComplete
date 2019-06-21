package de.westnordost.streetcomplete.quests.car_wash_type

import android.os.Bundle
import android.view.View

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.ImageSelectAdapter
import de.westnordost.streetcomplete.view.Item

class AddCarWashTypeForm : AImageListQuestAnswerFragment<AddCarWashTypeForm.CarWashOption, CarWashType>() {

    override val items = listOf(
        Item(CarWashOption.AUTOMATED, R.drawable.car_wash_automated, R.string.quest_carWashType_automated),
        Item(CarWashOption.SELF_SERVICE, R.drawable.car_wash_self_service, R.string.quest_carWashType_selfService),
        Item(CarWashOption.SERVICE, R.drawable.car_wash_service, R.string.quest_carWashType_service)
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

    override fun onClickOk(selectedItems: List<CarWashOption>) {
        applyAnswer(CarWashType(
            isSelfService = selectedItems.contains(CarWashOption.SELF_SERVICE),
            isAutomated = selectedItems.contains(CarWashOption.AUTOMATED)
        ))
    }

    enum class CarWashOption { AUTOMATED, SELF_SERVICE, SERVICE }
}
