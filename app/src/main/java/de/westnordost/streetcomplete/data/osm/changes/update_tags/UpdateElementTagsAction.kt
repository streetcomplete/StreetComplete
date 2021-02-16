package de.westnordost.streetcomplete.data.osm.changes.update_tags

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.data.osm.changes.*
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.upload.ElementConflictException
import de.westnordost.streetcomplete.ktx.copy
import de.westnordost.streetcomplete.util.distanceTo

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
    private val originalElement: Element,
    private val changes: StringMapChanges,
    private val questType: OsmElementQuestType<*>?
): ElementEditAction, IsUndoable, IsRevertable {

    override val newNewElementsCount get() = NewElementsCount(0,0,0)

    override fun createUpdates(
        element: Element,
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): Collection<Element> {

        /* if after updating to the new version of the element, the quest is not applicable to
           the element anymore, drop it (#720) */
        if (questType?.isApplicableTo(element) == false) {
            throw ElementConflictException("Quest no longer applicable to the element")
        }

        if (isGeometrySubstantiallyDifferent(originalElement, element)) {
            throw ElementConflictException("Element geometry changed substantially")
        }

        return listOf(element.changesApplied(changes))
    }

    override fun createReverted(): ElementEditAction =
        RevertUpdateElementTagsAction(originalElement, changes.reversed())
}

/** Contains the information necessary to apply a revert of tag changes made on an element */
class RevertUpdateElementTagsAction(
    private val originalElement: Element,
    private val changes: StringMapChanges
): ElementEditAction, IsRevert {

    override val newNewElementsCount get() = NewElementsCount(0,0,0)

    override fun createUpdates(
        element: Element,
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): Collection<Element> {

        if (isGeometrySubstantiallyDifferent(originalElement, element)) {
            throw ElementConflictException("Element geometry changed substantially")
        }

        return listOf(element.changesApplied(changes))
    }
}

private fun Element.changesApplied(changes: StringMapChanges): Element {
    val copy = this.copy()
    try {
        if (copy.tags == null) throw ElementConflictException("The element has no tags")
        changes.applyTo(copy.tags)
    } catch (e: IllegalStateException) {
        throw ElementConflictException("Conflict while applying the changes")
    } catch (e: IllegalArgumentException) {
        /* There is a max key/value length limit of 255 characters in OSM. If we reach this
           point, it means the UI did permit an input of more than that. So, we have to catch
           this here latest.
           The UI should prevent this in the first place, at least
           for free-text input. For structured input, like opening hours, it is another matter
           because it's awkward to explain to a non-technical user this technical limitation

           See also https://github.com/openstreetmap/openstreetmap-website/issues/2025
          */
        throw ElementConflictException("Key or value is too long")
    }
    return copy
}

private fun isGeometrySubstantiallyDifferent(element: Element, newElement: Element) =
    when (element) {
        is Node -> isNodeGeometrySubstantiallyDifferent(element, newElement as Node)
        is Way -> isWayGeometrySubstantiallyDifferent(element, newElement as Way)
        is Relation -> isRelationGeometrySubstantiallyDifferent(element, newElement as Relation)
        else -> false
    }

private fun isNodeGeometrySubstantiallyDifferent(node: Node, newNode: Node) =
    /* Moving the node a distance beyond what would pass as adjusting the position within a
       building counts as substantial change. Also, the maximum distance should be not (much)
       bigger than the usual GPS inaccuracy in the city. */
    node.position.distanceTo(newNode.position) > 20

private fun isWayGeometrySubstantiallyDifferent(way: Way, newWay: Way) =
    /* if the first or last node is different, it means that the way has either been extended or
       shortened at one end, which is counted as being substantial:
       If for example the surveyor has been asked to determine something for a certain way
       and this way is now longer, his answer does not apply to the whole way anymore, so that
       is an unsolvable conflict. */
    way.nodeIds.firstOrNull() != newWay.nodeIds.firstOrNull() ||
        way.nodeIds.lastOrNull() != newWay.nodeIds.lastOrNull()

private fun isRelationGeometrySubstantiallyDifferent(relation: Relation, newRelation: Relation) =
    /* a relation is counted as substantially different, if any member changed, even if just
       the order changed because for some relations, the order has an important meaning */
    relation.members != newRelation.members
