package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.osm.edits.upload.LastEditTimeStore
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import java.util.concurrent.CopyOnWriteArrayList

class ElementEditsController(
    private val editsDB: ElementEditsDao,
    private val editElementsDB: EditElementsDao,
    private val elementIdProviderDB: ElementIdProviderDao,
    private val lastEditTimeStore: LastEditTimeStore
) : ElementEditsSource, AddElementEditsController {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    private val listeners: MutableList<ElementEditsSource.Listener> = CopyOnWriteArrayList()

    /* ----------------------- Unsynced edits and syncing them -------------------------------- */

    /** Add new unsynced edit to the to-be-uploaded queue */
    override fun add(
        type: ElementEditType,
        geometry: ElementGeometry,
        source: String,
        action: ElementEditAction
    ) {
        add(ElementEdit(0, type, geometry, source, nowAsEpochMilliseconds(), false, action))
    }

    override fun get(id: Long): ElementEdit? =
        editsDB.get(id)

    override fun getAll(): List<ElementEdit> =
        editsDB.getAll()

    override fun getAllUnsynced(): List<ElementEdit> =
        editsDB.getAllUnsynced()

    fun getOldestUnsynced(): ElementEdit? =
        editsDB.getOldestUnsynced()

    fun getIdProvider(id: Long): ElementIdProvider =
        elementIdProviderDB.get(id)

    /** Delete old synced (aka uploaded) edits older than the given timestamp. Used to clear
     *  the undo history */
    fun deleteSyncedOlderThan(timestamp: Long): Int {
        val deletedCount: Int
        val deleteEdits: List<ElementEdit>
        synchronized(this) {
            deleteEdits = editsDB.getSyncedOlderThan(timestamp)
            if (deleteEdits.isEmpty()) return 0
            val ids = deleteEdits.map { it.id }
            deletedCount = editsDB.deleteAll(ids)
            editElementsDB.deleteAll(ids)
        }
        onDeletedEdits(deleteEdits)
        /* must be deleted after the callback because the callback might want to get the id provider
           for that edit */
        elementIdProviderDB.deleteAll(deleteEdits.map { it.id })
        return deletedCount
    }

    override fun getUnsyncedCount(): Int =
        editsDB.getUnsyncedCount()

    override fun getPositiveUnsyncedCount(): Int {
        val unsynced = editsDB.getAllUnsynced().map { it.action }
        return unsynced.filter { it !is IsRevertAction }.size - unsynced.filter { it is IsRevertAction }.size
    }

    fun markSynced(edit: ElementEdit, elementUpdates: MapDataUpdates) {
        val idUpdatesMap = elementUpdates.idUpdates.associate {
            ElementKey(it.elementType, it.oldElementId) to it.newElementId
        }
        val syncSuccess: Boolean
        synchronized(this) {
            val editIdsToUpdate = HashSet<Long>()
            elementUpdates.idUpdates.flatMapTo(editIdsToUpdate) {
                editElementsDB.getAllByElement(it.elementType, it.oldElementId)
            }
            for (id in editIdsToUpdate) {
                val oldEdit = editsDB.get(id) ?: continue
                val updatedEdit = oldEdit.copy(action = oldEdit.action.idsUpdatesApplied(idUpdatesMap))
                editsDB.put(updatedEdit)
                // must clear first because the element ids associated with this id are different now
                editElementsDB.delete(id)
                editElementsDB.put(id, updatedEdit.action.elementKeys)
            }
            syncSuccess = editsDB.markSynced(edit.id)
        }
        if (syncSuccess) onSyncedEdit(edit)
        elementIdProviderDB.updateIds(elementUpdates.idUpdates)
    }

    fun markSyncFailed(edit: ElementEdit) {
        delete(edit)
    }

    /* ----------------------- Undoable edits and undoing them -------------------------------- */

    /** Undo edit with the given id. If unsynced yet, will delete the edit if it is undoable. If
     *  already synced, will add a revert of that edit as a new edit, if possible */
    fun undo(edit: ElementEdit): Boolean {
        // already uploaded
        if (edit.isSynced) {
            val action = edit.action
            if (action !is IsActionRevertable) return false
            // need to delete the original edit from history because this should not be undoable anymore
            delete(edit)
            // ... and add a new revert to the queue
            val reverted = action.createReverted(getIdProvider(edit.id))
            add(ElementEdit(0, edit.type, edit.originalGeometry, edit.source, nowAsEpochMilliseconds(), false, reverted))
        }
        // not uploaded yet
        else {
            delete(edit)
        }
        return true
    }

    /* ------------------------------------ add/sync/delete ------------------------------------- */

    private fun add(edit: ElementEdit) {
        synchronized(this) {
            editsDB.put(edit)
            editElementsDB.put(edit.id, edit.action.elementKeys)
            val createdElementsCount = edit.action.newElementsCount
            elementIdProviderDB.assign(
                edit.id,
                createdElementsCount.nodes,
                createdElementsCount.ways,
                createdElementsCount.relations
            )
        }
        onAddedEdit(edit)
    }

    private fun delete(edit: ElementEdit) {
        val edits = mutableListOf<ElementEdit>()
        val ids: List<Long>
        synchronized(this) {
            edits.addAll(getEditsBasedOnElementsCreatedByEdit(edit))

            ids = edits.map { it.id }

            editsDB.deleteAll(ids)
            editElementsDB.deleteAll(ids)
        }

        onDeletedEdits(edits)

        /* must be deleted after the callback because the callback might want to get the id provider
           for that edit */
        elementIdProviderDB.deleteAll(ids)
    }

    private fun getEditsBasedOnElementsCreatedByEdit(edit: ElementEdit): List<ElementEdit> {
        val result = mutableListOf<ElementEdit>()

        val createdElementKeys = elementIdProviderDB.get(edit.id).getAll()
        val editsBasedOnThese = createdElementKeys
            .flatMapTo(HashSet()) { editElementsDB.getAllByElement(it.type, it.id) }
            .mapNotNull { editsDB.get(it) }
            .filter { it.id != edit.id }

        // deep first
        for (e in editsBasedOnThese) {
            result += getEditsBasedOnElementsCreatedByEdit(e)
        }
        result += edit

        return result
    }

    /* ------------------------------------ Listeners ------------------------------------------- */

    override fun addListener(listener: ElementEditsSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: ElementEditsSource.Listener) {
        listeners.remove(listener)
    }

    private fun onAddedEdit(edit: ElementEdit) {
        lastEditTimeStore.touch()
        listeners.forEach { it.onAddedEdit(edit) }
    }

    private fun onSyncedEdit(edit: ElementEdit) {
        listeners.forEach { it.onSyncedEdit(edit) }
    }

    private fun onDeletedEdits(edits: List<ElementEdit>) {
        listeners.forEach { it.onDeletedEdits(edits) }
    }
}
