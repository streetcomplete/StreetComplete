package de.westnordost.streetcomplete.quests.drinking_water_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.DISUSED_DRINKING_WATER
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.HAND_PUMP
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.SPRING
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.WATER_FOUNTAIN_BOTTLE_REFILL_ONLY
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.WATER_FOUNTAIN_GENERIC
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.WATER_FOUNTAIN_JET
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.WATER_TAP
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.WATER_WELL_WITHOUT_PUMP
import de.westnordost.streetcomplete.view.image_select.Item

fun DrinkingWaterType.asItem() = Item(this, iconResId, titleResId)

private val DrinkingWaterType.titleResId: Int get() = when (this) {
    WATER_FOUNTAIN_GENERIC -> R.string.quest_drinking_water_type_generic_water_fountain
    WATER_FOUNTAIN_JET -> R.string.quest_drinking_water_type_jet_water_fountain
    WATER_FOUNTAIN_BOTTLE_REFILL_ONLY -> R.string.quest_drinking_water_type_bottle_refill_only_fountain
    WATER_TAP -> R.string.quest_drinking_water_type_tap
    HAND_PUMP -> R.string.quest_drinking_water_type_hand_pump
    WATER_WELL_WITHOUT_PUMP -> R.string.quest_drinking_water_type_water_well_no_pump
    SPRING -> R.string.quest_drinking_water_type_spring
    DISUSED_DRINKING_WATER -> R.string.quest_drinking_water_type_disused
}

private val DrinkingWaterType.iconResId: Int get() = when (this) {
    WATER_FOUNTAIN_GENERIC -> R.drawable.drinking_water_type_generic_water_fountain
    WATER_FOUNTAIN_JET -> R.drawable.drinking_water_type_jet_water_fountain
    WATER_FOUNTAIN_BOTTLE_REFILL_ONLY -> R.drawable.drinking_water_type_bottle_refill_only_fountain
    WATER_TAP -> R.drawable.drinking_water_type_tap
    HAND_PUMP -> R.drawable.drinking_water_type_hand_pump
    WATER_WELL_WITHOUT_PUMP -> R.drawable.drinking_water_type_water_well_no_pump
    SPRING -> R.drawable.drinking_water_type_spring
    DISUSED_DRINKING_WATER -> R.drawable.drinking_water_type_disused
}
