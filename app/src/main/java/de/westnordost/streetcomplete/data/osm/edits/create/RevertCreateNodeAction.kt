package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.upload.ConflictException
import kotlinx.serialization.Serializable

/** Action reverts creation of a (free-floating) node.
 *
 *  If the node has been touched at all in the meantime (node moved or tags changed), there'll be
 *  a conflict. */
@Serializable
object RevertCreateNodeAction : ElementEditAction, IsRevertAction {

    override fun createUpdates(
        originalElement: Element,
        element: Element?,
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val node = element as? Node ?: throw ConflictException("Element deleted")
        val originalNode = originalElement as? Node ?: throw IllegalArgumentException()

        if (node.tags != originalNode.tags) {
            throw ConflictException("Element tags changed")
        }
        if (node.position != originalNode.position) {
            throw ConflictException("Element position changed")
        }
        return MapDataChanges(deletions = listOf(node))
    }
}
