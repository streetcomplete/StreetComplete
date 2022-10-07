package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.osm.edits.upload.LastEditTimeStore
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import java.lang.System.currentTimeMillis
import java.util.concurrent.CopyOnWriteArrayList

class ElementEditsController(
    private val editsDB: ElementEditsDao,
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
        element: Element,
        geometry: ElementGeometry,
        source: String,
        action: ElementEditAction
    ) {
        add(ElementEdit(0, type, element.type, element.id, element, geometry, source, currentTimeMillis(), false, action))
    }

    fun get(id: Long): ElementEdit? =
        editsDB.get(id)

    fun getAll(): List<ElementEdit> =
        editsDB.getAll()

    fun getAllUnsynced(): List<ElementEdit> =
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
            deletedCount = editsDB.deleteAll(deleteEdits.map { it.id })
        }
        onDeletedEdits(deleteEdits)
        return deletedCount
    }

    override fun getUnsyncedCount(): Int =
        editsDB.getUnsyncedCount()

    override fun getPositiveUnsyncedCount(): Int {
        val unsynced = editsDB.getAllUnsynced().map { it.action }
        return unsynced.filter { it !is IsRevertAction }.size - unsynced.filter { it is IsRevertAction }.size
    }

    fun markSynced(edit: ElementEdit, elementUpdates: MapDataUpdates) {
        val syncSuccess: Boolean
        synchronized(this) {
            for (update in elementUpdates.idUpdates) {
                editsDB.updateElementId(update.elementType, update.oldElementId, update.newElementId)
            }
            syncSuccess = editsDB.markSynced(edit.id)
        }
        if (syncSuccess) onSyncedEdit(edit)

        /* must be deleted after the callback because the callback might want to get the id provider
           for that edit */
        elementIdProviderDB.delete(edit.id)
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
            add(ElementEdit(0, edit.type, edit.elementType, edit.elementId, edit.originalElement, edit.originalGeometry, edit.source, currentTimeMillis(), false, action.createReverted()))
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
            editsDB.add(edit)
            val id = edit.id
            val createdElementsCount = edit.action.newElementsCount
            elementIdProviderDB.assign(
                id,
                createdElementsCount.nodes,
                createdElementsCount.ways,
                createdElementsCount.relations
            )
            // set proper assigned id of the new element
            val hasDummyElement = edit.elementId == 0L
            if (hasDummyElement) {
                if (edit.elementType != ElementType.NODE) {
                    throw IllegalStateException("Element creation only supported for nodes")
                }
                val idProvider = elementIdProviderDB.get(id)
                editsDB.updateElementId(id, idProvider.nextNodeId())
            }
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
            .flatMap { editsDB.getByElement(it.type, it.id) }
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
