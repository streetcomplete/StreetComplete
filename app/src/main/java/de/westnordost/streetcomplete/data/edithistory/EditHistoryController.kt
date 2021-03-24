package de.westnordost.streetcomplete.data.edithistory

import de.westnordost.streetcomplete.ApplicationConstants.MAX_UNDO_HISTORY_AGE
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestKey
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import java.lang.System.currentTimeMillis
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

// TODO ADD TESTS!
/** All edits done by the user in one place: Edits made on notes, on map data, hidings of quests */
class EditHistoryController @Inject constructor(
    private val elementEditsController: ElementEditsController,
    private val noteEditsController: NoteEditsController,
    private val noteQuestController: OsmNoteQuestController,
    private val osmQuestController: OsmQuestController
): UndoablesSource {

    private val listeners: MutableList<UndoablesSource.Listener> = CopyOnWriteArrayList()

    private val osmElementEditsListener = object : ElementEditsSource.Listener {
        override fun onAddedEdit(edit: ElementEdit) { onAdded(edit) }
        override fun onSyncedEdit(edit: ElementEdit) { onSynced(edit) }
        override fun onDeletedEdit(edit: ElementEdit) { onDeleted(edit) }
    }

    private val osmNoteEditsListener = object : NoteEditsSource.Listener {
        override fun onAddedEdit(edit: NoteEdit) { onAdded(edit) }
        override fun onSyncedEdit(edit: NoteEdit) { onSynced(edit) }
        override fun onDeletedEdit(edit: NoteEdit) { onDeleted(edit) }
    }

    // TODO listeners on hidden quests!

    init {
        elementEditsController.addListener(osmElementEditsListener)
        noteEditsController.addListener(osmNoteEditsListener)
    }

    fun undo(edit: Edit): Boolean {
        if (!edit.isUndoable) return false
        return when(edit) {
            is ElementEdit -> elementEditsController.undo(edit)
            is NoteEdit -> noteEditsController.undo(edit)
            is OsmNoteQuestHidden -> noteQuestController.unhide(edit.note.id)
            is OsmQuestHidden -> osmQuestController.unhide(
                OsmQuestKey(edit.elementType, edit.elementId, edit.quesType::class.simpleName!!)
            )
            else -> throw IllegalArgumentException()
        }
    }

    override fun getMostRecentUndoable(): Edit? =
        // this could be optimized later by not querying all. Though, the amount that is queried
        // from database should never be that big anyway...
        getAll().firstOrNull { it.isUndoable }

    override fun getAll(): List<Edit> {
        val maxAge = currentTimeMillis() - MAX_UNDO_HISTORY_AGE

        val result = ArrayList<Edit>()
        result += elementEditsController.getAll()
        result += noteEditsController.getAll()
        result += noteQuestController.getAllHiddenNewerThan(maxAge)
        result += osmQuestController.getAllHiddenNewerThan(maxAge)

        result.sortByDescending { it.createdTimestamp }
        return result
    }

    override fun addListener(listener: UndoablesSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: UndoablesSource.Listener) {
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
