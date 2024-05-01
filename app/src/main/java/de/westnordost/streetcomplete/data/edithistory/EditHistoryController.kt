package de.westnordost.streetcomplete.data.edithistory

import de.westnordost.streetcomplete.ApplicationConstants.MAX_UNDO_HISTORY_AGE
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenSource
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestsHiddenController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestsHiddenSource
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds

/** All edits done by the user in one place: Edits made on notes, on map data, hidings of quests */
class EditHistoryController(
    private val elementEditsController: ElementEditsController,
    private val noteEditsController: NoteEditsController,
    private val osmNoteQuestsHiddenController: OsmNoteQuestsHiddenController,
    private val osmQuestsHiddenController: OsmQuestsHiddenController
) : EditHistorySource {
    private val listeners = Listeners<EditHistorySource.Listener>()

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

    private val osmNoteQuestHiddenListener = object : OsmNoteQuestsHiddenSource.Listener {
        override fun onHid(edit: OsmNoteQuestHidden) { onAdded(edit) }
        override fun onUnhid(edit: OsmNoteQuestHidden) { onDeleted(listOf(edit)) }
        override fun onUnhidAll() { onInvalidated() }
    }
    private val osmQuestHiddenListener = object : OsmQuestsHiddenSource.Listener {
        override fun onHid(edit: OsmQuestHidden) { onAdded(edit) }
        override fun onUnhid(edit: OsmQuestHidden) { onDeleted(listOf(edit)) }
        override fun onUnhidAll() { onInvalidated() }
    }

    init {
        elementEditsController.addListener(osmElementEditsListener)
        noteEditsController.addListener(osmNoteEditsListener)
        osmNoteQuestsHiddenController.addListener(osmNoteQuestHiddenListener)
        osmQuestsHiddenController.addListener(osmQuestHiddenListener)
    }

    fun undo(editKey: EditKey): Boolean {
        val edit = get(editKey) ?: return false
        if (!edit.isUndoable) return false
        return when (edit) {
            is ElementEdit -> elementEditsController.undo(edit)
            is NoteEdit -> noteEditsController.undo(edit)
            is OsmNoteQuestHidden -> osmNoteQuestsHiddenController.unhide(edit.note.id)
            is OsmQuestHidden -> osmQuestsHiddenController.unhide(edit.questKey)
            else -> throw IllegalArgumentException()
        }
    }

    fun deleteSyncedOlderThan(timestamp: Long): Int =
        elementEditsController.deleteSyncedOlderThan(timestamp) +
        noteEditsController.deleteSyncedOlderThan(timestamp)

    override fun get(key: EditKey): Edit? = when (key) {
        is ElementEditKey -> elementEditsController.get(key.id)
        is NoteEditKey -> noteEditsController.get(key.id)
        is OsmNoteQuestHiddenKey -> osmNoteQuestsHiddenController.getHidden(key.osmNoteQuestKey.noteId)
        is OsmQuestHiddenKey -> osmQuestsHiddenController.getHidden(key.osmQuestKey)
    }

    override fun getAll(): List<Edit> {
        val maxAge = nowAsEpochMilliseconds() - MAX_UNDO_HISTORY_AGE

        val result = ArrayList<Edit>()
        result += elementEditsController.getAll().filter { it.action !is IsRevertAction }
        result += noteEditsController.getAll()
        result += osmNoteQuestsHiddenController.getAllHiddenNewerThan(maxAge)
        result += osmQuestsHiddenController.getAllHiddenNewerThan(maxAge)

        result.sortByDescending { it.createdTimestamp }
        return result
    }

    override fun getCount(): Int =
        // could be optimized later too...
        getAll().size

    override fun addListener(listener: EditHistorySource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: EditHistorySource.Listener) {
        listeners.remove(listener)
    }

    private fun onAdded(edit: Edit) {
        listeners.forEach { it.onAdded(edit) }
    }
    private fun onSynced(edit: Edit) {
        listeners.forEach { it.onSynced(edit) }
    }
    private fun onDeleted(edits: List<Edit>) {
        listeners.forEach { it.onDeleted(edits) }
    }
    private fun onInvalidated() {
        listeners.forEach { it.onInvalidated() }
    }
}
