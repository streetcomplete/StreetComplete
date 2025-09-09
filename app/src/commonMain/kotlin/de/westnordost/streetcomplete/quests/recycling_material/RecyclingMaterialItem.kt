package de.westnordost.streetcomplete.quests.recycling_material

import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_recycling_type_any_glass
import de.westnordost.streetcomplete.resources.quest_recycling_type_batteries
import de.westnordost.streetcomplete.resources.quest_recycling_type_beverage_cartons
import de.westnordost.streetcomplete.resources.quest_recycling_type_cans
import de.westnordost.streetcomplete.resources.quest_recycling_type_clothes
import de.westnordost.streetcomplete.resources.quest_recycling_type_cooking_oil
import de.westnordost.streetcomplete.resources.quest_recycling_type_electric_appliances
import de.westnordost.streetcomplete.resources.quest_recycling_type_engine_oil
import de.westnordost.streetcomplete.resources.quest_recycling_type_food_waste
import de.westnordost.streetcomplete.resources.quest_recycling_type_glass_bottles
import de.westnordost.streetcomplete.resources.quest_recycling_type_green_waste
import de.westnordost.streetcomplete.resources.quest_recycling_type_paper
import de.westnordost.streetcomplete.resources.quest_recycling_type_pet
import de.westnordost.streetcomplete.resources.quest_recycling_type_plastic
import de.westnordost.streetcomplete.resources.quest_recycling_type_plastic_bottles
import de.westnordost.streetcomplete.resources.quest_recycling_type_plastic_bottles_and_cartons
import de.westnordost.streetcomplete.resources.quest_recycling_type_plastic_generic
import de.westnordost.streetcomplete.resources.quest_recycling_type_plastic_packaging
import de.westnordost.streetcomplete.resources.quest_recycling_type_scrap_metal
import de.westnordost.streetcomplete.resources.quest_recycling_type_shoes
import de.westnordost.streetcomplete.resources.recycling_batteries
import de.westnordost.streetcomplete.resources.recycling_beverage_cartons
import de.westnordost.streetcomplete.resources.recycling_cans
import de.westnordost.streetcomplete.resources.recycling_clothes
import de.westnordost.streetcomplete.resources.recycling_cooking_oil
import de.westnordost.streetcomplete.resources.recycling_engine_oil
import de.westnordost.streetcomplete.resources.recycling_food_waste
import de.westnordost.streetcomplete.resources.recycling_glass
import de.westnordost.streetcomplete.resources.recycling_glass_bottles
import de.westnordost.streetcomplete.resources.recycling_green_waste
import de.westnordost.streetcomplete.resources.recycling_paper
import de.westnordost.streetcomplete.resources.recycling_pet
import de.westnordost.streetcomplete.resources.recycling_plastic
import de.westnordost.streetcomplete.resources.recycling_plastic_bottles
import de.westnordost.streetcomplete.resources.recycling_plastic_bottles_and_cartons
import de.westnordost.streetcomplete.resources.recycling_plastic_packaging
import de.westnordost.streetcomplete.resources.recycling_scrap_metal
import de.westnordost.streetcomplete.resources.recycling_shoes
import de.westnordost.streetcomplete.resources.recycling_small_electrical_appliances
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val RecyclingMaterial.icon: DrawableResource get() = when (this) {
    GLASS_BOTTLES ->     Res.drawable.recycling_glass_bottles
    GLASS ->             Res.drawable.recycling_glass
    PAPER ->             Res.drawable.recycling_paper
    PLASTIC ->           Res.drawable.recycling_plastic
    PLASTIC_PACKAGING -> Res.drawable.recycling_plastic_packaging
    PLASTIC_BOTTLES ->   Res.drawable.recycling_plastic_bottles
    PET ->               Res.drawable.recycling_pet
    BEVERAGE_CARTONS ->  Res.drawable.recycling_beverage_cartons
    CANS ->              Res.drawable.recycling_cans
    SCRAP_METAL ->       Res.drawable.recycling_scrap_metal
    CLOTHES ->           Res.drawable.recycling_clothes
    SHOES ->             Res.drawable.recycling_shoes
    SMALL_ELECTRICAL_APPLIANCES -> Res.drawable.recycling_small_electrical_appliances
    BATTERIES ->         Res.drawable.recycling_batteries
    GREEN_WASTE ->       Res.drawable.recycling_green_waste
    FOOD_WASTE ->        Res.drawable.recycling_food_waste
    COOKING_OIL ->       Res.drawable.recycling_cooking_oil
    ENGINE_OIL ->        Res.drawable.recycling_engine_oil
}

val RecyclingMaterial.title: StringResource get() = when (this) {
    GLASS_BOTTLES ->     Res.string.quest_recycling_type_glass_bottles
    GLASS ->             Res.string.quest_recycling_type_any_glass
    PAPER ->             Res.string.quest_recycling_type_paper
    PLASTIC ->           Res.string.quest_recycling_type_plastic_generic
    PLASTIC_PACKAGING -> Res.string.quest_recycling_type_plastic_packaging
    PET ->               Res.string.quest_recycling_type_pet
    PLASTIC_BOTTLES ->   Res.string.quest_recycling_type_plastic_bottles
    BEVERAGE_CARTONS ->  Res.string.quest_recycling_type_beverage_cartons
    CANS ->              Res.string.quest_recycling_type_cans
    SCRAP_METAL ->       Res.string.quest_recycling_type_scrap_metal
    CLOTHES ->           Res.string.quest_recycling_type_clothes
    SHOES ->             Res.string.quest_recycling_type_shoes
    SMALL_ELECTRICAL_APPLIANCES -> Res.string.quest_recycling_type_electric_appliances
    BATTERIES ->         Res.string.quest_recycling_type_batteries
    GREEN_WASTE ->       Res.string.quest_recycling_type_green_waste
    FOOD_WASTE ->        Res.string.quest_recycling_type_food_waste
    COOKING_OIL ->       Res.string.quest_recycling_type_cooking_oil
    ENGINE_OIL ->        Res.string.quest_recycling_type_engine_oil
}

val List<RecyclingMaterial>.icon: DrawableResource get() = when (this) {
    listOf(PLASTIC_BOTTLES, BEVERAGE_CARTONS) -> Res.drawable.recycling_plastic_bottles_and_cartons
    else -> first().icon
}

val List<RecyclingMaterial>.title: StringResource get() = when (this) {
    listOf(PLASTIC) -> Res.string.quest_recycling_type_plastic
    listOf(PLASTIC_BOTTLES, BEVERAGE_CARTONS) -> Res.string.quest_recycling_type_plastic_bottles_and_cartons
    else -> first().title
}
