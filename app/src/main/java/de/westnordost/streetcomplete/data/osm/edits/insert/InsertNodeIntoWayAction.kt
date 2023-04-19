package de.westnordost.streetcomplete.data.osm.edits.insert

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.NewElementsCount
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.changesApplied
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

/** Action that creates a node as part of a single way or reuses a node that already exists at the
 *  desired position.
 *
 *  If [insertBetween] is not null, a new node at the given [position]  with the given [tags] will
 *  be inserted in between the nodes at the given positions (or throw a conflict if there are no two
 *  consecutive nodes at the given positions).
 *
 *  If [insertBetween] is null, the node at [position] will be used to insert the given [tags] (or
 *  throw a conflict if there is no node at that position)
 *
 *  That a new node is only created optionally is analogous to the SplitWayAction. We *could* use
 *  UpdateElementTagsAction for the purpose to just add some tags to a node in a way, but from the
 *  user's point of view, semantically, it is a different action. For example, there should be a
 *  conflict if the position moved.
 *  */
@Serializable
data class InsertNodeIntoWayAction(
    val position: LatLon,
    val changes: StringMapChanges,
    val insertBetween: InsertBetween? = null
) : ElementEditAction {

    override val newElementsCount get() =
        NewElementsCount(if (insertBetween != null) 1 else 0, 0, 0)

    override fun createUpdates(
        originalElement: Element,
        element: Element?,
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {

        // element is the way where we insert the node
        val way = element as? Way ?: throw ConflictException("Element deleted")
        val originalWay = originalElement as Way

        val completeWay = mapDataRepository.getWayComplete(way.id)
        val updatedWay = completeWay?.getWay(way.id)
            ?: throw ConflictException("Way #${way.id} has been deleted")

        if (isGeometrySubstantiallyDifferent(originalWay, updatedWay)) {
            throw ConflictException("Way #${way.id} has been changed and the conflict cannot be solved automatically")
        }

        val wayPositions = updatedWay.nodeIds.map { nodeId -> completeWay.getNode(nodeId)!!.position }

        if (insertBetween != null) {
            val node1Index = wayPositions.indexOfFirst { it.equalsInOsm(insertBetween.pos1) }
            if (node1Index == -1) throw ConflictException("Node in way at insert position has been moved or deleted")

            // ensures that node 2 is after node 1 (e.g. when node 2 is the first and last node in a closed way)
            val node2Index = wayPositions
                .subList(node1Index + 1, wayPositions.size)
                .indexOfFirst { it.equalsInOsm(insertBetween.pos2) }
            if (node2Index == -1) throw ConflictException("Node in way at insert position has been moved or deleted")
            // index 0 because of subList starting at node1Index + 1
            if (node2Index != 0) throw ConflictException("Other nodes have been inserted into the way")

            val tags = HashMap<String, String>()
            changes.applyTo(tags)
            val node = Node(idProvider.nextNodeId(), position, tags, 1, nowAsEpochMilliseconds())

            // insert node into the way
            val nodeIds = way.nodeIds.toMutableList()
            nodeIds.add(node1Index + 1, node.id)

            return MapDataChanges(
                creations = listOf(node),
                modifications = listOf(way.copy(
                    nodeIds = nodeIds,
                    timestampEdited = nowAsEpochMilliseconds()
                ))
            )

        } else {
            val nodeIndex = wayPositions.indexOfFirst { it.equalsInOsm(position) }
            if (nodeIndex == -1) throw ConflictException("Node in way at insert position has been moved or deleted")

            val node = completeWay.getNode(way.nodeIds[nodeIndex])!!

            return MapDataChanges(modifications = listOf(node.changesApplied(changes)))
        }
    }
}

@Serializable
data class InsertBetween(val pos1: LatLon, val pos2: LatLon)
