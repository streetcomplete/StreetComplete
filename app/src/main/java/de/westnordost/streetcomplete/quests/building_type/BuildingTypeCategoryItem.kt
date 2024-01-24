package de.westnordost.streetcomplete.quests.building_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.building_type.BuildingTypeCategory.CIVIC
import de.westnordost.streetcomplete.quests.building_type.BuildingTypeCategory.COMMERCIAL
import de.westnordost.streetcomplete.quests.building_type.BuildingTypeCategory.FOR_CARS
import de.westnordost.streetcomplete.quests.building_type.BuildingTypeCategory.FOR_FARMS
import de.westnordost.streetcomplete.quests.building_type.BuildingTypeCategory.OTHER
import de.westnordost.streetcomplete.quests.building_type.BuildingTypeCategory.RELIGIOUS
import de.westnordost.streetcomplete.quests.building_type.BuildingTypeCategory.RESIDENTIAL
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

fun Iterable<BuildingTypeCategory>.toItems() = map { it.asItem() }

fun BuildingTypeCategory.asItem(): GroupableDisplayItem<BuildingType> =
    Item(type, iconResId, titleResId, descriptionResId, subTypes.toItems())

private val BuildingTypeCategory.titleResId: Int get() = when (this) {
    RESIDENTIAL -> R.string.quest_buildingType_residential
    COMMERCIAL ->  R.string.quest_buildingType_commercial
    CIVIC ->       R.string.quest_buildingType_civic
    RELIGIOUS ->   R.string.quest_buildingType_religious
    FOR_CARS ->    R.string.quest_buildingType_cars
    FOR_FARMS ->   R.string.quest_buildingType_farm
    OTHER ->       R.string.quest_buildingType_other
}

private val BuildingTypeCategory.descriptionResId: Int? get() = when (this) {
    RESIDENTIAL -> R.string.quest_buildingType_residential_description
    COMMERCIAL ->  R.string.quest_buildingType_commercial_generic_description
    CIVIC ->       R.string.quest_buildingType_civic_description
    else ->        null
}

private val BuildingTypeCategory.iconResId: Int get() = when (this) {
    RESIDENTIAL -> R.drawable.ic_building_apartments
    COMMERCIAL ->  R.drawable.ic_building_office
    CIVIC ->       R.drawable.ic_building_civic
    RELIGIOUS ->   R.drawable.ic_building_temple
    FOR_CARS ->    R.drawable.ic_building_car
    FOR_FARMS ->   R.drawable.ic_building_farm
    OTHER ->       R.drawable.ic_building_other
}
