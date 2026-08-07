package de.westnordost.streetcomplete.util.math

import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitAtLinePosition
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitAtPoint
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitPolylineAtPosition
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.allExceptFirstAndLast
import kotlin.math.min

/**
 * Creates a SplitPolylineAtPosition near [position] for this polyline at the given [maxDistance]
 * and optionally with snapping to vertices at given [snapToVertexDistance].
 * */
fun List<LatLon>.getSplitAt(
    position: LatLon,
    maxDistance: Double,
    snapToVertexDistance: Double = 0.0
): SplitPolylineAtPosition? {
    if (snapToVertexDistance > 0.0) {
        // don't snap to first or last vertex. Can't split the way there.
        val nearestVertex = allExceptFirstAndLast().minBy { position.distanceTo(it) }
        if (position.distanceTo(nearestVertex) <= min(maxDistance, snapToVertexDistance)) {
            return SplitAtPoint(nearestVertex)
        }
    }
    val (start, end) = position.nearestArcOf(this)
    if (position.distanceToArc(start, end) <= maxDistance) {
        val delta = position.alongTrackDistanceTo(start, end)
        return SplitAtLinePosition(start, end, delta)
    }
    return null
}
