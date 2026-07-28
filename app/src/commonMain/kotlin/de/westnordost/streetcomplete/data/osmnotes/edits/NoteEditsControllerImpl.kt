package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

class NoteEditsControllerImpl(
    private val editsDB: NoteEditsDao
) : NoteEditsController {

    private val listeners = Listeners<NoteEditsSource.Listener>()

    private val lock = ReentrantLock()

    override fun add(
        noteId: Long,
        action: NoteEditAction,
        position: LatLon,
        text: String?,
        imagePaths: List<String>,
        track: List<Trackpoint>,
    ) {
        val edit = NoteEdit(
            0,
            noteId,
            position,
            action,
            text,
            imagePaths,
            nowAsEpochMilliseconds(),
            false,
            imagePaths.isNotEmpty(),
            track,
        )
        lock.withLock { editsDB.add(edit) }
        onAddedEdit(edit)
    }

    override fun get(id: Long): NoteEdit? =
        editsDB.get(id)

    override fun getAllUnsynced(): List<NoteEdit> =
        editsDB.getAllUnsynced()

    override fun getAll(): List<NoteEdit> =
        editsDB.getAll()

    override fun getOldestUnsynced(): NoteEdit? =
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

    override fun getOldestNeedingImagesActivation(): NoteEdit? =
        editsDB.getOldestNeedingImagesActivation()

    override fun markImagesActivated(id: Long): Boolean =
        lock.withLock { editsDB.markImagesActivated(id) }

    override fun markSynced(edit: NoteEdit, note: Note) {
        var markSyncedSuccess = false
        lock.withLock {
            if (edit.noteId != note.id) {
                editsDB.updateNoteId(edit.noteId, note.id)
            }
            markSyncedSuccess = editsDB.markSynced(edit.id)
        }

        if (markSyncedSuccess) {
            onSyncedEdit(edit.copy(isSynced = true))
        }
    }

    override fun markSyncFailed(edit: NoteEdit): Boolean =
        delete(edit)

    override fun undo(edit: NoteEdit): Boolean =
        delete(edit)

    override fun deleteSyncedOlderThan(timestamp: Long): Int {
        var deletedCount = 0
        var deleteEdits = listOf<NoteEdit>()
        lock.withLock {
            deleteEdits = editsDB.getSyncedOlderThan(timestamp)
            if (deleteEdits.isEmpty()) return 0
            deletedCount = editsDB.deleteAll(deleteEdits.map { it.id })
        }
        onDeletedEdits(deleteEdits)
        return deletedCount
    }

    private fun delete(edit: NoteEdit): Boolean {
        val deleteSuccess = lock.withLock { editsDB.delete(edit.id) }
        if (deleteSuccess) {
            onDeletedEdits(listOf(edit))
            return false
        }
        return true
    }

    override fun updateElementIds(idUpdates: Collection<ElementIdUpdate>) {
        for (idUpdate in idUpdates) {
            val elementType = idUpdate.elementType.name.lowercase()
            editsDB.replaceTextInUnsynced(
                "osm.org/$elementType/${idUpdate.oldElementId} ",
                "osm.org/$elementType/${idUpdate.newElementId} ",
            )
        }
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
