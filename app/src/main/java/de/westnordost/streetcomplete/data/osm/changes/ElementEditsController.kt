package de.westnordost.streetcomplete.data.osm.changes

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs
import de.westnordost.osmapi.map.ElementIdUpdate
import de.westnordost.osmapi.map.ElementUpdates
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
    private val prefs: SharedPreferences
): ElementEditsSource {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    private val listeners: MutableList<ElementEditsSource.Listener> = CopyOnWriteArrayList()

    /* ----------------------- Unsynced edits and syncing them -------------------------------- */

    /** Add new unsynced edit to the to-be-uploaded queue */
    fun add(
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
        val createdElementsCount = action.newElementsCount
        elementIdProviderDB.assign(
            id,
            createdElementsCount.nodes,
            createdElementsCount.ways,
            createdElementsCount.relations
        )
        onAddedEdit(edit)
    }

    fun getAllUnsynced(): List<ElementEdit> =
        editsDB.getAllUnsynced()

    fun getOldestUnsynced(): ElementEdit? =
        editsDB.getOldestUnsynced()

    fun getIdProvider(id: Long): ElementIdProvider =
        elementIdProviderDB.get(id)

    /** Delete old synced (aka uploaded) edits older than the given timestamp. Used to clear
     *  the undo history */
    fun deleteSyncedOlderThan(timestamp: Long): Int = editsDB.deleteSyncedOlderThan(timestamp)

    override fun getUnsyncedCount(): Int = editsDB.getUnsyncedCount()

    override fun getPositiveUnsyncedCount(): Int {
        val unsynced = editsDB.getAllUnsynced().map { it.action }
        return unsynced.filter { it !is IsRevert }.size - unsynced.filter { it is IsRevert }.size
    }

    /** update/delete elements because the elements have changed on OSM after upload */
    fun synced(edit: ElementEdit, elementUpdates: ElementUpdates) {
        updateElementIds(elementUpdates.idUpdates)
        markSynced(edit)
    }

    fun syncFailed(edit: ElementEdit) {
        deleteEdit(edit)
    }

    private fun markSynced(edit: ElementEdit) {
        val id = edit.id!!
        elementIdProviderDB.delete(id)
        if (editsDB.markSynced(id)) {
            onSyncedEdit(edit)
        }
    }

    private fun updateElementIds(updates: Collection<ElementIdUpdate>) {
        for (update in updates) {
            editsDB.updateElementId(update.elementType, update.oldElementId, update.newElementId)
        }
    }

    /* ----------------------- Undoable edits and undoing them -------------------------------- */

    fun getMostRecentUndoableEdit(): ElementEdit? =
        editsDB.getAll().firstOrNull {
            if (it.isSynced) it.action is IsRevertable else it.action is IsUndoable
        }

    /** Undo edit with the given id. If unsynced yet, will delete the edit if it is undoable. If
     *  already synced, will add a revert of that edit as a new edit, if possible */
    fun undoEdit(id: Long) {
        val edit = editsDB.get(id) ?: return
        // already uploaded
        if (edit.isSynced) {
            val action = edit.action
            if (action !is IsRevertable) return
            // need to delete the original edit from history because this should not be undoable anymore
            deleteEdit(edit)
            // ... and add a new revert to the queue
            add(edit.questType, edit.elementType, edit.elementId, edit.source, edit.position, action.createReverted())
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
}
