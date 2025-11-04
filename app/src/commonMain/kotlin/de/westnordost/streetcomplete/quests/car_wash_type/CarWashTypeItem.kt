package de.westnordost.streetcomplete.quests.car_wash_type

import de.westnordost.streetcomplete.quests.car_wash_type.CarWashType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.car_wash_automated
import de.westnordost.streetcomplete.resources.car_wash_self_service
import de.westnordost.streetcomplete.resources.car_wash_service
import de.westnordost.streetcomplete.resources.quest_carWashType_automated
import de.westnordost.streetcomplete.resources.quest_carWashType_selfService
import de.westnordost.streetcomplete.resources.quest_carWashType_service
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val CarWashType.title: StringResource get() = when (this) {
    AUTOMATED ->    Res.string.quest_carWashType_automated
    SELF_SERVICE -> Res.string.quest_carWashType_selfService
    SERVICE ->      Res.string.quest_carWashType_service
}

val CarWashType.icon: DrawableResource get() = when (this) {
    AUTOMATED ->    Res.drawable.car_wash_automated
    SELF_SERVICE -> Res.drawable.car_wash_self_service
    SERVICE ->      Res.drawable.car_wash_service
}
