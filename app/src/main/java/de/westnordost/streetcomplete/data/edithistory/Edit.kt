package de.westnordost.streetcomplete.data.edithistory

import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey

// TODO make sealed as soon as kotlin supports this (1.5)
interface Edit {
    val key: EditKey
    val createdTimestamp: Long
    val isUndoable: Boolean
    val position: LatLon
    val isSynced: Boolean?
}

sealed class EditKey

data class ElementEditKey(val id: Long): EditKey()
data class NoteEditKey(val id: Long): EditKey()
data class OsmQuestHiddenKey(val osmQuestKey: OsmQuestKey): EditKey()
data class OsmNoteQuestHiddenKey(val osmNoteQuestKey: OsmNoteQuestKey): EditKey()
