package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.osm.edits.upload.LastEditTimeStore
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestType
import de.westnordost.streetcomplete.data.quest.OtherSourceQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
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

    private val editCache by lazy {
        val c = hashMapOf<Long, ElementEdit>()
        editsDB.getAll().associateByTo(c) { it.id }
    }

    // full elementIdProvider cache didn't work as expected, so only store empty idPoviders (resp. their ids)
    // this is still very useful, because
    //  most are actually empty (edit tags action)
    //  on rebuildLocalChanges idProviders of all edits are queried, so the cache saves many db queries
    //    each query is fast, but for many unsynced edits this is a clear improvement
    private val emptyIdProviderCache = HashSet<Long>()

    /* ----------------------- Unsynced edits and syncing them -------------------------------- */

    /** Add new unsynced edit to the to-be-uploaded queue */
    override fun add(
        type: ElementEditType,
        element: Element,
        geometry: ElementGeometry,
        source: String,
        action: ElementEditAction,
        key: QuestKey?
    ) {
        add(ElementEdit(0, type, element.type, element.id, element, geometry, source, currentTimeMillis(), false, action))
    }

    fun get(id: Long): ElementEdit? =
        getAll().firstOrNull { it.id == id }

    fun getAll(): List<ElementEdit> = synchronized(this) { editCache.values.toList() }

    fun getAllUnsynced(): List<ElementEdit> =
        getAll().filterNot { it.isSynced }

    fun getOldestUnsynced(): ElementEdit? =
        getAllUnsynced().minByOrNull { it.createdTimestamp }

    fun getIdProvider(id: Long): ElementIdProvider = synchronized(emptyIdProviderCache) {
        if (emptyIdProviderCache.contains(id)) return ElementIdProvider(emptyList())
        val p = elementIdProviderDB.get(id)
        if (p.isEmpty()) emptyIdProviderCache.add(id)
        return p
    }

    /** Delete old synced (aka uploaded) edits older than the given timestamp. Used to clear
     *  the undo history */
    fun deleteSyncedOlderThan(timestamp: Long): Int {
        val deletedCount: Int
        val deleteEdits: List<ElementEdit>
        synchronized(this) {
            deleteEdits = editsDB.getSyncedOlderThan(timestamp)
            if (deleteEdits.isEmpty()) return 0
            deletedCount = editsDB.deleteAll(deleteEdits.map { it.id })
            editCache.values.removeAll { it.isSynced && it.createdTimestamp < timestamp }
        }
        onDeletedEdits(deleteEdits)
        return deletedCount
    }

    override fun getUnsyncedCount(): Int =
        getAllUnsynced().size

    override fun getPositiveUnsyncedCount(): Int {
        val unsynced = getAllUnsynced().map { it.action }
        return unsynced.filter { it !is IsRevertAction }.size - unsynced.filter { it is IsRevertAction }.size
    }

    fun markSynced(edit: ElementEdit, elementUpdates: MapDataUpdates) {
        val syncSuccess: Boolean
        synchronized(this) {
            for (update in elementUpdates.idUpdates) {
                editsDB.updateElementId(update.elementType, update.oldElementId, update.newElementId)
            }
            syncSuccess = editsDB.markSynced(edit.id)

            // now adjust cached edits, and update involved ids
            // alternatively (simpler, safer and slower): just reload editCache if idUpdates is not empty
            for (update in elementUpdates.idUpdates) {
                for (entry in editCache) {
                    if (entry.value.elementType == edit.elementType && entry.value.elementId == update.oldElementId)
                        entry.setValue(entry.value.copy(elementId = update.newElementId))
                }
            }
            if (syncSuccess)
                editCache[edit.id] = edit.copy(isSynced = true)
        }

        if (syncSuccess) onSyncedEdit(edit)

        /* must be deleted after the callback because the callback might want to get the id provider
           for that edit */
        synchronized(emptyIdProviderCache) { emptyIdProviderCache.remove(edit.id) }
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

    private fun add(edit: ElementEdit, key: QuestKey? = null) {
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
            editCache[edit.id] = edit
        }
        if (key != null)
            onAddedEdit(edit, key)
        else
            onAddedEdit(edit)
    }

    private fun delete(edit: ElementEdit) {
        val edits = mutableListOf<ElementEdit>()
        val ids: List<Long>
        synchronized(this) {
            edits.addAll(getEditsBasedOnElementsCreatedByEdit(edit))

            ids = edits.map { it.id }

            editsDB.deleteAll(ids)
            editCache.keys.removeAll(ids)
        }

        onDeletedEdits(edits)

        /* must be deleted after the callback because the callback might want to get the id provider
           for that edit */
        synchronized(emptyIdProviderCache) { ids.forEach { emptyIdProviderCache.remove(it) } }
        elementIdProviderDB.deleteAll(ids)
    }

    private fun getEditsBasedOnElementsCreatedByEdit(edit: ElementEdit): List<ElementEdit> {
        val result = mutableListOf<ElementEdit>()
        val createdElementKeys = getIdProvider(edit.id).getAll()
        val editsBasedOnThese = synchronized(editCache) { // synchronized so there is no need to acquire a lock for each "getAll"
            createdElementKeys.flatMap {
                // copy of db ordering behavior: first synced, then unsynced, and each part sorted by timestamp
                getAll().filter { edit -> edit.elementId == it.id && edit.elementType == it.type }
                    .sortedBy { it.createdTimestamp }.sortedBy { it.isSynced }
            }.filter { it.id != edit.id }
        }

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

    private fun onAddedEdit(edit: ElementEdit, key: QuestKey) {
        lastEditTimeStore.touch()
        listeners.forEach { it.onAddedEdit(edit, key) }
    }

    private fun onSyncedEdit(edit: ElementEdit) {
        listeners.forEach { it.onSyncedEdit(edit) }
    }

    private fun onDeletedEdits(edits: List<ElementEdit>) {
        listeners.forEach { it.onDeletedEdits(edits) }
    }
}
