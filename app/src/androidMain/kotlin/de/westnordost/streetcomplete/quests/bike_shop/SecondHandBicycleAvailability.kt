package de.westnordost.streetcomplete.quests.bike_shop

import de.westnordost.streetcomplete.quests.bike_shop.SecondHandBicycleAvailability.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_bicycle_shop_second_hand_new_and_used
import de.westnordost.streetcomplete.resources.quest_bicycle_shop_second_hand_no_bicycles
import de.westnordost.streetcomplete.resources.quest_bicycle_shop_second_hand_only_new
import de.westnordost.streetcomplete.resources.quest_bicycle_shop_second_hand_only_used
import org.jetbrains.compose.resources.StringResource

enum class SecondHandBicycleAvailability(val osmValue: String?) {
    ONLY_NEW("no"),
    NEW_AND_SECOND_HAND("yes"),
    ONLY_SECOND_HAND("only"),
    NO_BICYCLES_SOLD(null),
}

val SecondHandBicycleAvailability.text: StringResource get() = when (this) {
    ONLY_NEW -> Res.string.quest_bicycle_shop_second_hand_only_new
    NEW_AND_SECOND_HAND -> Res.string.quest_bicycle_shop_second_hand_new_and_used
    ONLY_SECOND_HAND -> Res.string.quest_bicycle_shop_second_hand_only_used
    NO_BICYCLES_SOLD -> Res.string.quest_bicycle_shop_second_hand_no_bicycles
}
