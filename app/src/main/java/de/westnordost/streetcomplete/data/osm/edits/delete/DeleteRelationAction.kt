package de.westnordost.streetcomplete.data.osm.edits.delete

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsActionRevertable
import de.westnordost.streetcomplete.data.osm.edits.update_tags.isGeometrySubstantiallyDifferent
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
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
data class DeleteRelationAction(
    val originalRelation: Relation
) : ElementEditAction, IsActionRevertable {

    override val elementKeys get() = listOf(originalRelation.key)

    override fun idsUpdatesApplied(updatedIds: Map<ElementKey, Long>) = copy(
        originalRelation = originalRelation.copy(id = updatedIds[originalRelation.key] ?: originalRelation.id)
    )

    override fun createUpdates(
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val currentRelation = mapDataRepository.getRelation(originalRelation.id)
            ?: throw ConflictException("Element deleted")

        if (isGeometrySubstantiallyDifferent(originalRelation, currentRelation)) {
            throw ConflictException("Element geometry changed substantially")
        }

        // delete relation
        val relations = mapDataRepository.getRelationsForRelation(currentRelation.id)
        if (relations.isNotEmpty()) throw ConflictException("Deleting relation that is member of other relation currently not supported")
        return MapDataChanges(deletions = listOf(currentRelation))
    }

    override fun createReverted(idProvider: ElementIdProvider) =
        RevertDeleteRelationAction(originalRelation)
}
