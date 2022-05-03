package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.OsmQuestHiddenKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.OsmQuestKey

data class OsmQuestHidden(
    val elementType: ElementType,
    val elementId: Long,
    val questType: OsmElementQuestType<*>,
    override val position: LatLon,
    override val createdTimestamp: Long
) : Edit {
    val questKey get() = OsmQuestKey(elementType, elementId, questType.name)
    override val key: OsmQuestHiddenKey get() = OsmQuestHiddenKey(questKey)
    override val isUndoable: Boolean get() = true
    override val isSynced: Boolean? get() = null
}
