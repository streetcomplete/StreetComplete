package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.notes.Note
import java.lang.System.currentTimeMillis
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class NoteEditsController @Inject constructor(
    private val editsDB: NoteEditsDao
): NoteEditsSource {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    private val listeners: MutableList<NoteEditsSource.Listener> = CopyOnWriteArrayList()

    @Synchronized fun add(
        noteId: Long,
        action: NoteEditAction,
        position: LatLon,
        text: String? = null,
        imagePaths: List<String> = emptyList()
    ) {
        val edit = NoteEdit(
            0,
            noteId,
            position,
            action,
            text,
            imagePaths,
            currentTimeMillis(),
            false,
            imagePaths.isNotEmpty()
        )
        editsDB.add(edit)
        onAddedEdit(edit)
    }

    fun get(id: Long): NoteEdit? =
        editsDB.get(id)

    override fun getAllUnsynced(): List<NoteEdit> =
        editsDB.getAllUnsynced()

    fun getAll(): List<NoteEdit> =
        editsDB.getAll()

    fun getOldestUnsynced(): NoteEdit? =
        editsDB.getOldestUnsynced()

    override fun getUnsyncedCount(): Int =
        editsDB.getUnsyncedCount()

    override fun getAllUnsyncedForNote(noteId: Long): List<NoteEdit> =
        editsDB.getAllUnsyncedForNote(noteId)

    override fun getAllUnsyncedForNotes(noteIds: Collection<Long>): List<NoteEdit> =
        editsDB.getAllUnsyncedForNotes(noteIds)

    override fun getAllUnsynced(bbox: BoundingBox): List<NoteEdit> =
        editsDB.getAllUnsynced(bbox)

    override fun getAllUnsyncedPositions(bbox: BoundingBox): List<LatLon> =
        editsDB.getAllUnsyncedPositions(bbox)

    fun getOldestNeedingImagesActivation(): NoteEdit? =
        editsDB.getOldestNeedingImagesActivation()

    @Synchronized fun imagesActivated(id: Long): Boolean =
        editsDB.markImagesActivated(id)

    @Synchronized fun synced(edit: NoteEdit, note: Note) {
        if (edit.noteId != note.id) {
            editsDB.updateNoteId(edit.noteId, note.id)
        }
        if (editsDB.markSynced(edit.id)) {
            onSyncedEdit(edit)
        }
    }

    @Synchronized fun syncFailed(edit: NoteEdit) {
        delete(edit)
    }

    @Synchronized fun undo(edit: NoteEdit): Boolean {
        return delete(edit)
    }

    @Synchronized fun deleteSyncedOlderThan(timestamp: Long): Int {
        val edits = editsDB.getSyncedOlderThan(timestamp)
        val result = editsDB.deleteAll(edits.map { it.id })
        onDeletedEdits(edits)
        return result
    }

    private fun delete(edit: NoteEdit): Boolean {
        if (editsDB.delete(edit.id)) {
            onDeletedEdits(listOf(edit))
            return false
        }
        return true
    }

    /* ------------------------------------ Listeners ------------------------------------------- */

    override fun addListener(listener: NoteEditsSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: NoteEditsSource.Listener) {
        listeners.remove(listener)
    }

    private fun onAddedEdit(edit: NoteEdit) {
        listeners.forEach { it.onAddedEdit(edit) }
    }

    private fun onSyncedEdit(edit: NoteEdit) {
        listeners.forEach { it.onSyncedEdit(edit) }
    }

    private fun onDeletedEdits(edits: List<NoteEdit>) {
        listeners.forEach { it.onDeletedEdits(edits) }
    }
}
