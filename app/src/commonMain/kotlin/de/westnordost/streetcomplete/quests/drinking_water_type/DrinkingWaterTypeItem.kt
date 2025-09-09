package de.westnordost.streetcomplete.quests.drinking_water_type

import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.drinking_water_type_bottle_refill_only_fountain
import de.westnordost.streetcomplete.resources.drinking_water_type_disused
import de.westnordost.streetcomplete.resources.drinking_water_type_generic_water_fountain
import de.westnordost.streetcomplete.resources.drinking_water_type_hand_pump
import de.westnordost.streetcomplete.resources.drinking_water_type_jet_water_fountain
import de.westnordost.streetcomplete.resources.drinking_water_type_spring
import de.westnordost.streetcomplete.resources.drinking_water_type_tap
import de.westnordost.streetcomplete.resources.drinking_water_type_water_well_no_pump
import de.westnordost.streetcomplete.resources.quest_drinking_water_type_bottle_refill_only_fountain
import de.westnordost.streetcomplete.resources.quest_drinking_water_type_disused
import de.westnordost.streetcomplete.resources.quest_drinking_water_type_generic_water_fountain
import de.westnordost.streetcomplete.resources.quest_drinking_water_type_hand_pump
import de.westnordost.streetcomplete.resources.quest_drinking_water_type_jet_water_fountain
import de.westnordost.streetcomplete.resources.quest_drinking_water_type_spring
import de.westnordost.streetcomplete.resources.quest_drinking_water_type_tap
import de.westnordost.streetcomplete.resources.quest_drinking_water_type_water_well_no_pump
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val DrinkingWaterType.title: StringResource get() = when (this) {
    WATER_FOUNTAIN_GENERIC -> Res.string.quest_drinking_water_type_generic_water_fountain
    WATER_FOUNTAIN_JET -> Res.string.quest_drinking_water_type_jet_water_fountain
    WATER_FOUNTAIN_BOTTLE_REFILL_ONLY -> Res.string.quest_drinking_water_type_bottle_refill_only_fountain
    WATER_TAP -> Res.string.quest_drinking_water_type_tap
    HAND_PUMP -> Res.string.quest_drinking_water_type_hand_pump
    WATER_WELL_WITHOUT_PUMP -> Res.string.quest_drinking_water_type_water_well_no_pump
    SPRING -> Res.string.quest_drinking_water_type_spring
    DISUSED_DRINKING_WATER -> Res.string.quest_drinking_water_type_disused
}

val DrinkingWaterType.icon: DrawableResource get() = when (this) {
    WATER_FOUNTAIN_GENERIC -> Res.drawable.drinking_water_type_generic_water_fountain
    WATER_FOUNTAIN_JET -> Res.drawable.drinking_water_type_jet_water_fountain
    WATER_FOUNTAIN_BOTTLE_REFILL_ONLY -> Res.drawable.drinking_water_type_bottle_refill_only_fountain
    WATER_TAP -> Res.drawable.drinking_water_type_tap
    HAND_PUMP -> Res.drawable.drinking_water_type_hand_pump
    WATER_WELL_WITHOUT_PUMP -> Res.drawable.drinking_water_type_water_well_no_pump
    SPRING -> Res.drawable.drinking_water_type_spring
    DISUSED_DRINKING_WATER -> Res.drawable.drinking_water_type_disused
}
