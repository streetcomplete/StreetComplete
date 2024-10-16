package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsActionRevertable
import de.westnordost.streetcomplete.data.osm.edits.NewElementsCount
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.util.ktx.equalsInOsm
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.serialization.Serializable

/** Action that creates a node and optionally inserts it as a vertex into the given way(s) */
@Serializable
data class CreateNodeAction(
    val position: LatLon,
    val tags: Map<String, String>,
    val insertIntoWays: List<InsertIntoWayAt> = emptyList()
) : ElementEditAction, IsActionRevertable {

    override val newElementsCount get() = NewElementsCount(1, 0, 0)

    override val elementKeys: List<ElementKey> get() =
        insertIntoWays.map { ElementKey(ElementType.WAY, it.wayId) }

    override fun idsUpdatesApplied(updatedIds: Map<ElementKey, Long>) = copy(
        insertIntoWays = insertIntoWays.map {
            it.copy(wayId = updatedIds[ElementKey(ElementType.WAY, it.wayId)] ?: it.wayId)
        }
    )

    override fun createUpdates(
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val newNode = createNode(idProvider)

        // inserting node into way(s)
        val editedWays = ArrayList<Way>(insertIntoWays.size)
        for (insertIntoWay in insertIntoWays) {
            val wayId = insertIntoWay.wayId
            val wayComplete = mapDataRepository.getWayComplete(wayId)

            val way = wayComplete?.getWay(wayId)
                ?: throw ConflictException("Way #$wayId has been deleted")

            val positions = way.nodeIds.map { nodeId -> wayComplete.getNode(nodeId)!!.position }

            val node1Index = positions.indexOfFirst { it.equalsInOsm(insertIntoWay.pos1) }
            if (node1Index == -1) throw ConflictException("Node in way at insert position has been moved or deleted")

            // ensures that node 2 is after node 1 (e.g. when node 2 is the first and last node in a closed way)
            val node2Index = positions
                .subList(node1Index + 1, positions.size)
                .indexOfFirst { it.equalsInOsm(insertIntoWay.pos2) }
            if (node2Index == -1) throw ConflictException("Node in way at insert position has been moved or deleted")
            // index 0 because of subList starting at node1Index + 1
            if (node2Index != 0) throw ConflictException("Other nodes have been inserted into the way")

            val nodeIds = way.nodeIds.toMutableList()
            nodeIds.add(node1Index + 1, newNode.id)

            editedWays.add(way.copy(nodeIds = nodeIds, timestampEdited = nowAsEpochMilliseconds()))
        }

        return MapDataChanges(creations = listOf(newNode), modifications = editedWays)
    }

    override fun createReverted(idProvider: ElementIdProvider) =
        RevertCreateNodeAction(createNode(idProvider), insertIntoWays.map { it.wayId })

    private fun createNode(idProvider: ElementIdProvider) =
        Node(idProvider.nextNodeId(), position, tags, 1, nowAsEpochMilliseconds())
}

/** Inserting the node into the given [wayId] in between the nodes at the given positions
 *  [pos1] and [pos2].
 *  Not the node ids but the positions are given for better conflict checking - if any of the two
 *  nodes have been moved, that's a conflict. */
@Serializable
data class InsertIntoWayAt(val wayId: Long, val pos1: LatLon, val pos2: LatLon)
