package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.notes.Note

interface NoteSource {

    /** Interface to be notified of new notes in the given bbox */
    interface NoteUpdatesListener {
        fun onUpdated(note: Note)
        fun onDeleted(noteId: Long)
        fun onUpdatedForBBox(bbox: BoundingBox, notes: Collection<Note>)
    }

    /** get note of the given id */
    fun get(noteId: Long): Note?

    /** get the positions of all notes in the given bounding box */
    fun getAllPositions(bbox: BoundingBox): List<LatLon>

    /** get all notes in the given bounding box */
    fun getAll(bbox: BoundingBox): List<Note>

    fun addNoteUpdatesListener(listener: NoteUpdatesListener)
    fun removeNoteUpdatesListener(listener: NoteUpdatesListener)

}
