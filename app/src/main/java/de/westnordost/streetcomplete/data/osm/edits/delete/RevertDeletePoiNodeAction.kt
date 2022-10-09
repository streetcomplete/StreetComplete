package de.westnordost.streetcomplete.data.osm.edits.delete

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.edits.NewElementsCount
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.upload.ConflictException
import kotlinx.serialization.Serializable
import java.lang.System.currentTimeMillis

/** Action that restores a POI node to the previous state before deletion/clearing of tags
 */
@Serializable
data class RevertDeletePoiNodeAction(
    private val originalNode: Node
) : ElementEditAction, IsRevertAction {

    /** No "new" elements are created, instead, an old one is being revived */
    override val newElementsCount get() = NewElementsCount(0, 0, 0)

    override fun createUpdates(
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val newVersion = originalNode.version + 1
        val currentNode = mapDataRepository.getNode(originalNode.id)

        // already has been restored apparently
        if (currentNode != null && currentNode.version > newVersion) {
            throw ConflictException("Element has been restored already")
        }

        val restoredNode = originalNode.copy(
            version = newVersion,
            timestampEdited = currentTimeMillis()
        )
        return MapDataChanges(modifications = listOf(restoredNode))
    }
}
