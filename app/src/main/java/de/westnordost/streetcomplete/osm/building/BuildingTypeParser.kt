package de.westnordost.streetcomplete.osm.building

fun createBuildingType(tags: Map<String, String>): BuildingType? {
    val building = BuildingType.entries.find { it.osmKey != null && tags[it.osmKey] == it.osmValue }
        ?: BuildingType.aliases.entries.find { tags[it.key.first] == it.key.second }?.value

    if (building == null) {
        val buildingValue = tags["building"]
        // treat deprecated building value as if it is unspecified
        if (buildingValue in BuildingType.deprecatedValues) return null

        // treat unspecified building value as unspecified...
        if (buildingValue == "yes") return null

        // ah yes, those two clowns...
        if (buildingValue == "no" || buildingValue == "entrance") return null

        // otherwise, there is some value but we do not support it
        return BuildingType.UNSUPPORTED
    }

    return building
}

// TODO TESTS

// TODO understand building=house + house=X and variants
