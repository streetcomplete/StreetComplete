package de.westnordost.streetcomplete.osm

fun estimateBuildingHeight(tags: Map<String, String>): Float? {
    val height = tags["height"]?.toFloat()
    if (height != null) return height

    val buildingLevels = tags["building:levels"]?.toIntOrNull() ?: return null
    val roofLevels = tags["roof:levels"]?.toIntOrNull() ?: 0

    return (buildingLevels * 3 + roofLevels * 2).toFloat()
}

fun estimateMinBuildingHeight(tags: Map<String, String>): Float? {
    val minHeight = tags["min_height"]?.toFloat()
    if (minHeight != null) return minHeight

    val minBuildingLevel = tags["building:min_level"]?.toIntOrNull() ?: return null
    return (minBuildingLevel * 3).toFloat()
}
