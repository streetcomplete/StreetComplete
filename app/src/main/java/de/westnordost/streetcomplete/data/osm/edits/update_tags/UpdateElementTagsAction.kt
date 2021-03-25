package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.data.osm.edits.*
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry

/** Action that updates the tags on an element.
 *
 *  The tag updates are passed in as a diff to be more robust when handling conflicts.
 *
 *  For stricter conflict handling, the quest type in which context this edit action was created
 *  is also passed in: If the updated element is not applicable to the quest type anymore, it
 *  is considered a conflict.
 *
 *  The original element is passed in in order to decide if an updated element is still compatible
 *  with the action: Basically, if the geometry changed significantly, there is a possibility that
 *  the tag update made may not be correct anymore, so that is considered a conflict.
 *  */
class UpdateElementTagsAction(
    private val spatialPartsOfOriginalElement: SpatialPartsOfElement,
    val changes: StringMapChanges,
    private val questType: OsmElementQuestType<*>?
): ElementEditAction, IsActionRevertable {

    override val newElementsCount get() = NewElementsCount(0,0,0)

    override fun createUpdates(
        element: Element,
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): Collection<Element> {

        /* if after updating to the new version of the element, the quest is not applicable to
           the element anymore, drop it (#720) */
        if (questType?.isApplicableTo(element) == false) {
            throw ConflictException("Quest no longer applicable to the element")
        }

        if (isGeometrySubstantiallyDifferent(spatialPartsOfOriginalElement, element)) {
            throw ConflictException("Element geometry changed substantially")
        }

        return listOf(element.changesApplied(changes))
    }

    override fun createReverted(): ElementEditAction =
        RevertUpdateElementTagsAction(spatialPartsOfOriginalElement, changes.reversed())

    fun isReverseOf(other: UpdateElementTagsAction): Boolean = changes.reversed() == other.changes

    /** This class is as such not serializable because of that "questType" parameter which should
     *  be serialized to a string and deserialized using the QuestTypeRegistry */
    fun createSerializable() = Serializable(
        spatialPartsOfOriginalElement, changes, questType?.let { it::class.simpleName }
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UpdateElementTagsAction) return false
        return changes == other.changes
            && spatialPartsOfOriginalElement == other.spatialPartsOfOriginalElement
            && questType == other.questType
    }

    override fun hashCode(): Int {
        var result = spatialPartsOfOriginalElement.hashCode()
        result = 31 * result + changes.hashCode()
        result = 31 * result + (questType?.hashCode() ?: 0)
        return result
    }

    data class Serializable(
        private val spatialPartsOfOriginalElement: SpatialPartsOfElement,
        private val changes: StringMapChanges,
        private val questTypeName: String?
    ) {
        fun createObject(questTypeRegistry: QuestTypeRegistry) = UpdateElementTagsAction(
            spatialPartsOfOriginalElement,
            changes,
            questTypeName?.let { questTypeRegistry.getByName(it)!! as OsmElementQuestType<*> }
        )
    }
}
