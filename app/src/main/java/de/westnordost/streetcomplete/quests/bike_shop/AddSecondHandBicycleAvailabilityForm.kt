package de.westnordost.streetcomplete.quests.bike_shop

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem

class AddSecondHandBicycleAvailabilityForm : AListQuestForm<SecondHandBicycleAvailability>() {

    override val items = listOf(
        TextItem(SecondHandBicycleAvailability.ONLY_NEW, R.string.quest_bicycle_shop_second_hand_only_new),
        TextItem(SecondHandBicycleAvailability.NEW_AND_SECOND_HAND, R.string.quest_bicycle_shop_second_hand_new_and_used),
        TextItem(SecondHandBicycleAvailability.ONLY_SECOND_HAND, R.string.quest_bicycle_shop_second_hand_only_used),
        TextItem(SecondHandBicycleAvailability.NO_BICYCLES_SOLD, R.string.quest_bicycle_shop_second_hand_no_bicycles),
    )
}
