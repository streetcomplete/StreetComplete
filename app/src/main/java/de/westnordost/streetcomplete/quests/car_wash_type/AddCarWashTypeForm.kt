package de.westnordost.streetcomplete.quests.car_wash_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.car_wash_type.CarWashType.AUTOMATED
import de.westnordost.streetcomplete.quests.car_wash_type.CarWashType.SELF_SERVICE
import de.westnordost.streetcomplete.quests.car_wash_type.CarWashType.SERVICE
import de.westnordost.streetcomplete.view.image_select.Item

class AddCarWashTypeForm : AImageListQuestForm<CarWashType, List<CarWashType>>() {

    override val items = listOf(
        Item(AUTOMATED, R.drawable.car_wash_automated, R.string.quest_carWashType_automated),
        Item(SELF_SERVICE, R.drawable.car_wash_self_service, R.string.quest_carWashType_selfService),
        Item(SERVICE, R.drawable.car_wash_service, R.string.quest_carWashType_service)
    )

    override val itemsPerRow = 3
    override val maxSelectableItems = -1

    override fun onClickOk(selectedItems: List<CarWashType>) {
        applyAnswer(selectedItems)
    }
}
