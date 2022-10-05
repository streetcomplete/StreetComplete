package de.westnordost.streetcomplete.data.othersource

import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.OtherSourceQuestHiddenKey
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.OtherSourceQuestKey
import de.westnordost.streetcomplete.data.quest.Quest

data class OtherSourceQuest(
    /** Each quest must be uniquely identified by the [id] and [source] */
    val id: String,
    override val geometry: ElementGeometry,
    override val type: OtherSourceQuestType,
) : Quest {
    override val key by lazy { OtherSourceQuestKey(id, source) }
    override val markerLocations: Collection<LatLon> get() = listOf(geometry.center)
    override val position: LatLon get() = geometry.center
    val source get() = type.source
}

data class OtherSourceQuestHidden(
    val id: String,
    val questType: OtherSourceQuestType,
    override val position: LatLon,
    override val createdTimestamp: Long
) : Edit {
    val questKey get() = OtherSourceQuestKey(id, questType.source)
    override val key: OtherSourceQuestHiddenKey get() = OtherSourceQuestHiddenKey(questKey)
    override val isUndoable: Boolean get() = true
    override val isSynced: Boolean? get() = null
}

