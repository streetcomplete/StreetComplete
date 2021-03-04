package de.westnordost.streetcomplete.data.osm.edits.delete

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.ktx.copy

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
 *
 *  The original node version is passed because if the node changed in the meantime, it should be
 *  considered as a conflict. For example,
 *  the node may have been moved to the real location of the POI, the tagging may have been
 *  corrected to reflect what the POI really is, it may have been re-purposed to be something
 *  else now, etc.
 *  */
class DeletePoiNodeAction(
    private val originalNodeVersion: Int,
) : ElementEditAction {

    override fun createUpdates(
        element: Element,
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): Collection<Element> {
        val node = element.copy() as OsmNode

        if (node.version > originalNodeVersion) throw ConflictException()

        // delete free-floating node
        if (mapDataRepository.getWaysForNode(node.id).isEmpty() &&
            mapDataRepository.getRelationsForNode(node.id).isEmpty()) {
            node.isDeleted = true
        }
        // if it is a vertex in a way or has a role in a relation: just clear the tags then
        else {
            node.tags.clear()
        }

        return listOf(node)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeletePoiNodeAction) return false
        return originalNodeVersion == other.originalNodeVersion
    }

    override fun hashCode(): Int = originalNodeVersion
}
