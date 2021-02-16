package de.westnordost.streetcomplete.data.osm.changes

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType

data class ElementEdit(
    /** (row) id of the edit. Null if not inserted into DB yet */
    var id: Long?,

    /** quest type associated with the edit. This is used to sort this edit into a changeset
     *  associated with the quest type. A changeset gets its commit message from the quest type */
    val questType: OsmElementQuestType<*>,

    /** element type this edit refers to */
    val elementType: Element.Type,
    /** element id this edit refers to */
    val elementId: Long,

    /** what is the source of this edit? (Currently, always "survey"). Used for the changeset
     *  field "source". Edits with different sources are not put into the same changeset */
    val source: String,

    /** (center) position of (the element) the edit refers to. Used for local statistics, i.e. to
     *  ascertain in which country the edit has been made */
    val position: LatLon,

    /** timestamp when this edit was made. Used to order the (unsynced) edits in a queue */
    val createdTimestamp: Long,

    /** whether this edit has been uploaded already */
    val isSynced: Boolean,

    /** The action to perform */
    val action: ElementEditAction
)
