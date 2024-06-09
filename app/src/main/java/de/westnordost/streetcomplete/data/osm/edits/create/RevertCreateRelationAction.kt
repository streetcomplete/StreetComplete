package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import kotlinx.serialization.Serializable

@Serializable
data class RevertCreateRelationAction(
    val originalRelation: Relation,
) : ElementEditAction, IsRevertAction {

    override val elementKeys get() = listOf(originalRelation.key)

    override fun idsUpdatesApplied(updatedIds: Map<ElementKey, Long>) = copy(
        originalRelation = originalRelation.copy(id = updatedIds[originalRelation.key] ?: originalRelation.id))

    override fun createUpdates(
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val currentRelation = mapDataRepository.getRelation(originalRelation.id)
            ?: throw ConflictException("Element deleted")

        if (originalRelation.members != currentRelation.members) {
            throw ConflictException("Relation members changed")
        }

        if (originalRelation.tags != currentRelation.tags) {
            throw ConflictException("Some tags have already been changed")
        }

        if (mapDataRepository.getRelationsForNode(currentRelation.id).isNotEmpty()) {
            throw ConflictException("Relation is now member of a relation")
        }

        return MapDataChanges(deletions = listOf(currentRelation))
    }
}
