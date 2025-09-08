package de.westnordost.streetcomplete.quests.recycling_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.*
import de.westnordost.streetcomplete.view.image_select.Item

fun RecyclingMaterial.asItem(): Item<List<RecyclingMaterial>> =
    Item(listOf(this), iconResId, titleResId)

fun List<RecyclingMaterial>.asItem(): Item<List<RecyclingMaterial>> =
    Item(this, iconResId, titleResId)

private val RecyclingMaterial.iconResId: Int get() = when (this) {
    GLASS_BOTTLES ->     R.drawable.ic_recycling_glass_bottles
    GLASS ->             R.drawable.ic_recycling_glass
    PAPER ->             R.drawable.ic_recycling_paper
    PLASTIC ->           R.drawable.ic_recycling_plastic
    PLASTIC_PACKAGING -> R.drawable.ic_recycling_plastic_packaging
    PLASTIC_BOTTLES ->   R.drawable.ic_recycling_plastic_bottles
    PET ->               R.drawable.ic_recycling_pet
    BEVERAGE_CARTONS ->  R.drawable.ic_recycling_beverage_cartons
    CANS ->              R.drawable.ic_recycling_cans
    SCRAP_METAL ->       R.drawable.ic_recycling_scrap_metal
    CLOTHES ->           R.drawable.ic_recycling_clothes
    SHOES ->             R.drawable.ic_recycling_shoes
    SMALL_ELECTRICAL_APPLIANCES -> R.drawable.ic_recycling_small_electrical_appliances
    BATTERIES ->         R.drawable.ic_recycling_batteries
    GREEN_WASTE ->       R.drawable.ic_recycling_green_waste
    FOOD_WASTE ->        R.drawable.ic_recycling_food_waste
    COOKING_OIL ->       R.drawable.ic_recycling_cooking_oil
    ENGINE_OIL ->        R.drawable.ic_recycling_engine_oil
}

private val RecyclingMaterial.titleResId: Int get() = when (this) {
    GLASS_BOTTLES ->     R.string.quest_recycling_type_glass_bottles
    GLASS ->             R.string.quest_recycling_type_any_glass
    PAPER ->             R.string.quest_recycling_type_paper
    PLASTIC ->           R.string.quest_recycling_type_plastic_generic
    PLASTIC_PACKAGING -> R.string.quest_recycling_type_plastic_packaging
    PET ->               R.string.quest_recycling_type_pet
    PLASTIC_BOTTLES ->   R.string.quest_recycling_type_plastic_bottles
    BEVERAGE_CARTONS ->  R.string.quest_recycling_type_beverage_cartons
    CANS ->              R.string.quest_recycling_type_cans
    SCRAP_METAL ->       R.string.quest_recycling_type_scrap_metal
    CLOTHES ->           R.string.quest_recycling_type_clothes
    SHOES ->             R.string.quest_recycling_type_shoes
    SMALL_ELECTRICAL_APPLIANCES -> R.string.quest_recycling_type_electric_appliances
    BATTERIES ->         R.string.quest_recycling_type_batteries
    GREEN_WASTE ->       R.string.quest_recycling_type_green_waste
    FOOD_WASTE ->        R.string.quest_recycling_type_food_waste
    COOKING_OIL ->       R.string.quest_recycling_type_cooking_oil
    ENGINE_OIL ->        R.string.quest_recycling_type_engine_oil
}

private val List<RecyclingMaterial>.iconResId: Int get() = when (this) {
    listOf(PLASTIC_BOTTLES, BEVERAGE_CARTONS) -> R.drawable.ic_recycling_plastic_bottles_and_cartons
    else -> first().iconResId
}

private val List<RecyclingMaterial>.titleResId: Int get() = when (this) {
    listOf(PLASTIC) -> R.string.quest_recycling_type_plastic
    listOf(PLASTIC_BOTTLES, BEVERAGE_CARTONS) -> R.string.quest_recycling_type_plastic_bottles_and_cartons
    else -> first().titleResId
}
