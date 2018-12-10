package de.westnordost.streetcomplete.quests.oneway

import de.westnordost.osmapi.map.data.LatLon

class TrafficFlowSegment(val fromPosition: LatLon, val toPosition: LatLon) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as TrafficFlowSegment?
        return fromPosition == that!!.fromPosition && toPosition == that.toPosition
    }

    override fun hashCode(): Int {
        var result = fromPosition.hashCode()
        result = 31 * result + toPosition.hashCode()
        return result
    }
}
