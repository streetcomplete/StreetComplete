package de.westnordost.streetcomplete.data.osm.splitway

import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.util.measuredLength
import de.westnordost.streetcomplete.util.pointOnPolylineFromStart

/** Contains information about at which position to split a way. */
sealed class SplitPolylineAtPosition {
    abstract val pos: LatLon
}

/** When intending to split a way at a node, indicates the precise position of that node */
data class SplitAtPoint(override val pos: OsmLatLon) : SplitPolylineAtPosition()

/** When intending to split a way at a position between two nodes, indicates the precise position
 *  of these two nodes  */
data class SplitAtLinePosition(val pos1: OsmLatLon, val pos2: OsmLatLon, val delta: Double) : SplitPolylineAtPosition() {
    override val pos: LatLon get() {
        val line = listOf(pos1, pos2)
        return line.pointOnPolylineFromStart(line.measuredLength() * delta)!!
    }
    init {
        if(delta <= 0 || delta >= 1)
            throw IllegalArgumentException("Delta must be between 0 and 1 (both exclusive)")
    }
}
