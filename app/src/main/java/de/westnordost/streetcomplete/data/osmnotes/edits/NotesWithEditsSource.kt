package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.NoteComment
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.COMMENT
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.CREATE
import de.westnordost.streetcomplete.data.user.User
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.util.Listeners

class NotesWithEditsSource(
    private val noteController: NoteController,
    private val noteEditsSource: NoteEditsSource,
    private val userDataSource: UserDataSource
) {
    /** Interface to be notified of new notes, updated notes and notes that have been deleted */
    interface Listener {
        fun onUpdated(added: Collection<Note>, updated: Collection<Note>, deleted: Collection<Long>)

        fun onCleared()
    }
    private val listeners = Listeners<Listener>()

    private val noteControllerListener = object : NoteController.Listener {
        override fun onUpdated(added: Collection<Note>, updated: Collection<Note>, deleted: Collection<Long>) {
            /* not include note creations: This is the update from the server, the update does not
             * include the new notes we added ourselves. Implementation here would only need to be
             * changed if action type "REOPEN" or "CLOSE" is implemented */
            val noteCommentEdits = noteEditsSource.getAllUnsynced().filter { it.action != CREATE }
            callOnUpdated(
                editsAppliedToNotes(added, noteCommentEdits),
                editsAppliedToNotes(updated, noteCommentEdits),
                deleted
            )
        }

        override fun onCleared() {
            callOnCleared()
        }
    }

    private val noteEditsListener = object : NoteEditsSource.Listener {
        override fun onAddedEdit(edit: NoteEdit) {
            /* can't just get the associated note from DB and apply this edit to it because this
             * edit might just be the last in a long chain of edits, i.e. if several comments
             * are added to a note, or if a note is created through an edit (and then commented) */
            val note = get(edit.noteId) ?: return

            if (edit.action == CREATE) {
                callOnUpdated(added = listOf(note))
            } else {
                callOnUpdated(updated = listOf(note))
            }
        }

        override fun onSyncedEdit(edit: NoteEdit) {
            /* do nothing: If the change was synced successfully, it means that our local change
               was accepted by the server. There will also be a call to onUpdated
               in NoteController.Listener any moment now */
        }

        override fun onDeletedEdits(edits: List<NoteEdit>) {
            callOnUpdated(
                updated = edits.filter { it.action != CREATE }.mapNotNull { get(it.noteId) },
                deleted = edits.filter { it.action == CREATE }.map { it.noteId }
            )
        }
    }

    init {
        noteController.addListener(noteControllerListener)
        noteEditsSource.addListener(noteEditsListener)
    }

    fun get(noteId: Long): Note? {
        val noteEdits = noteEditsSource.getAllUnsyncedForNote(noteId)
        var note = noteController.get(noteId)
        for (noteEdit in noteEdits) {
            when (noteEdit.action) {
                CREATE -> {
                    if (note == null) note = noteEdit.createNote()
                }
                COMMENT -> {
                    if (note != null) {
                        note = note.copy(comments = note.comments + noteEdit.createNoteComment())
                    }
                }
            }
        }
        return note
    }

    fun getAllPositions(bbox: BoundingBox): List<LatLon> =
        noteController.getAllPositions(bbox) +
        noteEditsSource.getAllUnsyncedPositions(bbox)

    fun getAll(bbox: BoundingBox): Collection<Note> =
        editsAppliedToNotes(
            noteController.getAll(bbox),
            noteEditsSource.getAllUnsynced(bbox)
        )

    fun getAll(noteIds: Collection<Long>): Collection<Note> =
        editsAppliedToNotes(
            noteController.getAll(noteIds),
            noteEditsSource.getAllUnsyncedForNotes(noteIds)
        )

    /** returns collection with modified notes */
    private fun editsAppliedToNotes(originalNotes: Collection<Note>, noteEdits: List<NoteEdit>): Collection<Note> {
        if (originalNotes.isEmpty() && noteEdits.isEmpty()) return originalNotes

        val notesById = HashMap<Long, Note>(originalNotes.size)
        originalNotes.associateByTo(notesById) { it.id }

        for (noteEdit in noteEdits) {
            val id = noteEdit.noteId
            when (noteEdit.action) {
                CREATE -> {
                    if (!notesById.containsKey(id)) notesById[id] = noteEdit.createNote()
                }
                COMMENT -> {
                    val note = notesById[id]
                    if (note != null) {
                        notesById[id] = note.copy(comments = note.comments + noteEdit.createNoteComment())
                    }
                }
            }
        }
        return notesById.values
    }

    private fun NoteEdit.createNote() = Note(
        position,
        noteId,
        createdTimestamp,
        null,
        Note.Status.OPEN,
        arrayListOf(createNoteComment(NoteComment.Action.OPENED))
    )

    private fun NoteEdit.createNoteComment(action: NoteComment.Action = NoteComment.Action.COMMENTED): NoteComment {
        var commentText = text ?: ""
        if (imagePaths.isNotEmpty()) {
            commentText += "\n\n(Photo(s) will be attached on upload)"
        }

        return NoteComment(
            createdTimestamp,
            action,
            commentText,
            User(userDataSource.userId, userDataSource.userName ?: "")
        )
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun callOnUpdated(added: Collection<Note> = emptyList(), updated: Collection<Note> = emptyList(), deleted: Collection<Long> = emptyList()) {
        listeners.forEach { it.onUpdated(added, updated, deleted) }
    }

    private fun callOnCleared() {
        listeners.forEach { it.onCleared() }
    }
}
