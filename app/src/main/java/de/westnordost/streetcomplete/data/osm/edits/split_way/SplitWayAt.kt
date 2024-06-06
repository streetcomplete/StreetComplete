package de.westnordost.streetcomplete.data.osm.edits.split_way

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.equalsInOsm
import de.westnordost.streetcomplete.util.math.measuredLength
import de.westnordost.streetcomplete.util.math.pointOnPolylineFromStart
import kotlin.math.sign

/** data class that carries the information for one split to perform on a random position on a way.
 *  So, same as SplitPolylineAtPosition, but additionally with the index of the split in the way. */
sealed class SplitWayAt : Comparable<SplitWayAt> {
    abstract val pos: LatLon
    protected abstract val index: Int
    protected abstract val delta: Double

    /** sort by index, then delta, ascending. The algorithm relies on this order! */
    override fun compareTo(other: SplitWayAt): Int {
        val diffIndex = index - other.index
        if (diffIndex != 0) return diffIndex

        val diffDelta = delta - other.delta
        return diffDelta.sign.toInt()
    }
}

data class SplitWayAtIndex(override val pos: LatLon, public override val index: Int) : SplitWayAt() {
    override val delta get() = 0.0
}

data class SplitWayAtLinePosition(
    val pos1: LatLon,
    val index1: Int,
    val pos2: LatLon,
    val index2: Int,
    public override val delta: Double
) : SplitWayAt() {
    override val index get() = index1
    override val pos: LatLon
        get() {
            val line = listOf(pos1, pos2)
            return line.pointOnPolylineFromStart(line.measuredLength() * delta)!!
        }
}

/** creates a SplitWay from a SplitLineAtPosition, given the nodes of the way. So, basically it
 *  simply finds the node index/indices at which the split should be made.
 *  One SplitPolylineAtPosition will map to several SplitWays for self-intersecting ways that have
 *  a split at the position where they self-intersect. I.e. a way in the shape of an 8 split exactly
 *  in the centre.
 *  If the way changed significantly in the meantime, it will throw an ElementConflictException */
fun SplitPolylineAtPosition.toSplitWayAt(positions: List<LatLon>): SplitWayAt {
    /* For stability reasons, or to be more precise, to be able to state in advance how many new
       elements will be created by this change, we always only split at the first intersection of
       self-intersecting. This doesn't make a huge difference anyway as self-intersecting ways are
       very rare and likely an error and splitting such a way only once and not several times at
       the same position does not lead to wrong or corrupted data */
    return when (this) {
        is SplitAtPoint -> toSplitWays(positions)
        is SplitAtLinePosition -> toSplitWaysAt(positions)
    }.first()
}

private fun SplitAtPoint.toSplitWays(positions: List<LatLon>): Collection<SplitWayAtIndex> {
    /* could be several indices, for example if the way has the shape of an 8.
       For example a line going through points 1,2,3,4,2,5
       3   1
       |\ /
       | 2
       |/ \
       4   5
     */

    var indicesOf = positions.osmIndicesOf(pos)
    if (indicesOf.isEmpty()) throw ConflictException("To be split point has been moved")

    indicesOf = indicesOf.filter { index -> index > 0 && index < positions.lastIndex }
    if (indicesOf.isEmpty()) {
        throw ConflictException("Split position is now at the very start or end of the way - can't split there")
    }

    return indicesOf.map { indexOf -> SplitWayAtIndex(pos, indexOf) }.sorted()
}

private fun SplitAtLinePosition.toSplitWaysAt(positions: List<LatLon>): Collection<SplitWayAtLinePosition> {
    // could be several indices, for example if the way has the shape of an 8,
    // see SplitAtPoint.toSplitWay

    val indicesOf1 = positions.osmIndicesOf(pos1)
    if (indicesOf1.isEmpty()) throw ConflictException("To be split line has been moved")

    val indicesOf2 = positions.osmIndicesOf(pos2)
    if (indicesOf2.isEmpty()) throw ConflictException("To be split line has been moved")

    // ...and we need to find out which of the lines is meant
    val result = mutableListOf<SplitWayAtLinePosition>()
    for (i1 in indicesOf1) {
        for (i2 in indicesOf2) {
            /* For SplitAtLinePosition, the direction of the way does not matter. But for the
               SplitWayAtLinePosition it must be in the same order as the OSM way. */
            if (i1 + 1 == i2) result.add(SplitWayAtLinePosition(pos1, i1, pos2, i2, delta))
            if (i2 + 1 == i1) result.add(SplitWayAtLinePosition(pos2, i2, pos1, i1, 1.0 - delta))
        }
    }
    /* The result can still be several split SplitWayAtLinePosition: if two lines are on top of each
       other. For example a line going through the points 1,2,3,4,2,1,5
       3
       |\
       | 2 -- 1 -- 5
       |/
       4
     */

    if (result.isEmpty()) {
        throw ConflictException("End points of the to be split line are not directly successive anymore")
    }

    return result.sorted()
}

/** returns the indices at which the given pos is found in this list, taking into account the limited
 *  precision of positions in OSM. */
private fun List<LatLon>.osmIndicesOf(pos: LatLon): List<Int> =
    mapIndexedNotNull { i, p -> if (p.equalsInOsm(pos)) i else null }
