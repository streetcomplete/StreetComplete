package de.westnordost.streetcomplete.data.osmnotes

import android.util.Log
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.ktx.format
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Manages access to the notes storage */
@Singleton class NoteController @Inject constructor(
    private val dao: NoteDao
): NoteSource {
    /* Must be a singleton because there is a listener that should respond to a change in the
    *  database table */

    private val listeners: MutableList<NoteSource.Listener> = CopyOnWriteArrayList()

    /** Replace all notes in the given bounding box with the given notes */
    fun putAllForBBox(bbox: BoundingBox, notes: Collection<Note>) {
        val time = System.currentTimeMillis()

        val oldNotesById = mutableMapOf<Long, Note>()
        dao.getAll(bbox).associateByTo(oldNotesById) { it.id }

        val addedNotes = mutableListOf<Note>()
        val updatedNotes = mutableListOf<Note>()
        for (note in notes) {
            if (oldNotesById.containsKey(note.id)) {
                updatedNotes.add(note)
            } else {
                addedNotes.add(note)
            }
            oldNotesById.remove(note.id)
        }
        dao.putAll(notes)
        dao.deleteAll(oldNotesById.keys)

        val seconds = (System.currentTimeMillis() - time) / 1000.0
        Log.i(TAG,"Added ${addedNotes.size} and deleted ${oldNotesById.size} notes in ${seconds.format(1)}s")

        onUpdated(added = addedNotes, updated = updatedNotes, deleted = oldNotesById.keys)
    }

    override fun get(noteId: Long): Note? = dao.get(noteId)

    /** delete a note because the note does not exist anymore on OSM (has been closed) */
    fun delete(noteId: Long) {
        if (dao.delete(noteId)) {
            onUpdated(deleted = listOf(noteId))
        }
    }

    /** put a note because the note has been created/changed on OSM */
    fun put(note: Note) {
        val hasNote = dao.get(note.id) != null
        dao.put(note)
        if (hasNote) onUpdated(updated = listOf(note))
        else onUpdated(added = listOf(note))
    }

    fun deleteAllOlderThan(timestamp: Long): Int {
        val ids = dao.getAllIdsOlderThan(timestamp)
        dao.deleteAll(ids)
        Log.i(TAG, "Deleted ${ids.size} old notes")
        onUpdated(deleted = ids)
        return ids.size
    }

    override fun getAllPositions(bbox: BoundingBox): List<LatLon> = dao.getAllPositions(bbox)
    override fun getAll(bbox: BoundingBox): List<Note> = dao.getAll(bbox)
    override fun getAll(noteIds: Collection<Long>): List<Note> = dao.getAll(noteIds)

    /* ------------------------------------ Listeners ------------------------------------------- */

    override fun addListener(listener: NoteSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: NoteSource.Listener) {
        listeners.remove(listener)
    }

    private fun onUpdated(added: Collection<Note> = emptyList(), updated: Collection<Note> = emptyList(), deleted: Collection<Long> = emptyList()) {
        listeners.forEach { it.onUpdated(added, updated, deleted) }
    }

    companion object {
        private const val TAG = "NoteController"
    }
}
