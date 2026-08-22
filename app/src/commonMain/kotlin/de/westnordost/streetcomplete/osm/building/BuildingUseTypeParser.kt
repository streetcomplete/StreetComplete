package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.building.BuildingType.*
fun createBuildingUseType(tags: Map<String,String>): BuildingType? {
    // Excluded building types that shouldn't belong with building:use
    val excluded = listOf(RUINS, ABANDONED, HISTORIC)
    val buildingUseValue = tags["building:use"] ?: return null
    val buildingUse = BuildingType.entries.find { it.osmValue == buildingUseValue && it !in excluded }
        ?: BuildingType.aliases.entries.find { it.key.second == buildingUseValue && it.value !in excluded }?.value

    if (buildingUse == HOUSE) return createBuildingTypeFromHouseType(tags["house"])

    return buildingUse
}
