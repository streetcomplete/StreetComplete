package de.westnordost.streetcomplete.data.osm.changes

import android.content.SharedPreferences
import android.util.Log
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.OsmElementController
import de.westnordost.streetcomplete.data.osm.upload.ElementConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementDeletedException
import de.westnordost.streetcomplete.data.osm.upload.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.upload.SingleOsmElementChangeUploader
import java.lang.System.currentTimeMillis
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class OsmElementChangesController @Inject constructor(
    private val changesDB: OsmElementChangesDao,
    private val newOsmElementIdProviderDB: NewOsmElementIdProviderDao,
    private val singleUploader: SingleOsmElementChangeUploader,
    private val osmElementController: OsmElementController,
    private val prefs: SharedPreferences
): OsmElementChangesSource {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    private val listeners: MutableList<OsmElementChangesSource.Listener> = CopyOnWriteArrayList()

    /* ----------------------- Unsynced changes and syncing them -------------------------------- */

    /** Add new unsynced change to the to-be-uploaded queue */
    fun addChange(change: OsmElementChange) {
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

    /** Upload the first item in the unsynced changes queue */
    fun syncOldestChange(): SyncOsmElementChangeResult? {
        val change = changesDB.getOldestUnsynced() ?: return null
        val changeClassName = change::class.simpleName

        try {
            val element = osmElementController.get(change.elementType, change.elementId)
                ?: throw ElementDeletedException()

            val elementUpdates = singleUploader.upload(change, element)
            Log.d(UPLOAD_TAG, "Uploaded a $changeClassName")
            markChangeSynced(change)
            updateChangesElementIds(elementUpdates.idUpdates)
            osmElementController.putAll(elementUpdates.updated)
            osmElementController.deleteAll(elementUpdates.deleted)
            // TODO broadcast id updates, broadcast element updates
            // TODO rebuild local changes: on rebuilding, check for each change if new element compatible!
            return SyncOsmElementChangeResult(true, change)
        } catch (e: ElementConflictException) {
            Log.d(UPLOAD_TAG, "Dropped a $changeClassName: ${e.message}")
            deleteChange(change)
            // TODO rebuild local changes
            if (e is ElementDeletedException) {
                osmElementController.deleteAll(listOf(ElementKey(change.elementType, change.elementId)))
            }
            return SyncOsmElementChangeResult(false, change)
        }
    }

    /** Delete old synced (aka uploaded) changes older than the given timestamp. Used to clear
     *  the undo history */
    fun deleteSyncedChangesOlderThan(timestamp: Long): Int = changesDB.deleteSyncedOlderThan(timestamp)

    override fun getUnsyncedChangesCount(): Int = changesDB.getUnsyncedCount()

    override fun getChangesCountSolved(): Int {
        val unsynced = changesDB.getAllUnsynced()
        return unsynced.filter { it !is IsRevert }.size - unsynced.filter { it is IsRevert }.size
    }

    private fun markChangeSynced(change: OsmElementChange) {
        val id = change.id!!
        newOsmElementIdProviderDB.delete(id)
        if (changesDB.markSynced(id)) {
            onSyncedChange(change)
        }
    }

    private fun updateChangesElementIds(updates: Collection<ElementIdUpdate>) {
        for (update in updates) {
            changesDB.updateElementId(update.elementType, update.oldElementId, update.newElementId)
        }
    }

    /* ----------------------- Undoable changes and undoing them -------------------------------- */

    fun getFirstUndoableChange(): OsmElementChange? =
        changesDB.getAll().firstOrNull { if (it.isSynced) it is IsRevertable else it is IsUndoable }

    fun undoChange(id: Long) {
        val change = changesDB.get(id) ?: return
        // already uploaded
        if (change.isSynced) {
            if (change !is IsRevertable) return
            // need to delete the original change from history because this should not be undoable anymore
            deleteChange(change)
            // ... and add a new revert to the queue
            addChange(change.createReverted())
        }
        // not uploaded yet
        else {
            if (change !is IsUndoable) return
            deleteChange(change)
        }
    }

    private fun deleteChange(change: OsmElementChange) {
        val id = change.id!!
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

    private fun onSyncedChange(change: OsmElementChange) {
        listeners.forEach { it.onSyncedChange(change) }
    }

    private fun onDeletedChange(change: OsmElementChange) {
        listeners.forEach { it.onDeletedChange(change) }
    }

    companion object {
        private const val UPLOAD_TAG = "Upload"
    }
}

data class SyncOsmElementChangeResult(val success: Boolean, val change: OsmElementChange)
