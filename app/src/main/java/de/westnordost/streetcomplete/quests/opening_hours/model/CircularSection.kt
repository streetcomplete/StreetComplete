package de.westnordost.streetcomplete.quests.opening_hours.model

/** An integer range that defines a section in a circle. The range that is defined is actually
 * closed-open: [start,end+1). i.e Jun-Jul (= start:6 end:7) shall be both June and July. If start
 * is bigger than end, it means that the section crosses the upper boundary. Think degrees.
 */
open class CircularSection(val start: Int, val end: Int) : Comparable<CircularSection> {

    open fun intersects(other: CircularSection) =
        loops && other.loops ||
        if (loops || other.loops) other.end >= start || other.start <= end
        else                      other.end >= start && other.start <= end

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

    fun toStringUsing(names: Array<String>, range: String): String {
        val sb = StringBuilder()
        sb.append(names[start])
        if (start != end) {
            sb.append(range)
            sb.append(names[end])
        }
        return sb.toString()
    }

    override fun toString() = "$start-$end"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CircularSection) return false
        return start == other.start && end == other.end
    }

    override fun hashCode() = 31 * start + end
}
