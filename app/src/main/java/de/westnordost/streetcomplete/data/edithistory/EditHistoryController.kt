package de.westnordost.streetcomplete.data.edithistory

import de.westnordost.streetcomplete.ApplicationConstants.MAX_UNDO_HISTORY_AGE
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import java.lang.System.currentTimeMillis
import java.util.TreeSet
import java.util.concurrent.CopyOnWriteArrayList

/** All edits done by the user in one place: Edits made on notes, on map data, hidings of quests */
class EditHistoryController(
    private val elementEditsController: ElementEditsController,
    private val noteEditsController: NoteEditsController,
    private val noteQuestController: OsmNoteQuestController,
    private val osmQuestController: OsmQuestController
) : EditHistorySource {
    private val listeners: MutableList<EditHistorySource.Listener> = CopyOnWriteArrayList()

    private val osmElementEditsListener = object : ElementEditsSource.Listener {
        override fun onAddedEdit(edit: ElementEdit) {
            if (edit.action !is IsRevertAction) onAdded(edit)
        }
        override fun onSyncedEdit(edit: ElementEdit) {
            if (edit.action !is IsRevertAction) onSynced(edit)
        }
        override fun onDeletedEdits(edits: List<ElementEdit>) {
            onDeleted(edits.filter { it.action !is IsRevertAction })
        }
    }

    private val osmNoteEditsListener = object : NoteEditsSource.Listener {
        override fun onAddedEdit(edit: NoteEdit) { onAdded(edit) }
        override fun onSyncedEdit(edit: NoteEdit) { onSynced(edit) }
        override fun onDeletedEdits(edits: List<NoteEdit>) { onDeleted(edits) }
    }

    private val osmNoteQuestHiddenListener = object : OsmNoteQuestController.HideOsmNoteQuestListener {
        override fun onHid(edit: OsmNoteQuestHidden) { onAdded(edit) }
        override fun onUnhid(edit: OsmNoteQuestHidden) { onDeleted(listOf(edit)) }
        override fun onUnhidAll() { onInvalidated() }
    }
    private val osmQuestHiddenListener = object : OsmQuestController.HideOsmQuestListener {
        override fun onHid(edit: OsmQuestHidden) { onAdded(edit) }
        override fun onUnhid(edit: OsmQuestHidden) { onDeleted(listOf(edit)) }
        override fun onUnhidAll() { onInvalidated() }
    }

    // todo: trimmer (though the cache will be small, and will fill up again quickly, so trim / clear might not be necessary)
    private val cache = TreeSet<Edit> { t, t2 ->
        t2.createdTimestamp.compareTo(t.createdTimestamp)
    }

    init {
        elementEditsController.addListener(osmElementEditsListener)
        noteEditsController.addListener(osmNoteEditsListener)
        noteQuestController.addHideQuestsListener(osmNoteQuestHiddenListener)
        osmQuestController.addHideQuestsListener(osmQuestHiddenListener)
    }

    fun undo(edit: Edit): Boolean {
        if (!edit.isUndoable) return false
        return when (edit) {
            is ElementEdit -> elementEditsController.undo(edit)
            is NoteEdit -> noteEditsController.undo(edit)
            is OsmNoteQuestHidden -> noteQuestController.unhide(edit.note.id)
            is OsmQuestHidden -> osmQuestController.unhide(edit.questKey)
            else -> throw IllegalArgumentException()
        }
    }

    fun deleteSyncedOlderThan(timestamp: Long): Int =
        elementEditsController.deleteSyncedOlderThan(timestamp) +
        noteEditsController.deleteSyncedOlderThan(timestamp)

    override fun get(key: EditKey): Edit? = getAll().firstOrNull { it.key == key }

    override fun getMostRecentUndoable(): Edit? =
        // this could be optimized later by not querying all. Though, the amount that is queried
        // from database should never be that big anyway...
        getAll().firstOrNull { it.isUndoable }

    override fun getAll(): List<Edit> = synchronized(cache) {
        if (cache.isNotEmpty()) return cache.toList() // TreeSet is already sorted!

        val maxAge = currentTimeMillis() - MAX_UNDO_HISTORY_AGE

        val result = ArrayList<Edit>()
        result += elementEditsController.getAll().filter { it.action !is IsRevertAction }
        result += noteEditsController.getAll()
        result += noteQuestController.getAllHiddenNewerThan(maxAge)
        result += osmQuestController.getAllHiddenNewerThan(maxAge)
        cache.addAll(result)

        return cache.toList()
    }

    override fun getCount(): Int =
        // could be optimized later too...
        cache.size

    override fun addListener(listener: EditHistorySource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: EditHistorySource.Listener) {
        listeners.remove(listener)
    }

    private fun onAdded(edit: Edit) {
        synchronized(cache) { cache.add(edit) }
        listeners.forEach { it.onAdded(edit) }
    }
    private fun onSynced(edit: Edit) {
        synchronized(cache) {
            if (edit is ElementEdit) cache.add(edit.copy(isSynced = true))
            if (edit is NoteEdit) cache.add(edit.copy(isSynced = true))
            cache.remove(edit) // can't be called for (un)hiding, so no need to care about it
        }
        listeners.forEach { it.onSynced(edit) }
    }
    private fun onDeleted(edits: List<Edit>) {
        synchronized(cache) { cache.removeAll(edits) }
        listeners.forEach { it.onDeleted(edits) }
    }
    private fun onInvalidated() {
        synchronized(cache) { cache.clear() }
        listeners.forEach { it.onInvalidated() }
    }
}
