package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox

/** Manages access to the notes storage */
interface NoteController : NoteSource {
    /** Replace all notes in the given bounding box with the given notes */
    fun putAllForBBox(bbox: BoundingBox, notes: Collection<Note>)

    /** delete a note because the note does not exist anymore on OSM (has been closed) */
    fun delete(noteId: Long)

    /** put a note because the note has been created/changed on OSM */
    fun put(note: Note)

    fun deleteOlderThan(timestamp: Long, limit: Int? = null): Int

    fun clear()
}
