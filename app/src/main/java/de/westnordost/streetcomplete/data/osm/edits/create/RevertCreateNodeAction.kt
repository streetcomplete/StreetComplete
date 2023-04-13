package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.serialization.Serializable

/** Action reverts creation of a node */
@Serializable
data class RevertCreateNodeAction(
    private val originalNode: Node,
    private val insertedIntoWayIds: List<Long> = emptyList()
) : ElementEditAction, IsRevertAction {

    // the revert does not depend on the ways into which the node has been inserted to still be there
    override val elementKeys get() = listOf(originalNode.key)

    override fun idsUpdatesApplied(updatedIds: Map<ElementKey, Long>) = copy(
        originalNode = originalNode.copy(id = updatedIds[originalNode.key] ?: originalNode.id),
        insertedIntoWayIds = insertedIntoWayIds.map {
            updatedIds[ElementKey(ElementType.WAY, it)] ?: it
        }
    )

    override fun createUpdates(
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val currentNode = mapDataRepository.getNode(originalNode.id)
            ?: throw ConflictException("Element deleted")

        if (originalNode.position != currentNode.position) {
            throw ConflictException("Node position changed")
        }

        if (originalNode.tags != currentNode.tags) {
            throw ConflictException("Some tags have already been changed")
        }

        if (mapDataRepository.getRelationsForNode(currentNode.id).isNotEmpty()) {
            throw ConflictException("Node is now member of a relation")
        }
        val waysById = mapDataRepository.getWaysForNode(currentNode.id).associateBy { it.id }
        if (waysById.keys.any { it !in insertedIntoWayIds }) {
            throw ConflictException("Node is now also part of yet another way")
        }

        val editedWays = ArrayList<Way>(insertedIntoWayIds.size)
        for (wayId in insertedIntoWayIds) {
            // if the node is not part of the way it was initially in anymore, that's fine
            val way = waysById[wayId] ?: continue

            val nodeIds = way.nodeIds.filter { it != currentNode.id }

            editedWays.add(way.copy(nodeIds = nodeIds, timestampEdited = nowAsEpochMilliseconds()))
        }

        return MapDataChanges(modifications = editedWays, deletions = listOf(currentNode))
    }
}
