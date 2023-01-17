package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.ElementEditKey
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

data class ElementEdit(
    /** (row) id of the edit. 0 if not inserted into DB yet */
    var id: Long,

    /** edit type associated with the edit. This is used to sort this edit into a changeset
     *  associated with the quest type. A changeset gets its comment from the quest type */
    val type: ElementEditType,

    /** element type this edit refers to */
    val elementType: ElementType,
    /** element id this edit refers to. Unlike element.id, this field may change when the OSM API
     *  returns element ID updates. Use 0 for newly created elements */
    val elementId: Long,
    /** original element this edit was made on */
    val originalElement: Element,
    /** original geometry of element this edit refers to */
    val originalGeometry: ElementGeometry,

    /** what is the source of this edit? (Currently, always "survey"). Used for the changeset
     *  field "source". Edits with different sources are not put into the same changeset */
    val source: String,

    /** timestamp when this edit was made. Used to order the edits in a queue */
    override val createdTimestamp: Long,

    /** whether this edit has been uploaded already */
    override val isSynced: Boolean,

    /** The action to perform */
    val action: ElementEditAction
) : Edit {
    override val isUndoable: Boolean get() = !isSynced || action is IsActionRevertable
    override val key: ElementEditKey get() = ElementEditKey(id)

    /** (center) position of (the element) the edit refers to. Used for local statistics, i.e. to
     *  ascertain in which country the edit has been made */
    override val position: LatLon get() = originalGeometry.center
}
