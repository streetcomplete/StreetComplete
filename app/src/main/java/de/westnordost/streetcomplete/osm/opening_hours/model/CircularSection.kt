package de.westnordost.streetcomplete.osm.opening_hours.model

import kotlinx.serialization.Serializable

/** An integer range that defines a section in a circle. The range that is defined is actually
 * closed-open: [start,end+1). i.e Jun-Jul (= start:6 end:7) shall be both June and July. If start
 * is bigger than end, it means that the section crosses the upper boundary. Think degrees.
 */
@Serializable
data class CircularSection(val start: Int, val end: Int) : Comparable<CircularSection> {

    fun intersects(other: CircularSection): Boolean =
        loops && other.loops ||
        if (loops || other.loops) {
            other.end >= start || other.start <= end
        } else {
            other.end >= start && other.start <= end
        }

    val loops get() = end < start

    override fun compareTo(other: CircularSection): Int {
        // loopers come first,
        if (loops && !other.loops) return -1
        if (!loops && other.loops) return +1
        // then by start
        val result = start - other.start
        if (result != 0) return result
        // then by end
        return end - other.end
    }

    override fun toString() = "$start-$end"
}
