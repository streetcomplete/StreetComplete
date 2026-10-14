package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.expandSidesTags
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides
import de.westnordost.streetcomplete.osm.surface.parseSurface

fun parseSidewalksSurface(tags: Map<String, String>): SidewalkSurface? {
    val expandedTags = tags.toMutableMap()
    expandedTags.expandSidesTags("sidewalk", "surface", true)

    val sidewalkSides = parseSidewalkSides(tags)

    // A side without a sidewalk of its own, on this way, must not carry sidewalk:surface tags
    val left = if (sidewalkSides?.left != Sidewalk.YES) null
    else parseSurface(expandedTags["sidewalk:left:surface"])

    val right = if (sidewalkSides?.right != Sidewalk.YES) null
    else parseSurface(expandedTags["sidewalk:right:surface"])

    if (left == null && right == null) return null

    return SidewalkSurface(Sides(left, right))
}
