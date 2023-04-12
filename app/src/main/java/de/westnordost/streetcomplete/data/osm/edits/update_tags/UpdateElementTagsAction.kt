package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsActionRevertable
import de.westnordost.streetcomplete.data.osm.edits.NewElementsCount
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.upload.ConflictException
import kotlinx.serialization.Serializable

/** Action that updates the tags on an element.
 *
 *  The tag updates are passed in as a diff to be more robust when handling conflicts.
 *
 *  The original element is passed in in order to decide if an updated element is still compatible
 *  with the action: Basically, if the geometry changed significantly, there is a possibility that
 *  the tag update made may not be correct anymore, so that is considered a conflict.
 *  */
@Serializable
data class UpdateElementTagsAction(
    val originalElement: Element,
    val changes: StringMapChanges
) : ElementEditAction, IsActionRevertable {

    override val newElementsCount get() = NewElementsCount(0, 0, 0)

    override val elementKeys get() = listOf(ElementKey(originalElement.type, originalElement.id))

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

    override fun createReverted(idProvider: ElementIdProvider): ElementEditAction =
        RevertUpdateElementTagsAction(originalElement, changes.reversed())

    fun isReverseOf(other: UpdateElementTagsAction): Boolean =
        changes.reversed() == other.changes
}
