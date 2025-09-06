package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

/** Manages access to the ATP data storage */
class AtpController(
    private val dao: AtpDao
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    /** Interface to be notified of new atp entries, updated atp entries and atp entries that have been deleted */
    interface Listener {
        /** called when a number of ATP entries has been added, updated or deleted */
        fun onUpdated(added: Collection<AtpEntry>, updated: Collection<AtpEntry>, deleted: Collection<Long>)
        /** called when all atp entries have been cleared */
        fun onCleared()
    }
    private val listeners = Listeners<Listener>()

    private val lock = ReentrantLock()

    /** Replace all entries in the given bounding box with the given entries */
    fun putAllForBBox(bbox: BoundingBox, entries: Collection<AtpEntry>) {
        val time = nowAsEpochMilliseconds()

        val oldEntriesById = mutableMapOf<Long, AtpEntry>()
        val addedEntries = mutableListOf<AtpEntry>()
        val updatedEntries = mutableListOf<AtpEntry>()
        lock.withLock {
            dao.getAll(bbox).associateByTo(oldEntriesById) { it.id }

            for (entry in entries) {
                if (oldEntriesById.containsKey(entry.id)) {
                    updatedEntries.add(entry)
                } else {
                    addedEntries.add(entry)
                }
                oldEntriesById.remove(entry.id)
            }

            dao.putAll(entries)
            dao.deleteAll(oldEntriesById.keys)
        }

        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
        Log.i(TAG, "Persisted ${addedEntries.size} and deleted ${oldEntriesById.size} ATP entries in ${seconds.format(1)}s")

        this@AtpController.onUpdated(added = addedEntries, updated = updatedEntries, deleted = oldEntriesById.keys)
    }

    fun get(entryId: Long): AtpEntry? = dao.get(entryId)

    /** it was needed for notes (delete a note because the note does not exist anymore on OSM (has been closed)) - do we need it here TODO */
    fun delete(entryId: Long) {
        val deleteSuccess = synchronized(this) { dao.delete(entryId) }
        if (deleteSuccess) {
            this@AtpController.onUpdated(deleted = listOf(entryId))
        }
    }


    fun deleteOlderThan(timestamp: Long, limit: Int? = null): Int {
        val ids: List<Long>
        val deletedCount: Int
        synchronized(this) {
            ids = dao.getIdsOlderThan(timestamp, limit)
            if (ids.isEmpty()) return 0

            deletedCount = dao.deleteAll(ids)
        }

        Log.i(TAG, "Deleted $deletedCount old atp entries")

        this@AtpController.onUpdated(deleted = ids)

        return ids.size
    }

    fun clear() {
        dao.clear()
        listeners.forEach { it.onCleared() }
    }

    fun getAllPositions(bbox: BoundingBox): List<LatLon> = dao.getAllPositions(bbox)
    fun getAll(bbox: BoundingBox): List<AtpEntry> = dao.getAll(bbox)
    fun getAll(atpEntries: Collection<Long>): List<AtpEntry> = dao.getAll(atpEntries)

    /* ------------------------------------ Listeners ------------------------------------------- */

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun onUpdated(added: Collection<AtpEntry> = emptyList(), updated: Collection<AtpEntry> = emptyList(), deleted: Collection<Long> = emptyList()) {
        if (added.isEmpty() && updated.isEmpty() && deleted.isEmpty()) return

        listeners.forEach { it.onUpdated(added, updated, deleted) }
    }

    companion object {
        private const val TAG = "AtpController"
    }
}
