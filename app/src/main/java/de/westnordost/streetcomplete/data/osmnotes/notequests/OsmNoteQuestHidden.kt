package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.OsmNoteQuestHiddenKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey

data class OsmNoteQuestHidden(
    val note: Note,
    override val createdTimestamp: Long
) : Edit {
    override val key: OsmNoteQuestHiddenKey get() = OsmNoteQuestHiddenKey(OsmNoteQuestKey(note.id))
    override val isUndoable: Boolean get() = true
    override val position: LatLon get() = note.position
    override val isSynced: Boolean? get() = null
}
