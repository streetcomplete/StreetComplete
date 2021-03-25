package de.westnordost.streetcomplete.data.edithistory

import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

/** All edits done by the user in one place: Edits made on notes, on map data, hidings of quests */
class EditHistoryController @Inject constructor(
    private val elementEditsController: ElementEditsController,
    private val noteEditsController: NoteEditsController
): EditHistorySource {

    private val listeners: MutableList<EditHistorySource.Listener> = CopyOnWriteArrayList()

    private val osmElementEditsListener = object : ElementEditsSource.Listener {
        override fun onAddedEdit(edit: ElementEdit) {
            if (edit.action !is IsRevertAction) onAdded(edit)
        }
        override fun onSyncedEdit(edit: ElementEdit) {
            if (edit.action !is IsRevertAction) onSynced(edit)
        }
        override fun onDeletedEdit(edit: ElementEdit) {
            if (edit.action !is IsRevertAction) onDeleted(edit)
        }
    }

    private val osmNoteEditsListener = object : NoteEditsSource.Listener {
        override fun onAddedEdit(edit: NoteEdit) { onAdded(edit) }
        override fun onSyncedEdit(edit: NoteEdit) { onSynced(edit) }
        override fun onDeletedEdit(edit: NoteEdit) { onDeleted(edit) }
    }

    init {
        elementEditsController.addListener(osmElementEditsListener)
        noteEditsController.addListener(osmNoteEditsListener)
    }

    fun undo(edit: Edit): Boolean {
        if (!edit.isUndoable) return false
        return when(edit) {
            is ElementEdit -> elementEditsController.undo(edit)
            is NoteEdit -> noteEditsController.undo(edit)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getMostRecentUndoable(): Edit? =
        // this could be optimized later by not querying all. Though, the amount that is queried
        // from database should never be that big anyway...
        getAll().firstOrNull { it.isUndoable }

    override fun getAll(): List<Edit> {
        val result = ArrayList<Edit>()
        result += elementEditsController.getAll().filter { it.action !is IsRevertAction }
        result += noteEditsController.getAll()
        result.sortByDescending { it.createdTimestamp }
        return result
    }

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
    private fun onDeleted(edit: Edit) {
        listeners.forEach { it.onDeleted(edit) }
    }
}
