package de.westnordost.streetcomplete.data.osm.edits.move

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsActionRevertable
import de.westnordost.streetcomplete.data.osm.edits.NewElementsCount
import de.westnordost.streetcomplete.data.osm.edits.update_tags.isGeometrySubstantiallyDifferent
import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.serialization.Serializable

/** Action that moves a node. */
@Serializable
data class MoveNodeAction(
    val originalNode: Node,
    val position: LatLon
) : ElementEditAction, IsActionRevertable {

    override val newElementsCount get() = NewElementsCount(0, 0, 0)

    override val elementKeys get() = listOf(ElementKey(originalNode.type, originalNode.id))

    override fun idsUpdatesApplied(idUpdates: Collection<ElementIdUpdate>): ElementEditAction {
        val newId = idUpdates.find {
            it.elementType == originalNode.type && it.oldElementId == originalNode.id
        }?.newElementId ?: return this

        return copy(originalNode = originalNode.copy(id = newId))
    }

    override fun createUpdates(
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val currentNode = mapDataRepository.getNode(originalNode.id)
            ?: throw ConflictException("Element deleted")
        val node = currentNode as? Node ?: throw ConflictException("Element deleted")
        if (isGeometrySubstantiallyDifferent(originalNode, currentNode)) {
            throw ConflictException("Element geometry changed substantially")
        }
        return MapDataChanges(modifications = listOf(node.copy(
            position = position,
            timestampEdited = nowAsEpochMilliseconds()
        )))
    }

    override fun createReverted(idProvider: ElementIdProvider) =
        RevertMoveNodeAction(originalNode)
}
