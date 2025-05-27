package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.osm.expandSidesTags
import de.westnordost.streetcomplete.osm.surface.parseSurface

fun parseSidewalkSurface(tags: Map<String, String>): LeftAndRightSidewalkSurface? {
    val expandedTags = tags.toMutableMap()
    expandedTags.expandSidesTags("sidewalk", "surface", true)

    val left = parseSurface(expandedTags["sidewalk:left:surface"])
    val right = parseSurface(expandedTags["sidewalk:right:surface"])

    if (left == null && right == null) return null

    return LeftAndRightSidewalkSurface(left, right)
}
