package de.westnordost.streetcomplete.data.osm.changes

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import java.lang.System.currentTimeMillis

sealed class OsmElementChange {
    /** (row) id of the change. Null if not inserted into DB yet */
    abstract var id: Long?

    /** quest type associated with the change. This is used to sort this change into a changeset
     *  associated with the quest type. A changeset gets its commit message from the quest type */
    abstract val questType: OsmElementQuestType<*>

    /** element type this change refers to */
    abstract val elementType: Element.Type
    /** element id this change refers to */
    abstract val elementId: Long

    /** what is the source of this change? (Currently, always "survey"). Used for the changeset
     *  field "source". Changes with different sources are not put into the same changeset */
    abstract val source: String

    /** (center) position of (the element) the change refers to. Used for local statistics, i.e. to
     *  ascertain in which country the change has been made */
    abstract val position: LatLon

    /** timestamp when this change was made. Used to order the (unsynced) changed in a queue */
    abstract val createdTimestamp: Long

    /** whether this change has been uploaded already */
    abstract val isSynced: Boolean

    /** the number of new elements this change creates. This needs to be stated in advance so that
     *  negative element ids can be reserved for this change: The same element id needs to be used
     *  when applying the change locally and when uploading the change */
    open val newElementsCount: ElementsCount? = null
}

data class ElementsCount(val nodes: Int, val ways: Int, val relations: Int)

interface IsUndoable
interface IsRevertable {
    fun createReverted(): OsmElementChange
}
interface IsRevert

/** Contains the information necessary to apply tag changes made on an element */
data class ChangeOsmElementTags(
    override var id: Long?,
    override val questType: OsmElementQuestType<*>,
    override val elementType: Element.Type,
    override val elementId: Long,
    override val source: String,
    override val position: LatLon,
    override val createdTimestamp: Long = currentTimeMillis(),
    override val isSynced: Boolean = false,
    override val changes: StringMapChanges
): OsmElementChange(), IsUndoable, IsRevertable, HasElementTagChanges {

    override fun isApplicableTo(element: Element) : Boolean? = questType.isApplicableTo(element)

    override fun createReverted() = RevertChangeOsmElementTags(
        id, questType, elementType, elementId, source, position, changes = changes.reversed()
    )
}

/** Contains the information necessary to revert tag changes made on an element */
class RevertChangeOsmElementTags(
    override var id: Long?,
    override val questType: OsmElementQuestType<*>,
    override val elementType: Element.Type,
    override val elementId: Long,
    override val source: String,
    override val position: LatLon,
    override val createdTimestamp: Long = currentTimeMillis(),
    override val isSynced: Boolean = false,
    override val changes: StringMapChanges
): OsmElementChange(), IsRevert, HasElementTagChanges {

    override fun isApplicableTo(element: Element): Boolean? = true
}

/** Contains all necessary information to delete an OSM element as a result of a quest answer */
class DeleteOsmElement(
    override var id: Long?,
    override val questType: OsmElementQuestType<*>,
    override val elementType: Element.Type,
    override val elementId: Long,
    override val source: String,
    override val position: LatLon,
    override val createdTimestamp: Long = currentTimeMillis(),
    override val isSynced: Boolean = false,
) : OsmElementChange()

/** Contains all necessary information about where to perform a split of a certain OSM way.
 *
 *  It is assigned to a quest and source because it should be put in the same changeset as the
 *  quest normally would, so that the context in which a way was split is clear for people doing
 *  QA.
 *
 *  Keeping the split positions as a lat-lon position because it more robust when handling
 *  conflicts than if the split positions were kept as node ids or node indices of the way.
 *  */
class SplitOsmWay(
    override var id: Long?,
    override val questType: OsmElementQuestType<*>,
    override val elementType: Element.Type,
    override val elementId: Long,
    override val source: String,
    override val position: LatLon,
    override val createdTimestamp: Long = currentTimeMillis(),
    override val isSynced: Boolean = false,
    val splits: ArrayList<SplitPolylineAtPosition>
) : OsmElementChange() {

    override val newElementsCount get() = ElementsCount(
        nodes = splits.filterIsInstance<SplitAtLinePosition>().size,
        ways = splits.size,
        relations = 0
    )
}
