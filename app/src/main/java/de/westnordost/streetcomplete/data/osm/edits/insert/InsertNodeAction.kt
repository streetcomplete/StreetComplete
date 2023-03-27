package de.westnordost.streetcomplete.data.osm.edits.insert

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.NewElementsCount
import de.westnordost.streetcomplete.data.osm.edits.update_tags.isGeometrySubstantiallyDifferent
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.util.ktx.equalsInOsm
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.serialization.Serializable

/** Action that creates a node as part of a single way. */
@Serializable
data class InsertNodeAction(
    val position: LatLon,
    val tags: Map<String, String>,
    val insertBetween: InsertBetween
) : ElementEditAction {

    override val newElementsCount get() = NewElementsCount(1, 0, 0)

    override fun createUpdates(
        originalElement: Element,
        element: Element?,
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val node = Node(idProvider.nextNodeId(), position, tags, 1, nowAsEpochMilliseconds())
        // element is the way where we insert the node
        val way = element as? Way ?: throw ConflictException("Element deleted")
        val originalWay = originalElement as Way

        val completeWay = mapDataRepository.getWayComplete(way.id)
        val updatedWay = completeWay?.getWay(way.id)
            ?: throw ConflictException("Way #${way.id} has been deleted")

        if (isGeometrySubstantiallyDifferent(originalWay, updatedWay)) {
            throw ConflictException("Way #${way.id} has been changed and the conflict cannot be solved automatically")
        }

        val positions = updatedWay.nodeIds.map { nodeId -> completeWay.getNode(nodeId)!!.position }
        val node1Index = positions.indexOfFirst { it.equalsInOsm(insertBetween.pos1) }
        if (node1Index == -1) throw ConflictException("Node in way at insert position has been moved or deleted")

        // ensures that node 2 is after node 1 (e.g. when node 2 is the first and last node in a closed way)
        val node2Index = positions
            .subList(node1Index + 1, positions.size)
            .indexOfFirst { it.equalsInOsm(insertBetween.pos2) }
        if (node2Index == -1) throw ConflictException("Node in way at insert position has been moved or deleted")
        // index 0 because of subList starting at node1Index + 1
        if (node2Index != 0) throw ConflictException("Other nodes have been inserted into the way")

        // insert node into the way
        val nodeIds = way.nodeIds.toMutableList()
        nodeIds.add(node1Index + 1, node.id)
        return MapDataChanges(creations = listOf(node), modifications = listOf(way.copy(nodeIds = nodeIds, timestampEdited = nowAsEpochMilliseconds())))
    }
}

@Serializable
data class InsertBetween(val pos1: LatLon, val pos2: LatLon)
