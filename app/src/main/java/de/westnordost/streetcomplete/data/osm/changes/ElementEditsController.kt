package de.westnordost.streetcomplete.data.osm.changes

import android.content.SharedPreferences
import android.util.Log
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.upload.ElementConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementDeletedException
import de.westnordost.osmapi.map.ElementIdUpdate
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import java.lang.System.currentTimeMillis
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class ElementEditsController @Inject constructor(
    private val editsDB: ElementEditsDao,
    private val elementIdProviderDB: ElementIdProviderDao,
    private val singleUploader: ElementEditUploader,
    private val mapDataController: MapDataController,
    private val prefs: SharedPreferences
): ElementEditsSource {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    private val listeners: MutableList<ElementEditsSource.Listener> = CopyOnWriteArrayList()

    /* ----------------------- Unsynced edits and syncing them -------------------------------- */

    /** Add new unsynced edit to the to-be-uploaded queue */
    fun addEdit(
        questType: OsmElementQuestType<*>,
        elementType: Element.Type,
        elementId: Long,
        source: String,
        position: LatLon,
        action: ElementEditAction
    ) {
        val edit = ElementEdit(null, questType, elementType, elementId, source, position, currentTimeMillis(), false, action)
        editsDB.add(edit)
        val id = edit.id!!
        val createdElementsCount = edit.action.newElementsCount
        elementIdProviderDB.assign(
            id,
            createdElementsCount.nodes,
            createdElementsCount.ways,
            createdElementsCount.relations
        )
        onAddedEdit(edit)
    }

    /** Upload the first item in the unsynced edits queue */
    fun syncOldestEdit(): SyncElementEditResult? {
        val edit = editsDB.getOldestUnsynced() ?: return null
        val editClassName = edit::class.simpleName

        try {
            val idProvider = elementIdProviderDB.get(edit.id!!)

            val elementUpdates = singleUploader.upload(edit, idProvider)
            Log.d(UPLOAD_TAG, "Uploaded a $editClassName")
            markEditSynced(edit)
            updateEditsElementIds(elementUpdates.idUpdates)
            mapDataController.putAll(elementUpdates.updated)
            mapDataController.deleteAll(elementUpdates.deleted)
            // TODO broadcast id updates, broadcast element updates
            // TODO rebuild local changes: on rebuilding, check for each change if new element compatible!
            return SyncElementEditResult(true, edit)
        } catch (e: ElementConflictException) {
            Log.d(UPLOAD_TAG, "Dropped a $editClassName: ${e.message}")
            deleteEdit(edit)
            // TODO rebuild local changes
            if (e is ElementDeletedException) {
                mapDataController.deleteAll(listOf(ElementKey(edit.elementType, edit.elementId)))
            }
            return SyncElementEditResult(false, edit)
        }
    }

    /** Delete old synced (aka uploaded) edits older than the given timestamp. Used to clear
     *  the undo history */
    fun deleteSyncedEditsOlderThan(timestamp: Long): Int = editsDB.deleteSyncedOlderThan(timestamp)

    override fun getUnsyncedEditsCount(): Int = editsDB.getUnsyncedCount()

    override fun getEditsCountSolved(): Int {
        val unsynced = editsDB.getAllUnsynced().map { it.action }
        return unsynced.filter { it !is IsRevert }.size - unsynced.filter { it is IsRevert }.size
    }

    private fun markEditSynced(edit: ElementEdit) {
        val id = edit.id!!
        elementIdProviderDB.delete(id)
        if (editsDB.markSynced(id)) {
            onSyncedEdit(edit)
        }
    }

    private fun updateEditsElementIds(updates: Collection<ElementIdUpdate>) {
        for (update in updates) {
            editsDB.updateElementId(update.elementType, update.oldElementId, update.newElementId)
        }
    }

    /* ----------------------- Undoable edits and undoing them -------------------------------- */

    fun getFirstUndoableEdit(): ElementEdit? =
        editsDB.getAll().firstOrNull {
            if (it.isSynced) it.action is IsRevertable else it.action is IsUndoable
        }

    fun undoEdit(id: Long) {
        val edit = editsDB.get(id) ?: return
        // already uploaded
        if (edit.isSynced) {
            val action = edit.action
            if (action !is IsRevertable) return
            // need to delete the original edit from history because this should not be undoable anymore
            deleteEdit(edit)
            // ... and add a new revert to the queue
            addEdit(edit.questType, edit.elementType, edit.elementId, edit.source, edit.position, action.createReverted())
        }
        // not uploaded yet
        else {
            if (edit.action !is IsUndoable) return
            deleteEdit(edit)
        }
    }

    private fun deleteEdit(edit: ElementEdit) {
        val id = edit.id!!
        elementIdProviderDB.delete(id)
        if (editsDB.delete(id)) {
            onDeletedEdit(edit)
        }
    }

    /* ------------------------------------ Listeners ------------------------------------------- */

    override fun addListener(listener: ElementEditsSource.Listener) {
        this.listeners.add(listener)
    }
    override fun removeListener(listener: ElementEditsSource.Listener) {
        this.listeners.remove(listener)
    }

    private fun onAddedEdit(edit: ElementEdit) {
        prefs.edit().putLong(Prefs.LAST_CHANGE_TIME, currentTimeMillis()).apply()
        listeners.forEach { it.onAddedEdit(edit) }
    }

    private fun onSyncedEdit(edit: ElementEdit) {
        listeners.forEach { it.onSyncedEdit(edit) }
    }

    private fun onDeletedEdit(edit: ElementEdit) {
        listeners.forEach { it.onDeletedEdit(edit) }
    }

    companion object {
        private const val UPLOAD_TAG = "Upload"
    }
}

data class SyncElementEditResult(val success: Boolean, val edit: ElementEdit)
