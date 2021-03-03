package de.westnordost.streetcomplete.data.osm.edits

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs
import de.westnordost.osmapi.map.ElementIdUpdate
import de.westnordost.osmapi.map.ElementUpdates
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
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
    @Synchronized fun add(
        questType: OsmElementQuestType<*>,
        elementType: Element.Type,
        elementId: Long,
        source: String,
        position: LatLon,
        action: ElementEditAction
    ) {
        // some hardcode here, but not sure how to generalize this:
        if (action is UpdateElementTagsAction) {
            /* if there is an unsynced UpdateElementTagsAction that is the exact reverse of what
               shall be added now, instead of adding that, delete that reverse edit */
            val reverseEdit = getAllUnsynced().asReversed().find {
                it.elementType == elementType && it.elementId == elementId
                it.action is UpdateElementTagsAction && it.action.isReverseOf(action)
            }
            if (reverseEdit != null) {
                delete(reverseEdit)
                return
            }
        }

        val edit = ElementEdit(0, questType, elementType, elementId, source, position, currentTimeMillis(), false, action)
        add(edit)
    }

    fun getAllUnsynced(): List<ElementEdit> =
        editsDB.getAllUnsynced()

    fun getOldestUnsynced(): ElementEdit? =
        editsDB.getOldestUnsynced()

    fun getIdProvider(id: Long): ElementIdProvider =
        elementIdProviderDB.get(id)

    /** Delete old synced (aka uploaded) edits older than the given timestamp. Used to clear
     *  the undo history */
    @Synchronized fun deleteSyncedOlderThan(timestamp: Long): Int =
        editsDB.deleteSyncedOlderThan(timestamp)

    override fun getUnsyncedCount(): Int =
        editsDB.getUnsyncedCount()

    override fun getPositiveUnsyncedCount(): Int {
        val unsynced = editsDB.getAllUnsynced().map { it.action }
        return unsynced.filter { it !is IsRevertAction }.size - unsynced.filter { it is IsRevertAction }.size
    }

    @Synchronized fun synced(edit: ElementEdit, elementUpdates: ElementUpdates) {
        updateElementIds(elementUpdates.idUpdates)
        markSynced(edit)
    }

    @Synchronized fun syncFailed(edit: ElementEdit) {
        delete(edit)
    }

    private fun updateElementIds(updates: Collection<ElementIdUpdate>) {
        for (update in updates) {
            editsDB.updateElementId(update.elementType, update.oldElementId, update.newElementId)
        }
    }

    /* ----------------------- Undoable edits and undoing them -------------------------------- */

    fun getMostRecentUndoableEdit(): ElementEdit? =
        editsDB.getAll().firstOrNull { !it.isSynced || it.action is IsActionRevertable }

    /** Undo edit with the given id. If unsynced yet, will delete the edit if it is undoable. If
     *  already synced, will add a revert of that edit as a new edit, if possible */
    @Synchronized fun undo(id: Long) {
        val edit = editsDB.get(id) ?: return
        // already uploaded
        if (edit.isSynced) {
            val action = edit.action
            if (action !is IsActionRevertable) return
            // need to delete the original edit from history because this should not be undoable anymore
            delete(edit)
            // ... and add a new revert to the queue
            add(edit.questType, edit.elementType, edit.elementId, edit.source, edit.position, action.createReverted())
        }
        // not uploaded yet
        else {
            delete(edit)
        }
    }

    /* ------------------------------------ add/sync/delete ------------------------------------- */

    private fun add(edit: ElementEdit) {
        editsDB.add(edit)
        val id = edit.id
        val createdElementsCount = edit.action.newElementsCount
        elementIdProviderDB.assign(
            id,
            createdElementsCount.nodes,
            createdElementsCount.ways,
            createdElementsCount.relations
        )
        onAddedEdit(edit)
    }


    private fun markSynced(edit: ElementEdit) {
        val id = edit.id
        elementIdProviderDB.delete(id)
        if (editsDB.markSynced(id)) {
            onSyncedEdit(edit)
        }
    }

    private fun delete(edit: ElementEdit) {
        val id = edit.id
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
