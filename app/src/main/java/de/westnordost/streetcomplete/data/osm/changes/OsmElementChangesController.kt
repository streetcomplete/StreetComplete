package de.westnordost.streetcomplete.data.osm.changes

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs
import java.lang.System.currentTimeMillis
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class OsmElementChangesController @Inject constructor(
    private val changesDB: OsmElementChangesDao,
    private val newOsmElementIdProviderDB: NewOsmElementIdProviderDao,
    private val prefs: SharedPreferences
): OsmElementChangesSource {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    private val listeners: MutableList<OsmElementChangesSource.Listener> = CopyOnWriteArrayList()

    fun getOldestUnsynced(): OsmElementChange? = changesDB.getOldestUnsynced()

    fun getFirstUndoable(): OsmElementChange? =
        changesDB.getAll().firstOrNull { if (it.isSynced) it is IsRevertable else it is IsUndoable }


    fun undo(id: Long) {
        val change = changesDB.get(id) ?: return
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
    fun deleteSyncedOlderThan(timestamp: Long): Int = changesDB.deleteSyncedOlderThan(timestamp)

    override fun getUnsyncedCount(): Int = changesDB.getUnsyncedCount()

    override fun getSolvedCount(): Int {
        val unsynced = changesDB.getAllUnsynced()
        return unsynced.filter { it !is IsRevert }.size - unsynced.filter { it is IsRevert }.size
    }

    /** Add new unsynced change to the to-be-uploaded queue */
    fun add(change: OsmElementChange) {
        check(!change.isSynced)
        changesDB.add(change)
        val id = change.id
        check(id != null)
        val createdElementsCount = change.newElementsCount
        newOsmElementIdProviderDB.assign(
            change.id!!,
            createdElementsCount.nodes,
            createdElementsCount.ways,
            createdElementsCount.relations
        )
        onAddedChange(change)
    }

    /** Mark the change with the given id as synced (=uploaded) */
    fun markSynced(id: Long) {
        newOsmElementIdProviderDB.delete(id)
        if (changesDB.markSynced(id)) {
            TODO("should trigger listener?!")
        }
    }

    /** Delete the given change. */
    fun delete(change: OsmElementChange) {
        val id = change.id
        check(id != null)
        newOsmElementIdProviderDB.delete(id)
        if (changesDB.delete(id)) {
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
