package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote

fun createSidewalkSurface(tags: Map<String, String>): LeftAndRightSidewalkSurface? {
    val surfaceStr = tags["sidewalk:surface"]
    val surfaceBothStr = tags["sidewalk:both:surface"]
    val surfaceLeftStr = tags["sidewalk:left:surface"] ?: surfaceBothStr ?: surfaceStr
    val surfaceRightStr = tags["sidewalk:right:surface"] ?: surfaceBothStr ?: surfaceStr
    val surfaceLeft = surfaceMap[surfaceLeftStr]
    val surfaceRight = surfaceMap[surfaceRightStr]
    if (surfaceLeft == null && surfaceRight == null) return null
    val note = tags["sidewalk:surface:note"]
    val bothNote = tags["sidewalk:both:surface:note"]
    val leftNote = tags["sidewalk:left:surface:note"] ?: bothNote ?: note
    val rightNote = tags["sidewalk:right:surface:note"] ?: bothNote ?: note
    return LeftAndRightSidewalkSurface(
        surfaceLeft?.let { SurfaceAndNote(it, leftNote) },
        surfaceRight?.let { SurfaceAndNote(it, rightNote) }
    )
}

private val surfaceMap: Map<String, Surface> = Surface.values().associateBy { it.osmValue }
