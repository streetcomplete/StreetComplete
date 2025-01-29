package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.QuestHiddenKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey

data class OsmNoteQuestHidden(
    val note: Note,
    override val createdTimestamp: Long
) : Edit {
    val questKey get() = OsmNoteQuestKey(note.id)
    override val key: QuestHiddenKey get() = QuestHiddenKey(questKey)
    override val isUndoable: Boolean get() = true
    override val position: LatLon get() = note.position
    override val isSynced: Boolean? get() = null
}
