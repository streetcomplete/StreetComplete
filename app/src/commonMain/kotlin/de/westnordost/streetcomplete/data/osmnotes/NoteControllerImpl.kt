package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

class NoteControllerImpl(
    private val dao: NoteDao
): NoteController {

    private val listeners = Listeners<NoteSource.Listener>()

    private val lock = ReentrantLock()

    override fun putAllForBBox(bbox: BoundingBox, notes: Collection<Note>) {
        val time = nowAsEpochMilliseconds()

        val oldNotesById = mutableMapOf<Long, Note>()
        val addedNotes = mutableListOf<Note>()
        val updatedNotes = mutableListOf<Note>()
        lock.withLock {
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

    override fun get(noteId: Long): Note? = dao.get(noteId)

    override fun delete(noteId: Long) {
        val deleteSuccess = lock.withLock { dao.delete(noteId) }
        if (deleteSuccess) {
            onUpdated(deleted = listOf(noteId))
        }
    }

    override fun put(note: Note) {
        var hasNote = false
        lock.withLock {
            hasNote = dao.get(note.id) != null
            dao.put(note)
        }

        if (hasNote) {
            onUpdated(updated = listOf(note))
        } else {
            onUpdated(added = listOf(note))
        }
    }

    override fun deleteOlderThan(timestamp: Long, limit: Int?): Int {
        var ids = listOf<Long>()
        var deletedCount = 0
        lock.withLock {
            ids = dao.getIdsOlderThan(timestamp, limit)
            if (ids.isEmpty()) return 0

            deletedCount = dao.deleteAll(ids)
        }

        Log.i(TAG, "Deleted $deletedCount old notes")

        onUpdated(deleted = ids)

        return ids.size
    }

    override fun clear() {
        lock.withLock { dao.clear() }
        listeners.forEach { it.onCleared() }
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

    private fun onUpdated(
        added: Collection<Note> = emptyList(),
        updated: Collection<Note> = emptyList(),
        deleted: Collection<Long> = emptyList()
    ) {
        if (added.isEmpty() && updated.isEmpty() && deleted.isEmpty()) return

        listeners.forEach { it.onUpdated(added, updated, deleted) }
    }

    companion object {
        private const val TAG = "NoteController"
    }
}
