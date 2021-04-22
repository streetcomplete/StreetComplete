package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.OsmQuestHiddenKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey

data class OsmQuestHidden(
    val elementType: Element.Type,
    val elementId: Long,
    val questType: OsmElementQuestType<*>,
    override val position: LatLon,
    override val createdTimestamp: Long
) : Edit {
    val questKey get() = OsmQuestKey(elementType, elementId, questType::class.simpleName!!)
    override val key: OsmQuestHiddenKey get() = OsmQuestHiddenKey(questKey)
    override val isUndoable: Boolean get() = true
    override val isSynced: Boolean? get() = null
}
