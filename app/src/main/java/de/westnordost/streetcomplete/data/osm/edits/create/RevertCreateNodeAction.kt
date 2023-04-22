package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.upload.ConflictException
import kotlinx.serialization.Serializable

/** Action reverts creation of a (free-floating) node.
 *
 *  If the node has been touched at all in the meantime (node moved or tags changed), there'll be
 *  a conflict. */
@Serializable
data class RevertCreateNodeAction(
    private val originalNode: Node
) : ElementEditAction, IsRevertAction {

    override val elementKeys get() = listOf(originalNode.key)

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
        if (mapDataRepository.getWaysForNode(currentNode.id).isNotEmpty()) {
            throw ConflictException("Node is now also part of a way")
        }

        return MapDataChanges(deletions = listOf(currentNode))
    }
}
