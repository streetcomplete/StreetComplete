package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.building.BuildingTypeCategory.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.building_apartments
import de.westnordost.streetcomplete.resources.building_civic
import de.westnordost.streetcomplete.resources.building_farm
import de.westnordost.streetcomplete.resources.building_office
import de.westnordost.streetcomplete.resources.building_other
import de.westnordost.streetcomplete.resources.building_shed
import de.westnordost.streetcomplete.resources.building_temple
import de.westnordost.streetcomplete.resources.quest_buildingType_civic
import de.westnordost.streetcomplete.resources.quest_buildingType_civic_description
import de.westnordost.streetcomplete.resources.quest_buildingType_commercial
import de.westnordost.streetcomplete.resources.quest_buildingType_commercial_generic_description
import de.westnordost.streetcomplete.resources.quest_buildingType_farm
import de.westnordost.streetcomplete.resources.quest_buildingType_other
import de.westnordost.streetcomplete.resources.quest_buildingType_outbuilding
import de.westnordost.streetcomplete.resources.quest_buildingType_outbuilding_description
import de.westnordost.streetcomplete.resources.quest_buildingType_religious
import de.westnordost.streetcomplete.resources.quest_buildingType_residential
import de.westnordost.streetcomplete.resources.quest_buildingType_residential_description
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val BuildingTypeCategory.title: StringResource get() = when (this) {
    RESIDENTIAL ->  Res.string.quest_buildingType_residential
    COMMERCIAL ->   Res.string.quest_buildingType_commercial
    CIVIC ->        Res.string.quest_buildingType_civic
    RELIGIOUS ->    Res.string.quest_buildingType_religious
    OUTBUILDING ->  Res.string.quest_buildingType_outbuilding
    FOR_FARMS ->    Res.string.quest_buildingType_farm
    OTHER ->        Res.string.quest_buildingType_other
}

val BuildingTypeCategory.description: StringResource? get() = when (this) {
    RESIDENTIAL -> Res.string.quest_buildingType_residential_description
    COMMERCIAL ->  Res.string.quest_buildingType_commercial_generic_description
    CIVIC ->       Res.string.quest_buildingType_civic_description
    OUTBUILDING -> Res.string.quest_buildingType_outbuilding_description
    else ->        null
}

val BuildingTypeCategory.icon: DrawableResource get() = when (this) {
    RESIDENTIAL ->  Res.drawable.building_apartments
    COMMERCIAL ->   Res.drawable.building_office
    CIVIC ->        Res.drawable.building_civic
    RELIGIOUS ->    Res.drawable.building_temple
    OUTBUILDING ->  Res.drawable.building_shed
    FOR_FARMS ->    Res.drawable.building_farm
    OTHER ->        Res.drawable.building_other
}
