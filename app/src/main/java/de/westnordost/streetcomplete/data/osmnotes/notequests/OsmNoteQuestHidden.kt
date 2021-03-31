package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.data.edithistory.Edit

data class OsmNoteQuestHidden(
    val note: Note,
    override val createdTimestamp: Long
) : Edit {
    override val isUndoable: Boolean get() = true
    override val position: LatLon get() = note.position
    override val isSynced: Boolean? get() = null
}
