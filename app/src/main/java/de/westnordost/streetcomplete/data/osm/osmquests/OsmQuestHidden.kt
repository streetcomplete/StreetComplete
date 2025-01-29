package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.QuestHiddenKey
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.OsmQuestKey

data class OsmQuestHidden(
    val elementType: ElementType,
    val elementId: Long,
    val questType: OsmElementQuestType<*>,
    val geometry: ElementGeometry,
    override val createdTimestamp: Long
) : Edit {
    override val position: LatLon get() = geometry.center
    val questKey get() = OsmQuestKey(elementType, elementId, questType.name)
    override val key: QuestHiddenKey get() = QuestHiddenKey(questKey)
    override val isUndoable: Boolean get() = true
    override val isSynced: Boolean? get() = null
}
