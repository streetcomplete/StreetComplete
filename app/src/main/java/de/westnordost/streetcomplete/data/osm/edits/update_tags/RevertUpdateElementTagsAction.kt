package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.edits.NewElementsCount
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.util.ktx.copy
import kotlinx.serialization.Serializable

/** Contains the information necessary to apply a revert of tag changes made on an element */
@Serializable
data class RevertUpdateElementTagsAction(
    private val originalElement: Element,
    private val changes: StringMapChanges
) : ElementEditAction, IsRevertAction {

    override val newElementsCount get() = NewElementsCount(0, 0, 0)

    override val elementKeys get() = listOf(originalElement.key)

    override fun idsUpdatesApplied(idUpdates: Collection<ElementIdUpdate>): ElementEditAction {
        val newId = idUpdates.find {
            it.elementType == originalElement.type && it.oldElementId == originalElement.id
        }?.newElementId ?: return this

        return copy(originalElement = originalElement.copy(id = newId))
    }

    override fun createUpdates(
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val currentElement = mapDataRepository.get(originalElement.type, originalElement.id)
            ?: throw ConflictException("Element deleted")

        if (isGeometrySubstantiallyDifferent(originalElement, currentElement)) {
            throw ConflictException("Element geometry changed substantially")
        }

        return MapDataChanges(modifications = listOf(currentElement.changesApplied(changes)))
    }
}
