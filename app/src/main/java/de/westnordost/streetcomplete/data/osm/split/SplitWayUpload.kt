package de.westnordost.streetcomplete.data.osm.split

import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.util.SphericalEarthMath
import javax.inject.Inject
import de.westnordost.osmapi.map.data.Element.Type.*
import de.westnordost.streetcomplete.ktx.containsAny
import de.westnordost.streetcomplete.ktx.findNext
import de.westnordost.streetcomplete.ktx.findPrevious
import de.westnordost.streetcomplete.ktx.firstAndLast

class SplitWayUpload @Inject constructor(private val osmDao: MapDataDao) {

    fun upload(changesetId: Long, way: Way, splits: List<SplitWayAtPosition>) {
        
        if(way.isClosed() && splits.size < 2)
            throw IllegalArgumentException("Must specify at least two split positions for a closed way")

        val updatedWay = way.fetchUpdated()
        checkForConflicts(way, updatedWay)
        try {
            for (split in splits) split.validate(updatedWay)
        } catch (e: IllegalArgumentException) {
            throw ConflictException(e.message)
        }
        val sortedSplits = splits.sortedWith(SplitWayAtComparator(updatedWay))

        val uploadElements = mutableListOf<Element>()
        var newNodeId = -1L

        val splitAtIndices = mutableListOf<Int>()
        for (split in sortedSplits) {
            val updatedFirstNode = split.firstNode.fetchUpdated()
            checkForConflicts(split.firstNode, updatedFirstNode)
            val updatedSecondNode = split.secondNode.fetchUpdated()
            checkForConflicts(split.secondNode, updatedSecondNode)

            if (split.delta == 0.0) {
                val firstNodeIndex = updatedWay.nodeIds.indexOf(updatedFirstNode.id)
                splitAtIndices.add(firstNodeIndex)
            } else if (split.delta > 0.0) {
                val splitPosition = createSplitPosition(updatedFirstNode.position, updatedSecondNode.position, split.delta)
                val splitNode = OsmNode(newNodeId--, 1, splitPosition, null)
                uploadElements.add(splitNode)

                val secondNodeIndex = updatedWay.nodeIds.indexOf(updatedSecondNode.id)
                updatedWay.nodeIds.add(secondNodeIndex, splitNode.id)
                splitAtIndices.add(secondNodeIndex)
            }
        }

        uploadElements.addAll(splitWayAtIndices(updatedWay, splitAtIndices))
        osmDao.uploadChanges(changesetId, uploadElements, null)
    }

    private fun checkForConflicts(old: Way, new: Way) {
        if(old.version != new.version) {
            // unsolvable conflict if other was shortened (e.g. cut in two) or extended
            if(old.nodeIds.first() != new.nodeIds.first() || old.nodeIds.last() != new.nodeIds.last())
                throw ConflictException("Way #${old.id} has been changed and the conflict cannot be solved automatically")
        }
    }

    private fun checkForConflicts(old: Node, new: Node) {
        if(old.version != new.version) {
            // unsolvable conflict if node has been moved
            if(old.position.latitude != new.position.latitude || old.position.longitude != new.position.longitude)
                throw ConflictException("Node #${old.id} has been moved and the conflict cannot be solved automatically")
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

        val indexOfChunkToKeep = nodesChunks.indexOfMaxBy { it.size }
        val tags = originalWay.tags?.toMap()
        var newWayId = -1L
        return nodesChunks.mapIndexed { index, nodes ->
            // TODO modification aware shit
            if(index == indexOfChunkToKeep) OsmWay(originalWay.id, originalWay.version, nodes, tags)
            else                            OsmWay(newWayId--, 0, nodes, tags)
        }
    }

    /** Returns the elements that have been changed */
    private fun updateRelations(originalWay: Way, newWays: List<Way>) : Collection<Relation> {
        val relations = originalWay.fetchParentRelations()
        val result = mutableSetOf<Relation>()
        for (relation in relations) {
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
        if (relationType == "restriction" || relationType == "destination_sign") {
            val originalWayRole = relation.members[indexOfWayInRelation].role
            if (originalWayRole == "from" || originalWayRole == "to") {
                val viaNodeIds = relation.fetchViaNodeIds(relationType)
                if (viaNodeIds != null) {
                    val newWay = newWays.find { it.nodeIds.firstAndLast().containsAny(viaNodeIds) }
                    if (newWay != null) {
                        // TODO modification aware shit
                        val newRelationMember = OsmRelationMember(newWay.id, originalWayRole, WAY)
                        relation.members[indexOfWayInRelation] = newRelationMember
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun updateNormalRelation(relation: Relation, indexOfWayInRelation: Int,
                                     originalWay: Way, newWays: List<Way>) {
        val originalWayRole = relation.members[indexOfWayInRelation].role
        // TODO modification aware shit
        val newRelationMembers = newWays.map { way ->
            OsmRelationMember(way.id, originalWayRole, WAY) }.toMutableList()
        val isOrientedBackwards = originalWay.isOrientedForwardInOrderedRelation(relation, indexOfWayInRelation) == false
        if (isOrientedBackwards) newRelationMembers.reverse()

        relation.members.removeAt(indexOfWayInRelation)
        relation.members.addAll(indexOfWayInRelation, newRelationMembers)
    }

    private fun Node.fetchUpdated() =
        osmDao.getNode(id) ?: throw ConflictException("Node #$id has been deleted")

    private fun Way.fetchUpdated() =
        osmDao.getWay(id) ?: throw ConflictException("Way #$id has been deleted")

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

    private fun Relation.fetchViaNodeIds(relationType: String): List<Long>? {
        val via = findVia(relationType) ?: return null
        return when (via.type) {
            WAY -> osmDao.getWay(via.ref)?.nodeIds?.firstAndLast()
            NODE -> listOf(via.ref)
            else -> null
        }
    }
}

/** comparator that sorts all the splits from start to end in the way */
private class SplitWayAtComparator(private val way: Way) : Comparator<SplitWayAtPosition> {
    override fun compare(split1: SplitWayAtPosition, split2: SplitWayAtPosition): Int {
        val split1Index = way.nodeIds.indexOf(split1.secondNode.id)
        val split2Index = way.nodeIds.indexOf(split2.secondNode.id)
        val diffIndex = split1Index - split2Index
        if (diffIndex != 0) return diffIndex

        val diffDelta = split1.delta - split2.delta
        return if (diffDelta < 0) -1 else if (diffDelta > 0) 1 else 0
    }
}

private fun createSplitPosition(firstPosition: LatLon, secondPosition: LatLon, delta: Double) =
    SphericalEarthMath.createTranslated(
        firstPosition.latitude + delta * (secondPosition.latitude - firstPosition.latitude),
        firstPosition.longitude + delta * (secondPosition.longitude - firstPosition.longitude))

/** returns the index of the first element yielding the largest value of the given function or -1 if there are no elements. */
private inline fun <T, R : Comparable<R>> Iterable<T>.indexOfMaxBy(selector: (T) -> R): Int {
    val iterator = iterator()
    if (!iterator.hasNext()) return -1
    var indexOfMaxElem = 0
    var i = 0
    var maxValue = selector(iterator.next())
    while (iterator.hasNext()) {
        ++i
        val v = selector(iterator.next())
        if (maxValue < v) {
            indexOfMaxElem = i
            maxValue = v
        }
    }
    return indexOfMaxElem
}

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

private fun Way.isClosed() = nodeIds.size >= 3 && nodeIds.first() == nodeIds.last()

/** returns whether this way immediately precedes the given way in a chain */
private fun Way.isBeforeWayInChain(way:Way) =
    nodeIds.last() == way.nodeIds.last() || nodeIds.last() == way.nodeIds.first()

/** returns whether this way immediately follows the given way in a chain */
private fun Way.isAfterWayInChain(way:Way) =
    nodeIds.first() == way.nodeIds.last() || nodeIds.first() == way.nodeIds.first()

private fun Relation.findVia(relationType: String): RelationMember? {
    val nodesAndWays = members.filter { it.type == NODE || it.type == WAY }
    return when (relationType) {
        "restriction" -> nodesAndWays.find { it.role == "via" }
        "destination_sign" -> {
            nodesAndWays.find { it.role == "intersection" } ?:
            nodesAndWays.find { it.role == "sign" }
        }
        else -> null
    }
}
