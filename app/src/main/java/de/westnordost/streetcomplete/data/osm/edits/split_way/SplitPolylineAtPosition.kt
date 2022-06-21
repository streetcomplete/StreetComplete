package de.westnordost.streetcomplete.data.osm.edits.split_way

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.measuredLength
import de.westnordost.streetcomplete.util.math.pointOnPolylineFromStart
import kotlinx.serialization.Serializable

/** Contains information about at which position to split a way. */
@Serializable
sealed class SplitPolylineAtPosition {
    abstract val pos: LatLon
}

/** When intending to split a way at a node, indicates the precise position of that node */
@Serializable
data class SplitAtPoint(override val pos: LatLon) : SplitPolylineAtPosition()

/** When intending to split a way at a position between two nodes, indicates the precise position
 *  of these two nodes  */
@Serializable
data class SplitAtLinePosition(val pos1: LatLon, val pos2: LatLon, val delta: Double) : SplitPolylineAtPosition() {
    override val pos: LatLon get() {
        val line = listOf(pos1, pos2)
        return line.pointOnPolylineFromStart(line.measuredLength() * delta)!!
    }
    init {
        if (delta <= 0 || delta >= 1) {
            throw IllegalArgumentException("Delta must be between 0 and 1 (both exclusive)")
        }
    }
}
