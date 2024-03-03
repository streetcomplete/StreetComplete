package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.osm.edits.upload.LastEditTimeStore
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds

class ElementEditsController(
    private val editsDB: ElementEditsDao,
    private val editElementsDB: EditElementsDao,
    private val elementIdProviderDB: ElementIdProviderDao,
    private val lastEditTimeStore: LastEditTimeStore
) : ElementEditsSource, AddElementEditsController {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    private val listeners = Listeners<ElementEditsSource.Listener>()

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
        geometry: ElementGeometry,
        source: String,
        action: ElementEditAction,
        isNearUserLocation: Boolean,
        key: QuestKey?
    ) {
        // removes discardable tags if they were part of original element, but not if user added them
        val newAction = if (action is UpdateElementTagsAction && action.originalElement.tags.keys.any { it in DISCARDABLE_TAGS }) {
            val builder = StringMapChangesBuilder(action.originalElement.tags)
            action.changes.changes.forEach { when (it) {
                is StringMapEntryDelete -> builder.remove(it.key)
                is StringMapEntryAdd -> builder[it.key] = it.value
                is StringMapEntryModify -> builder[it.key] = it.value
            } }
            DISCARDABLE_TAGS.forEach { builder.remove(it) }
            UpdateElementTagsAction(action.originalElement, builder.create())
        } else
            action
        add(ElementEdit(0, type, geometry, source, nowAsEpochMilliseconds(), false, newAction, isNearUserLocation), key)
    }

    override fun get(id: Long): ElementEdit? = synchronized(this) { editCache[id] }

    override fun getAll(): List<ElementEdit> = synchronized(this) { editCache.values.toList() }

    override fun getAllUnsynced(): List<ElementEdit> =
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
            val ids = deleteEdits.map { it.id }
            editCache.keys.removeAll(ids)
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
        getAllUnsynced().size

    override fun getPositiveUnsyncedCount(): Int {
        val unsynced = getAllUnsynced().map { it.action }
        return unsynced.filter { it !is IsRevertAction }.size - unsynced.filter { it is IsRevertAction }.size
    }

    fun markSynced(edit: ElementEdit, elementUpdates: MapDataUpdates) {
        val idUpdatesMap = elementUpdates.idUpdates.associate {
            ElementKey(it.elementType, it.oldElementId) to it.newElementId
        }
        val syncSuccess: Boolean
        val editIdsToUpdate = HashSet<Long>()
        synchronized(this) {
            elementUpdates.idUpdates.flatMapTo(editIdsToUpdate) {
                editElementsDB.getAllByElement(it.elementType, it.oldElementId)
            }
            for (id in editIdsToUpdate) {
                val oldEdit = editsDB.get(id) ?: continue
                val updatedEdit = oldEdit.copy(action = oldEdit.action.idsUpdatesApplied(idUpdatesMap))
                editsDB.put(updatedEdit)
                editCache[updatedEdit.id] = updatedEdit
                // must clear first because the element ids associated with this id are different now
                editElementsDB.delete(id)
                editElementsDB.put(id, updatedEdit.action.elementKeys)
            }
            if (editIdsToUpdate.isNotEmpty())
                synchronized(emptyIdProviderCache) { emptyIdProviderCache.removeAll(editIdsToUpdate) }
            syncSuccess = editsDB.markSynced(edit.id)

            if (syncSuccess)
                editCache[edit.id] = edit.copy(isSynced = true)
        }

        if (syncSuccess) onSyncedEdit(edit, editIdsToUpdate) // forward which ids were updated, because history controller needs to reload those edits
        elementIdProviderDB.updateIds(elementUpdates.idUpdates)
        synchronized(emptyIdProviderCache) { emptyIdProviderCache.remove(edit.id) }
    }

    fun markSyncFailed(edit: ElementEdit) {
        delete(edit)
    }

    /* ----------------------- Undoable edits and undoing them -------------------------------- */

    /** Undo edit with the given id. If unsynced yet, will delete the edit if it is undoable. If
     *  already synced, will add a revert of that edit as a new edit, if possible */
    fun undo(edit: ElementEdit): Boolean {
        if (edit.isSynced) {
            // already uploaded
            val action = edit.action
            if (action !is IsActionRevertable) return false
            // first create the revert action, as ElementIdProvider will be deleted when deleting the edit
            val reverted = action.createReverted(getIdProvider(edit.id))
            // need to delete the original edit from history because this should not be undoable anymore
            delete(edit)
            // ... and add a new revert to the queue
            add(ElementEdit(0, edit.type, edit.originalGeometry, edit.source, nowAsEpochMilliseconds(), false, reverted, edit.isNearUserLocation))
        } else {
            // not uploaded yet
            delete(edit)
        }
        return true
    }

    /* ------------------------------------ add/sync/delete ------------------------------------- */

    private fun add(edit: ElementEdit, key: QuestKey? = null) {
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
            editCache[edit.id] = edit
        }
        onAddedEdit(edit, key)
    }

    private fun delete(edit: ElementEdit) {
        val edits = mutableListOf<ElementEdit>()
        val ids: List<Long>
        synchronized(this) {
            edits.addAll(getEditsBasedOnElementsCreatedByEdit(edit))

            ids = edits.map { it.id }

            editsDB.deleteAll(ids)
            editElementsDB.deleteAll(ids)
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

        val createdElementKeys = elementIdProviderDB.get(edit.id).getAll()
        val editsBasedOnThese = createdElementKeys
            .flatMapTo(HashSet()) { editElementsDB.getAllByElement(it.type, it.id) }
            .mapNotNull { editCache[it] }
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

    private fun onAddedEdit(edit: ElementEdit, key: QuestKey?) {
        lastEditTimeStore.touch()
        listeners.forEach { it.onAddedEdit(edit, key) }
    }

    private fun onSyncedEdit(edit: ElementEdit, updatedEditIds: Collection<Long>) {
        listeners.forEach { it.onSyncedEdit(edit, updatedEditIds) }
    }

    private fun onDeletedEdits(edits: List<ElementEdit>) {
        listeners.forEach { it.onDeletedEdits(edits) }
    }
}

// list from josm
private val DISCARDABLE_TAGS = hashSetOf(
    "created_by",
    "converted_by",
    "current_id",
    "geobase:datasetName",
    "geobase:uuid",
    "KSJ2:ADS",
    "KSJ2:ARE",
    "KSJ2:AdminArea",
    "KSJ2:COP_label",
    "KSJ2:DFD",
    "KSJ2:INT",
    "KSJ2:INT_label",
    "KSJ2:LOC",
    "KSJ2:LPN",
    "KSJ2:OPC",
    "KSJ2:PubFacAdmin",
    "KSJ2:RAC",
    "KSJ2:RAC_label",
    "KSJ2:RIC",
    "KSJ2:RIN",
    "KSJ2:WSC",
    "KSJ2:coordinate",
    "KSJ2:curve_id",
    "KSJ2:curve_type",
    "KSJ2:filename",
    "KSJ2:lake_id",
    "KSJ2:lat",
    "KSJ2:long",
    "KSJ2:river_id",
    "odbl",
    "odbl:note",
    "osmarender:nameDirection",
    "osmarender:renderName",
    "osmarender:renderRef",
    "osmarender:rendernames",
    "SK53_bulk:load",
    "sub_sea:type",
    "tiger:source",
    "tiger:separated",
    "tiger:tlid",
    "tiger:upload_uuid",
    "import_uuid",
    "gnis:import_uuid",
    "yh:LINE_NAME",
    "yh:LINE_NUM",
    "yh:STRUCTURE",
    "yh:TOTYUMONO",
    "yh:TYPE",
    "yh:WIDTH",
    "yh:WIDTH_RANK"
)
