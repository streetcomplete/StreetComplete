package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.building.BuildingType.*
import de.westnordost.streetcomplete.osm.building.BuildingLifecycle.*
import kotlin.collections.contains

fun createBuildingTypeAndStatus(tags: Map<String, String>): BuildingTypeAndStatus {
    // special case for building=construction + construction=actual_building_value
    if (tags["building"] == "construction") {
        val tagsWhenConstructionFinished = tags.toMutableMap().also {
            it["building"] = it["construction"] ?: "yes"
        }
        return BuildingTypeAndStatus(createBuildingType(tagsWhenConstructionFinished), CONSTRUCTION)
    }

    return BuildingTypeAndStatus(createBuildingType(tags), createBuildingStatus(tags))
}

private fun createBuildingType(tags: Map<String, String>, postfix: String = ""): BuildingType? {
    val building =
        BuildingType.entries.find {
            val value = tags[it.osmKey + postfix]
            value != null && value == it.osmValue
        }
        ?: BuildingType.aliases.entries.find { tags[it.key.first + postfix] == it.key.second }?.value

    if (building == HOUSE) {
        val house = createBuildingTypeFromHouseType(tags["house"])
        if (house != null) return house
    }

    if (building != null) return building

    val buildingValue = tags["building" + postfix]
    val manMadeValue = tags["man_made" + postfix]

    // treat unspecified building value as unspecified...
    if (buildingValue == "yes") return null

    // treat deprecated building value as if it is unspecified
    if (buildingValue in INVALID_BUILDING_TYPES) return null

    // no building and no man_made value at all
    if (buildingValue == null && manMadeValue == null) return null

    // otherwise, there is some value but we do not support it
    return UNSUPPORTED
}

private fun createBuildingStatus(tags: Map<String, String>): BuildingStatus? {
    for (lifecycle in BuildingLifecycle.entries) {
        val key = lifecycle.osmKey
        // e.g. abandoned = yes or abandoned:building = *
        if (tags[key] == "yes" || tags[key + ":building"] != null) {
            return lifecycle
        }
    }

    // e.g. building:use = apartments
    return createBuildingType(tags, ":use")
}

internal fun createBuildingTypeFromHouseType(houseType: String?): BuildingType? = when (houseType) {
    null ->             null
    "detached" ->       DETACHED
    "terrace" ->        TERRACE
    "terraced" ->       TERRACED_HOUSE
    "semi-detached" ->  SEMI_DETACHED
    "bungalow" ->       BUNGALOW
    else ->             UNSUPPORTED
}
