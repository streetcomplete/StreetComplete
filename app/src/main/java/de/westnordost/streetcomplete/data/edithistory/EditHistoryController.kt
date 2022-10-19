package de.westnordost.streetcomplete.data.edithistory

import de.westnordost.streetcomplete.ApplicationConstants.MAX_UNDO_HISTORY_AGE
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.delete.RevertDeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.RevertUpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestController
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestHidden
import java.util.TreeSet
import java.util.concurrent.CopyOnWriteArrayList

/** All edits done by the user in one place: Edits made on notes, on map data, hidings of quests */
class EditHistoryController(
    private val elementEditsController: ElementEditsController,
    private val noteEditsController: NoteEditsController,
    private val noteQuestController: OsmNoteQuestController,
    private val osmQuestController: OsmQuestController,
    private val otherSourceQuestController: OtherSourceQuestController,
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
    private val otherSourceQuestHiddenListener = object : OtherSourceQuestController.HideQuestListener {
        override fun onHid(edit: OtherSourceQuestHidden) { onAdded(edit) }
        override fun onUnhid(edit: OtherSourceQuestHidden) { onDeleted(listOf(edit)) }
        override fun onUnhidAll() { onInvalidated() }
    }

    private val cache by lazy {
        TreeSet<Edit> { t, t2 ->
            t2.createdTimestamp.compareTo(t.createdTimestamp)
        }.apply { addAll(fetchAll()) }
    }

    init {
        elementEditsController.addListener(osmElementEditsListener)
        noteEditsController.addListener(osmNoteEditsListener)
        noteQuestController.addHideQuestsListener(osmNoteQuestHiddenListener)
        osmQuestController.addHideQuestsListener(osmQuestHiddenListener)
        otherSourceQuestController.addHideListener(otherSourceQuestHiddenListener)
    }

    fun undo(edit: Edit): Boolean {
        if (!edit.isUndoable) return false
        return when (edit) {
            is ElementEdit -> elementEditsController.undo(edit)
            is NoteEdit -> noteEditsController.undo(edit)
            is OsmNoteQuestHidden -> noteQuestController.unhide(edit.note.id)
            is OsmQuestHidden -> osmQuestController.unhide(edit.questKey)
            is OtherSourceQuestHidden -> otherSourceQuestController.unhide(edit.questKey)
            else -> throw IllegalArgumentException()
        }
    }

    private fun fetchAll(): List<Edit> {
        val maxAge = nowAsEpochMilliseconds() - MAX_UNDO_HISTORY_AGE

        val result = ArrayList<Edit>()
        result += elementEditsController.getAll().filter { it.action !is IsRevertAction }
        result += noteEditsController.getAll()
        result += noteQuestController.getAllHiddenNewerThan(maxAge)
        result += osmQuestController.getAllHiddenNewerThan(maxAge)
        result += otherSourceQuestController.getAllHiddenNewerThan(maxAge)
        return result
    }

    fun deleteSyncedOlderThan(timestamp: Long): Int {
        val r = elementEditsController.deleteSyncedOlderThan(timestamp) +
            noteEditsController.deleteSyncedOlderThan(timestamp)
        synchronized(cache) { // just reset the cache, this doesn't happen often anyway
            cache.clear()
            cache.addAll(fetchAll())
        }
        return r
    }

    override fun get(key: EditKey): Edit? {
        val edit = getAll().firstOrNull { it.key == key }
        if (edit != null) return edit
        return when (key) {
            is OsmQuestHiddenKey -> osmQuestController.getHidden(key.osmQuestKey)
            is OsmNoteQuestHiddenKey -> noteQuestController.getHidden(key.osmNoteQuestKey.noteId)
            is OtherSourceQuestHiddenKey -> otherSourceQuestController.getHidden(key.otherSourceQuestKey)
            else -> null
        }
    }

    override fun getMostRecentUndoable(): Edit? =
        // this could be optimized later by not querying all. Though, the amount that is queried
        // from database should never be that big anyway...
        getAll().firstOrNull { it.isUndoable }

    // difference to upstream: may contain older hidden quests
    // but that really doesn't matter
    override fun getAll(allHidden: Boolean): List<Edit> =

        if (allHidden)
            (noteQuestController.getAllHiddenNewerThan(0L)
                + osmQuestController.getAllHiddenNewerThan(0L)
                + otherSourceQuestController.getAllHiddenNewerThan(0L)
            ).sortedByDescending { it.createdTimestamp }
        else synchronized(cache) { cache.toList() }

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
            if (edit is ElementEdit && edit.action !in noIdModifyActions) {
                // reload, because element ids of multiple edits may have changed after split way
                // this should never happen for other edit types, as they don't affect more than
                // a single element
                // or maybe remove this cache and do separate caches for all controllers?
                cache.apply {
                    removeAll { it is ElementEdit }
                    addAll(elementEditsController.getAll())
                }
                Unit // make compiler happy
            } else {
                if (!cache.remove(edit)) // remove first is really important!
                    cache.removeAll { it.key == edit.key } // fallback, never found it triggered
                if (edit is ElementEdit) cache.add(edit.copy(isSynced = true))
                if (edit is NoteEdit) cache.add(edit.copy(isSynced = true))
                // can't be called for (un)hiding, so no need to care about it
            }
        }
        listeners.forEach { it.onSynced(edit) }
    }
    private fun onDeleted(edits: List<Edit>) {
        val keys = edits.map { it.key }.toHashSet()
        synchronized(cache) { cache.removeAll { it.key in keys } }
        listeners.forEach { it.onDeleted(edits) }
    }
    private fun onInvalidated() {
        synchronized(cache) { cache.clear() }
        listeners.forEach { it.onInvalidated() }
    }
}

private val noIdModifyActions = setOf(UpdateElementTagsAction, DeletePoiNodeAction, RevertDeletePoiNodeAction, RevertUpdateElementTagsAction)
