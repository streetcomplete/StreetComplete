package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.edithistory.Edit

data class OsmQuestHidden(
    val elementType: Element.Type,
    val elementId: Long,
    val questType: OsmElementQuestType<*>,
    override val position: LatLon,
    override val createdTimestamp: Long
) : Edit {
    override val isUndoable: Boolean get() = true
}
