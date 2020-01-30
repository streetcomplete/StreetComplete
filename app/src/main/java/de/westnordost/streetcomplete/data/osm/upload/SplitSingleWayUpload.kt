package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.*
import javax.inject.Inject
import de.westnordost.osmapi.map.data.Element.Type.*
import de.westnordost.streetcomplete.data.osm.changes.SplitAtLinePosition
import de.westnordost.streetcomplete.data.osm.changes.SplitAtPoint
import de.westnordost.streetcomplete.data.osm.changes.SplitPolylineAtPosition
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.measuredLength
import de.westnordost.streetcomplete.util.pointOnPolylineFromStart
import kotlin.math.sign

/** Uploads one split way
 *  Returns only the ways that have been updated or throws a ConflictException */
class SplitSingleWayUpload @Inject constructor(private val osmDao: MapDataDao)  {

    fun upload(changesetId: Long, way: Way, splits: List<SplitPolylineAtPosition>): List<Way> {
        val updatedWay = way.fetchUpdated()
            ?: throw ElementDeletedException("Way #${way.id} has been deleted")
        if(updatedWay.isClosed() && splits.size < 2)
            throw ElementConflictException("Must specify at least two split positions for a closed way")
        checkForConflicts(way, updatedWay)

        val nodes = updatedWay.fetchNodes()
        val positions = nodes.map { it.position }
        /* the splits must be sorted strictly from start to end of way because the algorithm may
           insert nodes in the way */
        val sortedSplits = splits.flatMap { it.toSplitWay(positions) }.sorted()

        val uploadElements = mutableListOf<Element>()
        var newNodeId = -1L

        val splitAtIndices = mutableListOf<Int>()
        var insertedNodeCount = 0
        for (split in sortedSplits) {
            when(split) {
                is SplitWayAtPoint -> {
                    splitAtIndices.add(split.index + insertedNodeCount)
                }
                is SplitWayAtLinePosition -> {
                    val splitNode = OsmNode(newNodeId--, 1, split.pos, null)
                    uploadElements.add(splitNode)

                    val nodeIndex = split.index2 + insertedNodeCount
                    updatedWay.nodeIds.add(nodeIndex, splitNode.id)
                    splitAtIndices.add(nodeIndex)
                    ++insertedNodeCount
                }
            }
        }

        uploadElements.addAll(splitWayAtIndices(updatedWay, splitAtIndices))
        val handler = UpdateElementsHandler()
        try {
            osmDao.uploadChanges(changesetId, uploadElements, handler)
        } catch (e: OsmConflictException) {
            throw ChangesetConflictException(e.message, e)
        }
        // the added nodes and updated relations are not relevant for quest creation, only the way are
        return handler.getElementUpdates(uploadElements).updated.filterIsInstance<Way>()
    }

    private fun checkForConflicts(old: Way, new: Way) {
        if(old.version != new.version) {
            // unsolvable conflict if other was shortened (e.g. cut in two) or extended
            if(old.nodeIds.first() != new.nodeIds.first() || old.nodeIds.last() != new.nodeIds.last())
                throw ElementConflictException("Way #${old.id} has been changed and the conflict cannot be solved automatically")
        }
    }

    private fun splitWayAtIndices(originalWay: Way, splitIndices: List<Int>): List<Element> {
        val newWays = createSplitWays(originalWay, splitIndices)
        val updatedRelations = updateRelations(originalWay, newWays)
        return newWays + updatedRelations
    }

    /** Returns the elements that have been changed */
    private fun createSplitWays(originalWay: Way, splitIndices: List<Int>): List<Way> {
        val nodesChunks = originalWay.nodeIds.splitIntoChunks(splitIndices)
        /* Handle circular ways specially: If you split at a circular way at two nodes, you just
           want to split it at these points, not also at the former endpoint. So if the last node is
           the same first node, join the last and the first way chunk. (copied from JOSM) */
        if (nodesChunks.size > 1 && nodesChunks.first().first() == nodesChunks.last().last()) {
            val lastChunk = nodesChunks.removeAt(nodesChunks.lastIndex)
            lastChunk.removeAt(lastChunk.lastIndex)
            nodesChunks.first().addAll(0, lastChunk)
        }

        /* Instead of deleting the old way and replacing it with the new splitted chunks, one of the
           chunks should use the id of the old way, so that it inherits the OSM history of the
           previous way. The chunk with the most nodes is selected for this.
           This is the same behavior as JOSM and Vespucci. */
        val indexOfChunkToKeep = nodesChunks.indexOfMaxBy { it.size }
        val tags = originalWay.tags?.toMap()
        var newWayId = -1L
        return nodesChunks.mapIndexed { index, nodes ->
            if(index == indexOfChunkToKeep) {
                OsmWay(originalWay.id, originalWay.version, nodes, tags).apply {
                    isModified = true
                }
            }
            else {
                OsmWay(newWayId--, 0, nodes, tags)
            }
        }
    }

    /** Returns the elements that have been changed */
    private fun updateRelations(originalWay: Way, newWays: List<Way>) : Collection<Relation> {
        val relations = originalWay.fetchParentRelations()
        val result = mutableSetOf<Relation>()
        for (relation in relations) {
            /* iterating in reverse because the relation member for the original way is replaced
               by one or several new ways in-place within the loop. So, no relation member is
               iterated twice. If in the future, there will be any special relation that removes
               a relation member or replaces several relation members with only one relation member,
               then this here won't work anymore and the algorithm needs to modify a copy of the
               relation members list. */
            for(i in relation.members.size - 1 downTo 0) {
                val relationMember = relation.members[i]
                if (relationMember.type == WAY && relationMember.ref == originalWay.id) {
                    if (!updateSpecialRelation(relation, i, newWays)) {
                        updateNormalRelation(relation, i, originalWay, newWays)
                    }
                    result.add(relation)
                }
            }
        }
        return result
    }

    /** Returns whether it has been treated as a special relation type */
    private fun updateSpecialRelation(relation: Relation, indexOfWayInRelation: Int, newWays: List<Way>): Boolean {
        val relationType = relation.tags?.get("type") ?: ""
        val originalWayRole = relation.members[indexOfWayInRelation].role
        /* for a from-to-relation (i.e. turn restriction, destination sign, ...) only the two ways
           directly connecting with the via node/way should be kept in the relation. If one of these
           ways is split up, the correct way chunk must be selected to replace the old way. */
        if (originalWayRole == "from" || originalWayRole == "to") {
            val viaNodeIds = relation.fetchViaNodeIds(relationType)
            if (viaNodeIds.isNotEmpty()) {
                val newWay = newWays.find { viaNodeIds.containsAny(it.nodeIds.firstAndLast()) }
                if (newWay != null) {
                    val newRelationMember = OsmRelationMember(newWay.id, originalWayRole, WAY)
                    relation.members[indexOfWayInRelation] = newRelationMember
                    return true
                }
            }
        }
        // room for handling other special relation types here

        return false
    }

    private fun updateNormalRelation(relation: Relation, indexOfWayInRelation: Int,
                                     originalWay: Way, newWays: List<Way>) {
        /* for any (non-special, see above) relation, the new way chunks that replace the original
           way must be all inserted into each relation.  In the correct order, if the relation is
           ordered at all. */
        val originalWayRole = relation.members[indexOfWayInRelation].role
        val newRelationMembers = newWays.map { way ->
            OsmRelationMember(way.id, originalWayRole, WAY) }.toMutableList()
        val isOrientedBackwards = originalWay.isOrientedForwardInOrderedRelation(relation, indexOfWayInRelation) == false
        if (isOrientedBackwards) newRelationMembers.reverse()

        relation.members.removeAt(indexOfWayInRelation)
        relation.members.addAll(indexOfWayInRelation, newRelationMembers)
    }

    private fun Way.fetchNodes(): List<Node> {
        try {
            val nodesMap = osmDao.getNodes(nodeIds).associateBy { node -> node.id }
            // the fetched nodes must be returned ordered in the way
            return nodeIds.map { nodeId -> nodesMap.getValue(nodeId) }
        } catch (e: OsmNotFoundException) {
            throw ConflictException("Way was modified right while uploading the changes (what's the chance?)",e)
        }
    }

    private fun Way.fetchUpdated(): Way? = osmDao.getWay(id)

    private fun Way.fetchParentRelations() = osmDao.getRelationsForWay(id)

    /** returns null if the relation is not ordered, false if oriented backwards, true if oriented forward */
    private fun Way.isOrientedForwardInOrderedRelation(relation: Relation, indexInRelation: Int): Boolean? {
        val wayIdBefore = relation.members.findPrevious(indexInRelation) { it.type == WAY }?.ref
        val wayBefore = wayIdBefore?.let { osmDao.getWay(it) }
        if (wayBefore != null) {
            if (isAfterWayInChain(wayBefore)) return true
            if (isBeforeWayInChain(wayBefore)) return false
        }

        val wayIdAfter = relation.members.findNext(indexInRelation+1) { it.type == WAY }?.ref
        val wayAfter = wayIdAfter?.let { osmDao.getWay(it) }
        if (wayAfter != null) {
            if (isBeforeWayInChain(wayAfter)) return true
            if (isAfterWayInChain(wayAfter)) return false
        }

        return null
    }

    private fun Relation.fetchViaNodeIds(relationType: String): Set<Long> {
        val vias = findVias(relationType)
        val nodeIds = mutableSetOf<Long>()
        for (via in vias) {
            if (via.type == NODE) {
                nodeIds.add(via.ref)
            } else if (via.type == WAY) {
                val way = osmDao.getWay(via.ref)
                if (way != null) nodeIds.addAll(way.nodeIds.firstAndLast())
            }
        }
        return nodeIds
    }

}

/** data class that carries the information for one split to perform on a random position on a way.
 *  So, same as SplitPolylineAtPosition, but additionally with the index of the split in the way. */
private sealed class SplitWay : Comparable<SplitWay> {
    abstract val pos: LatLon
    protected abstract val index: Int
    protected abstract val delta: Double

    /** sort by index, then delta, ascending. The algorithm relies on this order! */
    override fun compareTo(other: SplitWay): Int {
        val diffIndex = index - other.index
        if (diffIndex != 0) return diffIndex

        val diffDelta = delta - other.delta
        return diffDelta.sign.toInt()
    }
}

private data class SplitWayAtPoint(override val pos: LatLon, public override val index: Int) : SplitWay() {
    override val delta get() = 0.0
}

private data class SplitWayAtLinePosition( val pos1: LatLon, val index1: Int,
                                           val pos2: LatLon, val index2: Int,
                                           public override val delta: Double) : SplitWay() {
    override val index get() = index1
    override val pos: LatLon get() {
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
private fun SplitPolylineAtPosition.toSplitWay(positions: List<LatLon>): Collection<SplitWay> {
    return when(this) {
        is SplitAtPoint -> toSplitWay(positions)
        is SplitAtLinePosition -> toSplitWay(positions)
    }
}

private fun SplitAtPoint.toSplitWay(positions: List<LatLon>): Collection<SplitWayAtPoint> {
    // could be several indices, for example if the way has the shape of an 8.
    var indicesOf = positions.osmIndicesOf(pos)
    if (indicesOf.isEmpty()) throw ElementConflictException("To be split point has been moved")

    indicesOf = indicesOf.filter { index -> index > 0 && index < positions.lastIndex }
    if (indicesOf.isEmpty())
        throw ElementConflictException("Split position is now at the very start or end of the way - can't split there")

    return indicesOf.map { indexOf -> SplitWayAtPoint(pos, indexOf) }
}

private fun SplitAtLinePosition.toSplitWay(positions: List<LatLon>): Collection<SplitWayAtLinePosition> {
    // could be several indices, for example if the way has the shape of an 8...
    val indicesOf1 = positions.osmIndicesOf(pos1)
    if (indicesOf1.isEmpty()) throw ElementConflictException("To be split line has been moved")

    val indicesOf2 = positions.osmIndicesOf(pos2)
    if (indicesOf2.isEmpty()) throw ElementConflictException("To be split line has been moved")

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
    if (result.isNotEmpty())
        return result
    else
        throw ElementConflictException("End points of the to be split line are not directly successive anymore")
}

/** returns the indices at which the given pos is found in this list, taking into accound the limited
 *  precision of positions in OSM. */
private fun List<LatLon>.osmIndicesOf(pos: LatLon): List<Int> =
    mapIndexedNotNull { i, p -> if (p.equalsInOsm(pos)) i else null }


/** returns a copy of the list split at the given indices with each chunk sharing each the first and last element */
private fun <E> List<E>.splitIntoChunks(indices: List<Int>): MutableList<MutableList<E>> {
    val result = mutableListOf<MutableList<E>>()
    var lastIndex = 0
    for (index in indices) {
        result.add(subList(lastIndex, index+1).toMutableList())
        lastIndex = index
    }
    result.add(subList(lastIndex, size).toMutableList())
    return result
}

/** returns whether this way immediately precedes the given way in a chain */
private fun Way.isBeforeWayInChain(way: Way) =
    nodeIds.last() == way.nodeIds.last() || nodeIds.last() == way.nodeIds.first()

/** returns whether this way immediately follows the given way in a chain */
private fun Way.isAfterWayInChain(way: Way) =
    nodeIds.first() == way.nodeIds.last() || nodeIds.first() == way.nodeIds.first()

private fun Relation.findVias(relationType: String): List<RelationMember> {
    val nodesAndWays = members.filter { it.type == NODE || it.type == WAY }
    return when (relationType) {
        "restriction" -> nodesAndWays.filter { it.role == "via" }
        "destination_sign" -> {
            nodesAndWays.filter { it.role == "intersection" }.takeUnless { it.isEmpty() } ?:
            nodesAndWays.filter { it.role == "sign" }
        }
        else -> nodesAndWays.filter { it.role == "via" }
    }
}
