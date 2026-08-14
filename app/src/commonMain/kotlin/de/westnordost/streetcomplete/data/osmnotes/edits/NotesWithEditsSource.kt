package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.Note

interface NotesWithEditsSource {
    /** Interface to be notified of new notes, updated notes and notes that have been deleted,
    this includes not yet synced answers in addition to what NoteController would report
     */
    interface Listener {
        fun onUpdated(added: Collection<Note>, updated: Collection<Note>, deleted: Collection<Long>)

        fun onCleared()
    }

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)


    fun get(noteId: Long): Note?

    fun getAllPositions(bbox: BoundingBox): List<LatLon>

    fun getAll(bbox: BoundingBox): Collection<Note>

    fun getAll(noteIds: Collection<Long>): Collection<Note>
}
