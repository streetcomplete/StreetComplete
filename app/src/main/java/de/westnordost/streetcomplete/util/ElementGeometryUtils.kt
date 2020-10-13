package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry

fun ElementPolylinesGeometry.getOrientationAtCenterLineInDegrees(): Float {
    val centerLine = polylines.first().centerLineOfPolyline()
    return centerLine.first.initialBearingTo(centerLine.second).toFloat()
}