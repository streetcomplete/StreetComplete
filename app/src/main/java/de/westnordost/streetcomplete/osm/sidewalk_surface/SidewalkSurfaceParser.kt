package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.osm.expandSidesTags
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote

fun createSidewalkSurface(tags: Map<String, String>): LeftAndRightSidewalkSurface? {
    val expandedTags = expandRelevantSidesTags(tags)

    val left = SURFACE_MAP[expandedTags["sidewalk:left:surface"]]
    val right = SURFACE_MAP[expandedTags["sidewalk:right:surface"]]
    if (left == null && right == null) return null

    val leftNote = expandedTags["sidewalk:left:surface:note"]
    val rightNote = expandedTags["sidewalk:right:surface:note"]

    return LeftAndRightSidewalkSurface(
        left?.let { SurfaceAndNote(it, leftNote) },
        right?.let { SurfaceAndNote(it, rightNote) }
    )
}

private val SURFACE_MAP: Map<String, Surface> = Surface.values().associateBy { it.osmValue }

private fun expandRelevantSidesTags(tags: Map<String, String>): Map<String, String> {
    val result = tags.toMutableMap()
    result.expandSidesTags("sidewalk", "surface", true)
    result.expandSidesTags("sidewalk", "surface:note", true)
    return result
}
