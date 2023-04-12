package de.westnordost.streetcomplete.data.osm.edits.move

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.serialization.Serializable

/** Action reverts moving a node. */
@Serializable
data class RevertMoveNodeAction(
    val originalNode: Node,
) : ElementEditAction, IsRevertAction {

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
        return MapDataChanges(modifications = listOf(currentNode.copy(
            position = originalNode.position,
            timestampEdited = nowAsEpochMilliseconds()
        )))
    }
}
