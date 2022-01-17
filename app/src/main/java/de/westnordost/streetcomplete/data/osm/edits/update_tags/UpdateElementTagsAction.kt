package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsActionRevertable
import de.westnordost.streetcomplete.data.osm.edits.NewElementsCount
import de.westnordost.streetcomplete.data.osm.mapdata.Element
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
data class UpdateElementTagsAction(val changes: StringMapChanges): ElementEditAction, IsActionRevertable {

    override val newElementsCount get() = NewElementsCount(0,0,0)

    override fun createUpdates(
        originalElement: Element,
        element: Element?,
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        if (element == null) throw ConflictException("Element deleted")
        if (isGeometrySubstantiallyDifferent(originalElement, element)) {
            throw ConflictException("Element geometry changed substantially")
        }

        return MapDataChanges(modifications = listOf(element.changesApplied(changes)))
    }

    override fun createReverted(): ElementEditAction =
        RevertUpdateElementTagsAction(changes.reversed())

    fun isReverseOf(other: UpdateElementTagsAction): Boolean =
        changes.reversed() == other.changes
}
