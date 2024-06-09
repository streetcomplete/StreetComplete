package de.westnordost.streetcomplete.data.osm.edits.delete

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.edits.NewElementsCount
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.serialization.Serializable

/** Action that restores a POI node to the previous state before deletion/clearing of tags
 */
@Serializable
data class RevertDeleteRelationAction(
    val originalRelation: Relation
) : ElementEditAction, IsRevertAction {

    /** No "new" elements are created, instead, an old one is being revived */
    override val newElementsCount get() = NewElementsCount(0, 0, 0)

    override val elementKeys get() = listOf(originalRelation.key)

    override fun idsUpdatesApplied(updatedIds: Map<ElementKey, Long>) = copy(
        originalRelation = originalRelation.copy(id = updatedIds[originalRelation.key] ?: originalRelation.id)
    )

    override fun createUpdates(
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val newVersion = originalRelation.version + 1
        val currentRelation = mapDataRepository.getRelation(originalRelation.id)

        // already has been restored apparently
        if (currentRelation != null && currentRelation.version > newVersion) {
            throw ConflictException("Element has been restored already")
        }

        val restoredRelation = originalRelation.copy(
            version = newVersion,
            timestampEdited = nowAsEpochMilliseconds()
        )
        return MapDataChanges(modifications = listOf(restoredRelation))
    }
}
