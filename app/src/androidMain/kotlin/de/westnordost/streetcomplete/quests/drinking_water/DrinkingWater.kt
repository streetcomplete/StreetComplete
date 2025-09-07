package de.westnordost.streetcomplete.quests.drinking_water

import de.westnordost.streetcomplete.quests.drinking_water.DrinkingWater.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_drinking_water_not_potable_signed
import de.westnordost.streetcomplete.resources.quest_drinking_water_potable_signed
import de.westnordost.streetcomplete.resources.quest_drinking_water_potable_unsigned2
import org.jetbrains.compose.resources.StringResource

enum class DrinkingWater {
    POTABLE_SIGNED,
    NOT_POTABLE_SIGNED,
    UNSIGNED,
}

val DrinkingWater.text: StringResource get() = when (this) {
    POTABLE_SIGNED -> Res.string.quest_drinking_water_potable_signed
    NOT_POTABLE_SIGNED -> Res.string.quest_drinking_water_not_potable_signed
    UNSIGNED -> Res.string.quest_drinking_water_potable_unsigned2
}
