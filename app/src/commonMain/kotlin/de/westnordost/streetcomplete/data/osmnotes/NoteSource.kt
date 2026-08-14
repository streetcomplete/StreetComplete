package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

interface NoteSource {
    /** Interface to be notified of new notes, updated notes and notes that have been deleted,
     *  to include also not yet synced changes use NotesWithEditsSource
     */
    interface Listener {
        /** called when a number of notes has been added, updated or deleted */
        fun onUpdated(added: Collection<Note>, updated: Collection<Note>, deleted: Collection<Long>)
        /** called when all notes have been cleared */
        fun onCleared()
    }

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)

    fun get(noteId: Long): Note?

    fun getAllPositions(bbox: BoundingBox): List<LatLon>

    fun getAll(bbox: BoundingBox): List<Note>

    fun getAll(noteIds: Collection<Long>): List<Note>
}
