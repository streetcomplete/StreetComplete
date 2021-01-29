package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.notes.Note

interface NoteSource {

    /** Interface to be notified of new notes, updated notes and notes that have been deleted */
    interface Listener {
        fun onUpdated(added: Collection<Note>, updated: Collection<Note>, deleted: Collection<Long>)
    }

    /** get note with the given id */
    fun get(noteId: Long): Note?

    /** get the positions of all notes in the given bounding box */
    fun getAllPositions(bbox: BoundingBox): List<LatLon>

    /** get all notes in the given bounding box */
    fun getAll(bbox: BoundingBox): List<Note>

    /** get all notes with the given ids */
    fun getAll(noteIds: Collection<Long>): List<Note>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)

}
