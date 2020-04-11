package de.westnordost.streetcomplete.data.osm.changes

import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.util.measuredLength
import de.westnordost.streetcomplete.util.pointOnPolylineFromStart

sealed class SplitPolylineAtPosition {
    abstract val pos: LatLon
}

data class SplitAtPoint(override val pos: OsmLatLon) : SplitPolylineAtPosition()

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
