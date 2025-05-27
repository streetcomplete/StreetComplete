package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log

/** Manages access to the notes storage */
class NoteController(
    private val dao: NoteDao
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    /** Interface to be notified of new notes, updated notes and notes that have been deleted */
    interface Listener {
        /** called when a number of notes has been added, updated or deleted */
        fun onUpdated(added: Collection<Note>, updated: Collection<Note>, deleted: Collection<Long>)
        /** called when all notes have been cleared */
        fun onCleared()
    }
    private val listeners = Listeners<Listener>()

    /** Replace all notes in the given bounding box with the given notes */
    fun putAllForBBox(bbox: BoundingBox, notes: Collection<Note>) {
        val time = nowAsEpochMilliseconds()

        val oldNotesById = mutableMapOf<Long, Note>()
        val addedNotes = mutableListOf<Note>()
        val updatedNotes = mutableListOf<Note>()
        synchronized(this) {
            dao.getAll(bbox).associateByTo(oldNotesById) { it.id }

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
        }

        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
        Log.i(TAG, "Persisted ${addedNotes.size} and deleted ${oldNotesById.size} notes in ${seconds.format(1)}s")

        onUpdated(added = addedNotes, updated = updatedNotes, deleted = oldNotesById.keys)
    }

    fun get(noteId: Long): Note? = dao.get(noteId)

    /** delete a note because the note does not exist anymore on OSM (has been closed) */
    fun delete(noteId: Long) {
        val deleteSuccess = synchronized(this) { dao.delete(noteId) }
        if (deleteSuccess) {
            onUpdated(deleted = listOf(noteId))
        }
    }

    /** put a note because the note has been created/changed on OSM */
    fun put(note: Note) {
        val hasNote = synchronized(this) { dao.get(note.id) != null }

        if (hasNote) {
            onUpdated(updated = listOf(note))
        } else {
            onUpdated(added = listOf(note))
        }

        dao.put(note)
    }

    fun deleteOlderThan(timestamp: Long, limit: Int? = null): Int {
        val ids: List<Long>
        val deletedCount: Int
        synchronized(this) {
            ids = dao.getIdsOlderThan(timestamp, limit)
            if (ids.isEmpty()) return 0

            deletedCount = dao.deleteAll(ids)
        }

        Log.i(TAG, "Deleted $deletedCount old notes")

        onUpdated(deleted = ids)

        return ids.size
    }

    fun clear() {
        dao.clear()
        listeners.forEach { it.onCleared() }
    }

    fun getAllPositions(bbox: BoundingBox): List<LatLon> = dao.getAllPositions(bbox)
    fun getAll(bbox: BoundingBox): List<Note> = dao.getAll(bbox)
    fun getAll(noteIds: Collection<Long>): List<Note> = dao.getAll(noteIds)

    /* ------------------------------------ Listeners ------------------------------------------- */

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun onUpdated(added: Collection<Note> = emptyList(), updated: Collection<Note> = emptyList(), deleted: Collection<Long> = emptyList()) {
        if (added.isEmpty() && updated.isEmpty() && deleted.isEmpty()) return

        listeners.forEach { it.onUpdated(added, updated, deleted) }
    }

    companion object {
        private const val TAG = "NoteController"
    }
}
