package de.westnordost.streetcomplete.data.osmnotes

import android.util.Log
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.notes.Note
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Manages access to the notes storage */
@Singleton class NoteController @Inject constructor(
    private val dao: NoteDao
): NoteSource {
    /* Must be a singleton because there is a listener that should respond to a change in the
    *  database table */

    private val noteUpdatesListeners: MutableList<NoteSource.NoteUpdatesListener> = CopyOnWriteArrayList()

    override fun getAllPositions(bbox: BoundingBox): List<LatLon> = dao.getAllPositions(bbox)

    /** Replace all notes in the given bounding box with the given notes */
    fun updateForBBox(bbox: BoundingBox, notes: List<Note>) {
        val time = System.currentTimeMillis()

        val oldNoteIds = dao.getAllIds(bbox).toMutableSet()
        for (note in notes) {
            oldNoteIds.remove(note.id)
        }
        dao.putAll(notes)
        dao.deleteAll(oldNoteIds)

        val seconds = (System.currentTimeMillis() - time) / 1000
        Log.i(TAG,"Persisted ${notes.size} and deleted ${oldNoteIds.size} notes in ${seconds}s")

        noteUpdatesListeners.forEach { it.onUpdatedForBBox(bbox, notes) }
    }

    /** add a new note because a new note has been added to OSM */
    fun add(note: Note) {
        dao.put(note)
        noteUpdatesListeners.forEach { it.onAdded(note) }
    }

    /** delete a note because the note does not exist anymore on OSM (has been closed) */
    fun delete(noteId: Long) {
        dao.delete(noteId)
        noteUpdatesListeners.forEach { it.onDeleted(noteId) }
    }

    /** update a note because the note has changed on OSM */
    fun update(note: Note) {
        // TODO bulk update would be better
        dao.put(note)
        noteUpdatesListeners.forEach { it.onUpdated(note) }
    }

    override fun addNoteUpdatesListener(listener: NoteSource.NoteUpdatesListener) {
        noteUpdatesListeners.add(listener)
    }
    override fun removeNoteUpdatesListener(listener: NoteSource.NoteUpdatesListener) {
        noteUpdatesListeners.remove(listener)
    }

    companion object {
        private const val TAG = "NoteController"
    }
}
