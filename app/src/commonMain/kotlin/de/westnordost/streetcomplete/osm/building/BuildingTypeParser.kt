package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.building.BuildingType.*
import kotlin.collections.contains

fun createBuildingType(tags: Map<String, String>): BuildingType? {
    val building =
        BuildingType.entries.find { it.osmKey != null && tags[it.osmKey] == it.osmValue }
        ?: BuildingType.aliases.entries.find { tags[it.key.first] == it.key.second }?.value

    if (building == BuildingType.HOUSE) return createBuildingTypeFromHouseType(tags["house"])

    if (building != null) return building

    val buildingValue = tags["building"]

    // treat unspecified building value as unspecified...
    if (buildingValue == "yes") return null

    // treat deprecated building value as if it is unspecified
    if (buildingValue in INVALID_BUILDING_TYPES) return null

    // no building and no man_made value at all
    if (buildingValue == null && tags["man_made"] == null) return null

    // otherwise, there is some value but we do not support it
    return BuildingType.UNSUPPORTED
}

fun createBuildingUseType(tags: Map<String,String>): BuildingType? {
    // Excluded building types that shouldn't belong with building:use (subject to future changes)
    val excluded = listOf(RUINS, ABANDONED, HISTORIC)

    val buildingUseValue = tags["building:use"] ?: return null
    val buildingUse =
        BuildingType.entries.find { it.osmKey == "building" && it.osmValue == buildingUseValue && it !in excluded }
        ?: BuildingType.aliases.entries.find { it.key.first == "building" && it.key.second == buildingUseValue && it.value !in excluded }?.value

    if (buildingUse == HOUSE) return createBuildingTypeFromHouseType(tags["house"])

    // treat deprecated building value as if it is unspecified
    if (buildingUseValue in INVALID_BUILDING_TYPES) return null

    return buildingUse
}


fun createBuildingTypeFromHouseType(houseType: String?): BuildingType = when (houseType) {
    null ->             BuildingType.HOUSE
    "detached" ->       BuildingType.DETACHED
    "terrace" ->        BuildingType.TERRACE
    "semi-detached" ->  BuildingType.SEMI_DETACHED
    "bungalow" ->       BuildingType.BUNGALOW

    else ->             BuildingType.HOUSE
}
