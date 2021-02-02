package de.westnordost.streetcomplete.data.osm.osmquest.changes

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.upload.HasElementTagChanges
import de.westnordost.streetcomplete.data.osm.upload.UploadableInChangeset

/** Contains the information necessary to apply changes made by answering an osm quest */
class OsmElementTagChanges(
    val id: Long?,
    override val osmElementQuestType: OsmElementQuestType<*>,
    override val elementType: Element.Type,
    override val elementId: Long,
    override val changes: StringMapChanges,
    override val source: String,
    override val position: LatLon,
    val isRevert: Boolean
) : UploadableInChangeset, HasElementTagChanges {

    /* change of the revert is exactly the opposite of what the quest would normally change and the
       element ergo has the changes already applied that a normal quest would add */
    override fun isApplicableTo(element: Element) : Boolean? {
        return if (!isRevert) osmElementQuestType.isApplicableTo(element) else true
    }
}
