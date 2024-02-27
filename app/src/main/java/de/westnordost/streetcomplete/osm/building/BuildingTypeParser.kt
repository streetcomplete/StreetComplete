package de.westnordost.streetcomplete.osm.building

fun createBuildingType(tags: Map<String, String>): BuildingType? {
    val building = BuildingType.entries.find { it.osmKey != null && tags[it.osmKey] == it.osmValue }
        ?: BuildingType.aliases.entries.find { tags[it.key.first] == it.key.second }?.value

    if (building == BuildingType.HOUSE) return createBuildingTypeFromHouseType(tags["house"])

    if (building != null) return building

    val buildingValue = tags["building"]

    // treat unspecified building value as unspecified...
    if (buildingValue == "yes") return null

    // treat deprecated building value as if it is unspecified
    if (buildingValue in BuildingType.deprecatedValues) return null

    // ah yes, those two clowns...
    if (buildingValue == "no" || buildingValue == "entrance") return null

    // no building and no man_made value at all
    if (buildingValue == null && tags["man_made"] == null) return null

    // otherwise, there is some value but we do not support it
    return BuildingType.UNSUPPORTED
}

private fun createBuildingTypeFromHouseType(houseType: String?): BuildingType = when (houseType) {
    null ->             BuildingType.HOUSE
    "detached" ->       BuildingType.DETACHED
    "terrace" ->        BuildingType.TERRACE
    "semi-detached" ->  BuildingType.SEMI_DETACHED
    "bungalow" ->       BuildingType.BUNGALOW

    else ->             BuildingType.HOUSE
}
