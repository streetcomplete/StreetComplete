package de.westnordost.streetcomplete.data.edithistory

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import kotlinx.serialization.Serializable

interface Edit {
    val key: EditKey
    val createdTimestamp: Long
    val isUndoable: Boolean
    val position: LatLon
    val isSynced: Boolean?
}

@Serializable
sealed class EditKey

@Serializable
data class ElementEditKey(val id: Long) : EditKey()
@Serializable
data class NoteEditKey(val id: Long) : EditKey()
@Serializable
data class OsmQuestHiddenKey(val osmQuestKey: OsmQuestKey) : EditKey()
@Serializable
data class OsmNoteQuestHiddenKey(val osmNoteQuestKey: OsmNoteQuestKey) : EditKey()
