package de.westnordost.streetcomplete.data.osm.changes

import android.content.SharedPreferences
import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.Prefs
import java.lang.System.currentTimeMillis
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class OsmElementChangesController @Inject constructor(
    private val db: OsmElementChangesDao,
    private val prefs: SharedPreferences
): OsmElementChangesSource {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    private val listeners: MutableList<OsmElementChangesSource.Listener> = CopyOnWriteArrayList()

    fun changesAppliedTo(element: Element?): Element? {
        TODO()
    }

    fun changesAppliedTo(mapDataWithGeometry: MapDataWithGeometry): MapDataWithGeometry {
        TODO()
    }

    fun getOldestUnsynced(): OsmElementChange? = db.getOldestUnsynced()

    fun getFirstUndoable(): OsmElementChange? =
        db.getAll().firstOrNull { if (it.isSynced) it is IsRevertable else it is IsUndoable }


    fun undo(id: Long) {
        val change = db.get(id) ?: return
        // already uploaded
        if (change.isSynced) {
            if (change !is IsRevertable) return
            // need to delete the original change from history because this should not be undoable anymore
            delete(change)
            // ... and add a new revert to the queue
            add(change.createReverted())
        }
        // not uploaded yet
        else {
            if (change !is IsUndoable) return
            delete(change)
        }
    }

    /** Delete old synced (aka uploaded) changes older than the given timestamp. Used to clear
     *  the undo history */
    fun deleteSyncedOlderThan(timestamp: Long): Int = db.deleteSyncedOlderThan(timestamp)

    override fun getUnsyncedCount(): Int = db.getUnsyncedCount()

    override fun getSolvedCount(): Int {
        val unsynced = db.getAllUnsynced()
        return unsynced.filter { it !is IsRevert }.size - unsynced.filter { it is IsRevert }.size
    }

    /** Add new unsynced change to the to-be-uploaded queue */
    fun add(change: OsmElementChange) {
        check(!change.isSynced)
        db.add(change)
        onAddedChange(change)
    }

    /** Mark the change with the given id as synced (=uploaded) */
    fun markSynced(id: Long) {
        if (db.markSynced(id)) {
            TODO("should trigger listener?!")
        }
    }

    /** Delete the given change. */
    fun delete(change: OsmElementChange) {
        val id = change.id
        check(id != null)
        if (db.delete(id)) {
            onDeletedChange(change)
        }
    }

    /* ------------------------------------ Listeners ------------------------------------------- */

    override fun addListener(listener: OsmElementChangesSource.Listener) {
        this.listeners.add(listener)
    }
    override fun removeListener(listener: OsmElementChangesSource.Listener) {
        this.listeners.remove(listener)
    }

    private fun onAddedChange(change: OsmElementChange) {
        prefs.edit().putLong(Prefs.LAST_CHANGE_TIME, currentTimeMillis()).apply()
        listeners.forEach { it.onAddedChange(change) }
    }

    private fun onDeletedChange(change: OsmElementChange) {
        listeners.forEach { it.onDeletedChange(change) }
    }
}
