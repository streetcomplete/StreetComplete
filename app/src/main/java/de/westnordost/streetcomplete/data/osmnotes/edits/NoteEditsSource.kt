package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

interface NoteEditsSource {

    interface Listener {
        fun onAddedEdit(edit: NoteEdit)
        fun onSyncedEdit(edit: NoteEdit)
        fun onDeletedEdits(edits: List<NoteEdit>)
    }

    /** Count of unsynced a.k.a to-be-uploaded edits */
    fun getUnsyncedCount(): Int

    fun getAllUnsynced(): List<NoteEdit>

    fun getAllUnsynced(bbox: BoundingBox): List<NoteEdit>

    fun getAllUnsyncedForNote(noteId: Long): List<NoteEdit>

    fun getAllUnsyncedForNotes(noteIds: Collection<Long>): List<NoteEdit>

    fun getAllUnsyncedPositions(bbox: BoundingBox): List<LatLon>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)

}
