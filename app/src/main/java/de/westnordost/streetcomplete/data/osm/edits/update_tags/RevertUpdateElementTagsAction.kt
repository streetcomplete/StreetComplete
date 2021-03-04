package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.edits.NewElementsCount
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.upload.ConflictException

/** Contains the information necessary to apply a revert of tag changes made on an element */
class RevertUpdateElementTagsAction(
    private val spatialPartsOfOriginalElement: SpatialPartsOfElement,
    private val changes: StringMapChanges
): ElementEditAction, IsRevertAction {

    override val newElementsCount get() = NewElementsCount(0,0,0)

    override fun createUpdates(
        element: Element,
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): Collection<Element> {

        if (isGeometrySubstantiallyDifferent(spatialPartsOfOriginalElement, element)) {
            throw ConflictException("Element geometry changed substantially")
        }

        return listOf(element.changesApplied(changes))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RevertUpdateElementTagsAction) return false
        return changes == other.changes &&
            spatialPartsOfOriginalElement == other.spatialPartsOfOriginalElement
    }

    override fun hashCode(): Int {
        var result = spatialPartsOfOriginalElement.hashCode()
        result = 31 * result + changes.hashCode()
        return result
    }
}
