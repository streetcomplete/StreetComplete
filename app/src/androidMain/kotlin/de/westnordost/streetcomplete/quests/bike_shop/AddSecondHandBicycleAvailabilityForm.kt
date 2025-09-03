package de.westnordost.streetcomplete.quests.bike_shop

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.bike_shop.SecondHandBicycleAvailability.NEW_AND_SECOND_HAND
import de.westnordost.streetcomplete.quests.bike_shop.SecondHandBicycleAvailability.NO_BICYCLES_SOLD
import de.westnordost.streetcomplete.quests.bike_shop.SecondHandBicycleAvailability.ONLY_NEW
import de.westnordost.streetcomplete.quests.bike_shop.SecondHandBicycleAvailability.ONLY_SECOND_HAND
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_bicycle_shop_second_hand_new_and_used
import de.westnordost.streetcomplete.resources.quest_bicycle_shop_second_hand_no_bicycles
import de.westnordost.streetcomplete.resources.quest_bicycle_shop_second_hand_only_new
import de.westnordost.streetcomplete.resources.quest_bicycle_shop_second_hand_only_used
import de.westnordost.streetcomplete.ui.common.TextItem

class AddSecondHandBicycleAvailabilityForm : AListQuestForm<SecondHandBicycleAvailability>() {

    override val items = listOf(
        TextItem(ONLY_NEW, Res.string.quest_bicycle_shop_second_hand_only_new),
        TextItem(NEW_AND_SECOND_HAND, Res.string.quest_bicycle_shop_second_hand_new_and_used),
        TextItem(ONLY_SECOND_HAND, Res.string.quest_bicycle_shop_second_hand_only_used),
        TextItem(NO_BICYCLES_SOLD, Res.string.quest_bicycle_shop_second_hand_no_bicycles),
    )
}
