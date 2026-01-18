package de.westnordost.streetcomplete.data.atp.atpquests

import de.westnordost.streetcomplete.data.atp.AtpEntry
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.QuestHiddenKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.AtpQuestKey

data class AtpQuestHidden (
    val atpEntry: AtpEntry,
    override val createdTimestamp: Long
) : Edit {
    val questKey get() = AtpQuestKey(atpEntry.id)
    override val key: QuestHiddenKey get() = QuestHiddenKey(questKey)
    override val isUndoable: Boolean get() = true
    override val position: LatLon get() = atpEntry.position
    override val isSynced: Boolean? get() = null
}
