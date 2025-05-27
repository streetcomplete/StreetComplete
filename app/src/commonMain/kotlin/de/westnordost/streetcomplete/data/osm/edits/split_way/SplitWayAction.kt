package de.westnordost.streetcomplete.data.osm.edits.split_way

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.NewElementsCount
import de.westnordost.streetcomplete.data.osm.edits.update_tags.isGeometrySubstantiallyDifferent
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.RelationMember
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.util.ktx.containsAny
import de.westnordost.streetcomplete.util.ktx.findNext
import de.westnordost.streetcomplete.util.ktx.findPrevious
import de.westnordost.streetcomplete.util.ktx.firstAndLast
import de.westnordost.streetcomplete.util.ktx.indexOfMaxBy
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.serialization.Serializable

/** Action that performs a split on a way.
 *
 *  The split positions are passed as lat-lon position because it more robust when handling
 *  conflicts than if the split positions were kept as node ids or node indices of the way.
 *
 *  The original way's first and last node id is passed in order to decide if an updated way is
 *  still compatible with the split action: If the updated way was shortened or extended on either
 *  end, it is not considered compatible anymore
 */
@Serializable
data class SplitWayAction(
    val originalWay: Way,
    val splits: List<SplitPolylineAtPosition>
) : ElementEditAction {

    override val newElementsCount get() = NewElementsCount(
        nodes = splits.count { it is SplitAtLinePosition },
        ways = splits.size,
        relations = 0
    )

    override val elementKeys get() = listOf(originalWay.key)

    override fun idsUpdatesApplied(updatedIds: Map<ElementKey, Long>) = copy(
        originalWay = originalWay.copy(id = updatedIds[originalWay.key] ?: originalWay.id)
    )

    override fun createUpdates(
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val wayId = originalWay.id
        val wayComplete = mapDataRepository.getWayComplete(wayId)
            ?: throw ConflictException("Element deleted")

        var currentWay = wayComplete.getWay(wayId)
            ?: throw ConflictException("Way #$wayId has been deleted")

        if (isGeometrySubstantiallyDifferent(originalWay, currentWay)) {
            throw ConflictException("Way #$wayId has been changed and the conflict cannot be solved automatically")
        }

        if (currentWay.isClosed && splits.size < 2) {
            throw ConflictException("Must specify at least two split positions for a closed way")
        }

        // step 0: convert list of SplitPolylineAtPosition to list of SplitWay
        val positions = currentWay.nodeIds.map { nodeId -> wayComplete.getNode(nodeId)!!.position }
        /* the splits must be sorted strictly from start to end of way because the algorithm may
           insert nodes in the way */
        val sortedSplits = splits.map { it.toSplitWayAt(positions) }.sorted()

        // step 1: add split nodes into original way
        val createdNodes = mutableListOf<Node>()
        val splitAtIndices = mutableListOf<Int>()
        var insertedNodeCount = 0
        for (split in sortedSplits) {
            when (split) {
                is SplitWayAtIndex -> {
                    splitAtIndices.add(split.index + insertedNodeCount)
                }
                is SplitWayAtLinePosition -> {
                    val splitNode = Node(idProvider.nextNodeId(), split.pos, emptyMap(), 1, nowAsEpochMilliseconds())
                    createdNodes.add(splitNode)

                    val nodeIndex = split.index2 + insertedNodeCount
                    val nodeIds = currentWay.nodeIds.toMutableList()
                    nodeIds.add(nodeIndex, splitNode.id)
                    currentWay = currentWay.copy(nodeIds = nodeIds)
                    splitAtIndices.add(nodeIndex)
                    ++insertedNodeCount
                }
            }
        }

        // step 2: split up the ways into several ways
        val updatedWays = getSplitWayAtIndices(currentWay, splitAtIndices, idProvider)

        // step 3: update all relations the original way was member of, if any
        val updatedRelations = getUpdatedRelations(currentWay, updatedWays, mapDataRepository)

        return MapDataChanges(
            creations = createdNodes + updatedWays.filter { it.id < 0 },
            modifications = updatedWays.filter { it.id >= 0 } + updatedRelations
        )
    }
}

//region Split way into ways

/** Split the given way at the specified indices.
 *  Returns the list of updated ways: the original but shortened way and a new way for each split */
private fun getSplitWayAtIndices(
    originalWay: Way,
    splitIndices: List<Int>,
    idProvider: ElementIdProvider
): List<Way> {
    val nodesChunks = originalWay.nodeIds.splitIntoChunks(splitIndices)
    /* Handle circular ways specially: If you split at a circular way at two nodes, you just
       want to split it at these points, not also at the former endpoint. So if the last node is
       the same first node, join the last and the first way chunk. (copied from JOSM) */
    if (nodesChunks.size > 1 && nodesChunks.first().first() == nodesChunks.last().last()) {
        val lastChunk = nodesChunks.removeAt(nodesChunks.lastIndex)
        lastChunk.removeAt(lastChunk.lastIndex)
        nodesChunks.first().addAll(0, lastChunk)
    }

    /* Instead of deleting the old way and replacing it with the new split chunks, one of the
       chunks should use the id of the old way, so that it inherits the OSM history of the previous
       way. The chunk with the most nodes is selected for this.
       This is the same behavior as JOSM and Vespucci. */
    val indexOfChunkToKeep = nodesChunks.indexOfMaxBy { it.size }
    val tags = originalWay.tags.toMutableMap()
    removeTagsThatArePotentiallyWrongAfterSplit(tags)

    return nodesChunks.mapIndexed { index, nodes ->
        // keep the original timestampEdited, so resurvey quests are still shown after splitting (only when auto-sync is off)
        if (index == indexOfChunkToKeep) {
            Way(originalWay.id, nodes, tags, originalWay.version, originalWay.timestampEdited)
        } else {
            Way(idProvider.nextWayId(), nodes, tags, 0, originalWay.timestampEdited)
        }
    }
}

/** returns a copy of the list split at the given indices with each chunk sharing each the first and last element */
private fun <E> List<E>.splitIntoChunks(indices: List<Int>): MutableList<MutableList<E>> {
    val result = mutableListOf<MutableList<E>>()
    var lastIndex = 0
    for (index in indices) {
        result.add(subList(lastIndex, index + 1).toMutableList())
        lastIndex = index
    }
    result.add(subList(lastIndex, size).toMutableList())
    return result
}

/** edit the tags because some tags shouldn't be carried over to the new ways as they may
 *  be incorrect now */
private fun removeTagsThatArePotentiallyWrongAfterSplit(tags: MutableMap<String, String>) {
    tags.remove("step_count")
    // only remove if "steps" is a number cause it is apparently also used to denote kind of steps
    if (tags["steps"]?.toIntOrNull() != null) tags.remove("steps")

    // only remove "incline" if it contains a number
    val inclineNumberRegex = Regex("[0-9]")
    val inclineValue = tags["incline"]
    if (inclineValue != null && inclineNumberRegex.containsMatchIn(inclineValue)) tags.remove("incline")

    // remove any capacity: "capacity", "bicycle_parking:capacity", "parking:lane:both:capacity", "parking:lane:right:capacity:disabled" etc.
    tags.remove("seats")
    val capacityRegex = Regex("^(.*:)?capacity(:.*)?$")
    val keysToDelete = tags.keys.filter { capacityRegex.matches(it) }
    for (key in keysToDelete) {
        tags.remove(key)
    }
}

//endregion

//region Update relations

/** Updates all relations that are referenced by the original way for when that way is split up
 *  into the the list of new ways. Returns the updated relations */
private fun getUpdatedRelations(
    originalWay: Way,
    newWays: List<Way>,
    mapDataRepository: MapDataRepository
): Collection<Relation> {
    val relations = mapDataRepository.getRelationsForWay(originalWay.id)
    val result = ArrayList<Relation>()
    for (relation in relations) {
        val updatedRelationMembers = ArrayList<RelationMember>()
        relation.members.forEachIndexed { i, relationMember ->
            if (relationMember.type == ElementType.WAY && relationMember.ref == originalWay.id) {
                updatedRelationMembers.addAll(
                    getRelationMemberReplacements(relation, i, originalWay, newWays, mapDataRepository)
                )
            } else {
                updatedRelationMembers.add(relationMember)
            }
        }
        result.add(relation.copy(
            members = updatedRelationMembers,
            timestampEdited = nowAsEpochMilliseconds()
        ))
    }
    return result
}

/** Return by which relation member(s) the relation member that referenced the original way should
 *  be replaced. Some relation types have certain special rules for that.
 */
private fun getRelationMemberReplacements(
    relation: Relation,
    indexOfWayInRelation: Int,
    originalWay: Way,
    newWays: List<Way>,
    mapDataRepository: MapDataRepository
): List<RelationMember> {
    val originalWayRole = relation.members[indexOfWayInRelation].role

    /* for a from-to-relation (e.g. turn restriction, destination sign, ...) only the two ways
      directly connecting with the via node/way should be kept in the relation. If one of these
      ways is split up, the correct way chunk must be selected to replace the old way. */
    if (originalWayRole == "from" || originalWayRole == "to") {
        val viaNodeIds = relation.fetchViaNodeIds(mapDataRepository)
        if (viaNodeIds.isNotEmpty()) {
            val newWay = newWays.find { viaNodeIds.containsAny(it.nodeIds.firstAndLast()) }
            if (newWay != null) {
                return listOf(RelationMember(ElementType.WAY, newWay.id, originalWayRole))
            }
        }
    }
    /* for any (non-special, see above) relation, the new way chunks that replace the original
       way must be all inserted into each relation. In the correct order, if the relation is
       ordered at all. */
    val newRelationMembers = newWays.map { way -> RelationMember(ElementType.WAY, way.id, originalWayRole) }.toMutableList()
    val isOrientedBackwards = originalWay.isOrientedForwardInOrderedRelation(relation, indexOfWayInRelation, mapDataRepository) == false
    if (isOrientedBackwards) newRelationMembers.reverse()

    return newRelationMembers
}

/** Return the node ids of the "via" node(s) that connect to the ways with the "from" and "to" role
 *  in a "restriction" (like) relation.
 *  Usually it is just one node, it may be several if the "via" is actually a way (the first and
 *  last node, then). */
private fun Relation.fetchViaNodeIds(mapDataRepository: MapDataRepository): Set<Long> {
    val vias = findVias()
    val nodeIds = mutableSetOf<Long>()
    for (via in vias) {
        if (via.type == ElementType.NODE) {
            nodeIds.add(via.ref)
        } else if (via.type == ElementType.WAY) {
            val way = mapDataRepository.getWay(via.ref)
            if (way != null) nodeIds.addAll(way.nodeIds.firstAndLast())
        }
    }
    return nodeIds
}

/** returns null if the relation is not ordered, false if oriented backwards, true if oriented forward */
private fun Way.isOrientedForwardInOrderedRelation(
    relation: Relation,
    indexInRelation: Int,
    mapDataRepository: MapDataRepository
): Boolean? {
    val wayIdBefore = relation.members.findPrevious(indexInRelation) { it.type == ElementType.WAY }?.ref
    val wayBefore = wayIdBefore?.let { mapDataRepository.getWay(it) }
    if (wayBefore != null) {
        if (isAfterWayInChain(wayBefore)) return true
        if (isBeforeWayInChain(wayBefore)) return false
    }

    val wayIdAfter = relation.members.findNext(indexInRelation + 1) { it.type == ElementType.WAY }?.ref
    val wayAfter = wayIdAfter?.let { mapDataRepository.getWay(it) }
    if (wayAfter != null) {
        if (isBeforeWayInChain(wayAfter)) return true
        if (isAfterWayInChain(wayAfter)) return false
    }

    return null
}

/** Return all relation members that fill the "via" role in a "restriction" or similar relation */
private fun Relation.findVias(): List<RelationMember> {
    val type = tags["type"] ?: ""
    val nodesAndWays = members.filter { it.type == ElementType.NODE || it.type == ElementType.WAY }
    return when (type) {
        "destination_sign" -> {
            nodesAndWays.filter { it.role == "intersection" }.takeUnless { it.isEmpty() }
                ?: nodesAndWays.filter { it.role == "sign" }
        }
        else -> {
            nodesAndWays.filter { it.role == "via" }
        }
    }
}

/** returns whether this way immediately precedes the given way in a chain */
private fun Way.isBeforeWayInChain(way: Way) =
    nodeIds.last() == way.nodeIds.last() || nodeIds.last() == way.nodeIds.first()

/** returns whether this way immediately follows the given way in a chain */
private fun Way.isAfterWayInChain(way: Way) =
    nodeIds.first() == way.nodeIds.last() || nodeIds.first() == way.nodeIds.first()

//endregion
