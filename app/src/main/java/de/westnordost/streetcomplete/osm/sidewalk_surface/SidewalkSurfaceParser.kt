package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.osm.expandSidesTags
import de.westnordost.streetcomplete.osm.surface.ParsedSurfaceWithNote
import de.westnordost.streetcomplete.osm.surface.parseSingleSurfaceTag

fun createSidewalkSurface(tags: Map<String, String>): LeftAndRightParsedSidewalkSurface? {
    val expandedTags = expandRelevantSidesTags(tags)

    val leftNote = expandedTags["sidewalk:left:surface:note"]
    val rightNote = expandedTags["sidewalk:right:surface:note"]

    val left = parseSingleSurfaceTag(expandedTags["sidewalk:left:surface"], leftNote)
    val right = parseSingleSurfaceTag(expandedTags["sidewalk:right:surface"], rightNote)
    if (left == null && right == null) {
        return null
    }
    return LeftAndRightParsedSidewalkSurface(ParsedSurfaceWithNote(left, leftNote), ParsedSurfaceWithNote(right, rightNote))
}

private fun expandRelevantSidesTags(tags: Map<String, String>): Map<String, String> {
    val result = tags.toMutableMap()
    result.expandSidesTags("sidewalk", "surface", true)
    result.expandSidesTags("sidewalk", "surface:note", true)
    return result
}
