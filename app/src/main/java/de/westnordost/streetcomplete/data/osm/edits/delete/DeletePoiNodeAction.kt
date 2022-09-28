package de.westnordost.streetcomplete.data.osm.edits.delete

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsActionRevertable
import de.westnordost.streetcomplete.data.osm.edits.update_tags.isGeometrySubstantiallyDifferent
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.upload.ConflictException
import kotlinx.serialization.Serializable

/** Action that deletes a POI node.
 *
 *  This is different from a generic element deletion seen in other editors in as ...
 *
 *  1. it only works on nodes. This is mainly to reduce complexity, because when deleting ways, it
 *     is expected to implicitly also delete all nodes of that way that are not part of any other
 *     way (or relation).
 *
 *  2. if that node is a vertex in a way or has a role in a relation, the node is not deleted but
 *     just "degraded" to be a vertex, i.e. the tags are cleared.
 *  */
@Serializable
object DeletePoiNodeAction : ElementEditAction, IsActionRevertable {

    override fun createUpdates(
        originalElement: Element,
        element: Element?,
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val node = element as? Node ?: throw ConflictException("Element deleted")
        if (isGeometrySubstantiallyDifferent(originalElement, element)) {
            throw ConflictException("Element geometry changed substantially")
        }

        // delete free-floating node
        return if (
            mapDataRepository.getWaysForNode(node.id).isEmpty()
            && mapDataRepository.getRelationsForNode(node.id).isEmpty()
        ) {
            MapDataChanges(deletions = listOf(node))
        }
        // if it is a vertex in a way or has a role in a relation: just clear the tags then
        else {
            MapDataChanges(modifications = listOf(node.copy(
                tags = emptyMap(),
                timestampEdited = System.currentTimeMillis()
            )))
        }
    }

    override fun createReverted(): ElementEditAction = RevertDeletePoiNodeAction
}
