package de.westnordost.streetcomplete.osm.building

fun createBuildingType(tags: Map<String, String>): BuildingType? {
    val building = BuildingType.values().find { tags[it.osmKey] == it.osmValue }

    // TODO handle synonyms etc.

    return building
}

