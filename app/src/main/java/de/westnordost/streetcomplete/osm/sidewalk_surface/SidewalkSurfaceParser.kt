package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.osm.expandSidesTags
import de.westnordost.streetcomplete.osm.surface.parseSurfaceAndNote

fun parseSidewalkSurface(tags: Map<String, String>): LeftAndRightSidewalkSurface? {
    val expandedTags = expandRelevantSidesTags(tags)

    val left = parseSurfaceAndNote(expandedTags, "sidewalk:left")
    val right = parseSurfaceAndNote(expandedTags, "sidewalk:right")

    if (left == null && right == null) return null

    return LeftAndRightSidewalkSurface(left, right)
}

private fun expandRelevantSidesTags(tags: Map<String, String>): Map<String, String> {
    val result = tags.toMutableMap()
    result.expandSidesTags("sidewalk", "surface", true)
    result.expandSidesTags("sidewalk", "surface:note", true)
    return result
}
