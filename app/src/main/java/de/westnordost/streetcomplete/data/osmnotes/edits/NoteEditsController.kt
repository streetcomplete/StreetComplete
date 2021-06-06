package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.Note
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

    fun add(
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
        synchronized(this) { editsDB.add(edit) }
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

    fun imagesActivated(id: Long): Boolean =
        synchronized(this) { editsDB.markImagesActivated(id) }

    fun synced(edit: NoteEdit, note: Note) {
        val markSyncedSuccess: Boolean
        synchronized(this) {
            if (edit.noteId != note.id) {
                editsDB.updateNoteId(edit.noteId, note.id)
            }
            markSyncedSuccess = editsDB.markSynced(edit.id)
        }

        if (markSyncedSuccess) {
            onSyncedEdit(edit)
        }
    }

    fun syncFailed(edit: NoteEdit): Boolean =
        delete(edit)

    fun undo(edit: NoteEdit): Boolean =
        delete(edit)

    fun deleteSyncedOlderThan(timestamp: Long): Int {
        val deletedCount: Int
        val deleteEdits: List<NoteEdit>
        synchronized(this) {
            deleteEdits = editsDB.getSyncedOlderThan(timestamp)
            if (deleteEdits.isEmpty()) return 0
            deletedCount = editsDB.deleteAll(deleteEdits.map { it.id })
        }
        onDeletedEdits(deleteEdits)
        return deletedCount
    }

    private fun delete(edit: NoteEdit): Boolean {
        val deleteSuccess = synchronized(this) { editsDB.delete(edit.id) }
        if (deleteSuccess) {
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
