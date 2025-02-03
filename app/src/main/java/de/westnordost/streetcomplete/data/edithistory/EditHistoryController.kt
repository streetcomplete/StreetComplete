package de.westnordost.streetcomplete.data.edithistory

import de.westnordost.streetcomplete.ApplicationConstants.MAX_UNDO_HISTORY_AGE
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenController
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenSource
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds

/** All edits done by the user in one place: Edits made on notes, on map data, hidings of quests */
class EditHistoryController(
    private val elementEditsController: ElementEditsController,
    private val noteEditsController: NoteEditsController,
    private val hiddenQuestsController: QuestsHiddenController,
    private val notesSource: NotesWithEditsSource,
    private val mapDataSource: MapDataWithEditsSource,
    private val questTypeRegistry: QuestTypeRegistry,
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

    private val questHiddenListener = object : QuestsHiddenSource.Listener {
        override fun onHid(key: QuestKey, timestamp: Long) {
            val edit = createQuestHiddenEdit(key, timestamp)
            if (edit != null) onAdded(edit)
        }
        override fun onUnhid(key: QuestKey, timestamp: Long) {
            val edit = createQuestHiddenEdit(key, timestamp)
            if (edit != null) onDeleted(listOf(edit))
        }
        override fun onUnhidAll() { onInvalidated() }
    }

    private fun createQuestHiddenEdit(key: QuestKey, timestamp: Long): Edit? {
        return when (key) {
            is OsmNoteQuestKey -> {
                val note = notesSource.get(key.noteId) ?: return null
                OsmNoteQuestHidden(note, timestamp)
            }
            is OsmQuestKey -> {
                val geometry = mapDataSource.getGeometry(key.elementType, key.elementId) ?: return null
                val questType = questTypeRegistry.getByName(key.questTypeName) as? OsmElementQuestType<*> ?: return null
                OsmQuestHidden(key.elementType, key.elementId, questType, geometry, timestamp)
            }
        }
    }

    init {
        elementEditsController.addListener(osmElementEditsListener)
        noteEditsController.addListener(osmNoteEditsListener)
        hiddenQuestsController.addListener(questHiddenListener)
    }

    fun undo(editKey: EditKey): Boolean {
        val edit = get(editKey) ?: return false
        if (!edit.isUndoable) return false
        return when (edit) {
            is ElementEdit -> elementEditsController.undo(edit)
            is NoteEdit -> noteEditsController.undo(edit)
            is OsmNoteQuestHidden -> hiddenQuestsController.unhide(edit.questKey)
            is OsmQuestHidden -> hiddenQuestsController.unhide(edit.questKey)
            else -> throw IllegalArgumentException()
        }
    }

    fun deleteSyncedOlderThan(timestamp: Long): Int =
        elementEditsController.deleteSyncedOlderThan(timestamp) +
        noteEditsController.deleteSyncedOlderThan(timestamp)

    override fun get(key: EditKey): Edit? = when (key) {
        is ElementEditKey -> elementEditsController.get(key.id)
        is NoteEditKey -> noteEditsController.get(key.id)
        is QuestHiddenKey -> {
            val timestamp = hiddenQuestsController.get(key.questKey)
            if (timestamp != null) createQuestHiddenEdit(key.questKey, timestamp) else null
        }
    }

    override fun getAll(): List<Edit> {
        val maxAge = nowAsEpochMilliseconds() - MAX_UNDO_HISTORY_AGE

        val result = ArrayList<Edit>()
        result += elementEditsController.getAll().filter { it.action !is IsRevertAction }
        result += noteEditsController.getAll()
        result += hiddenQuestsController.getAllNewerThan(maxAge).mapNotNull { (key, timestamp) ->
            createQuestHiddenEdit(key, timestamp)
        }

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
