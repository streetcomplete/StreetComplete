package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.NoteComment
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.NoteSource
import de.westnordost.streetcomplete.data.user.User
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.comment
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.noteEdit
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NotesWithEditsSourceImplTest {

    private lateinit var src: NotesWithEditsSource
    private lateinit var noteController: NoteController
    private lateinit var noteListener: NoteSource.Listener
    private lateinit var noteEditsSource: NoteEditsSource
    private lateinit var noteEditsListener: NoteEditsSource.Listener
    private lateinit var userDataSource: UserDataSource

    private val bbox = bbox()

    val user = User(id = 23, displayName = "test user")

    @BeforeTest fun setUp() {
        noteController = mock() {
            every { addListener(any()) } calls { (listener: NoteSource.Listener) ->
                noteListener = listener
            }
        }
        noteEditsSource = mock() {
            every { addListener(any()) } calls { (listener: NoteEditsSource.Listener) ->
                noteEditsListener = listener
            }
        }
        userDataSource = mock() {
            every { userId } returns 23
            every { userName } returns "test user"
        }

        src = NotesWithEditsSourceImpl(noteController, noteEditsSource, userDataSource)
    }

    //region get

    @Test
    fun `get returns nothing`() {
        every { noteController.get(1) } returns null
        every { noteEditsSource.getAllUnsyncedForNote(1) } returns emptyList()

        assertNull(src.get(1))
    }

    @Test
    fun `get returns original note`() {
        val note = note(1)
        every { noteController.get(1) } returns note
        every { noteEditsSource.getAllUnsyncedForNote(1) } returns emptyList()

        assertEquals(note, src.get(1))
    }

    @Test
    fun `get returns updated note with anonymous user`() {
        val comment = comment("test", NoteComment.Action.OPENED, timestamp = 100)
        val addedComment = comment("test2", NoteComment.Action.COMMENTED, timestamp = 500, user = user)

        val note = note(id = 1, comments = listOf(comment))
        val edits = listOf(
            noteEdit(noteId = 1, action = NoteEditAction.COMMENT, text = "test2", timestamp = 500)
        )

        every { noteController.get(1) } returns note
        every { noteEditsSource.getAllUnsyncedForNote(1) } returns edits

        assertEquals(listOf(comment, addedComment), src.get(1)!!.comments)
    }

    @Test
    fun `get returns updated note with attached images placeholder text`() {
        val comment = comment("test", NoteComment.Action.OPENED, timestamp = 123)
        val addedComment = comment(
            "test2\n\n(Photo(s) will be attached on upload)",
            NoteComment.Action.COMMENTED,
            timestamp = 500,
            user = user
        )

        val note = note(id = 1, comments = listOf(comment))
        val edits = listOf(noteEdit(
            noteId = 1,
            action = NoteEditAction.COMMENT,
            text = "test2",
            imagePaths = listOf("something"),
            timestamp = 500
        ))

        every { noteController.get(1) } returns note
        every { noteEditsSource.getAllUnsyncedForNote(1) } returns edits

        assertEquals(listOf(comment, addedComment), src.get(1)!!.comments)
    }

    @Test
    fun `get returns updated note`() {
        val comment1 = comment("test", NoteComment.Action.OPENED, timestamp = 123)
        val comment2 = comment("test2", NoteComment.Action.COMMENTED, timestamp = 500, user = User(23, "test user"))

        val note = note(id = 1, comments = listOf(comment1))
        val edits = listOf(
            noteEdit(noteId = 1, action = NoteEditAction.COMMENT, text = "test2", timestamp = 500)
        )

        every { noteController.get(1) } returns note
        every { noteEditsSource.getAllUnsyncedForNote(1) } returns edits

        assertEquals(listOf(comment1, comment2), src.get(1)!!.comments)
    }

    @Test
    fun `get returns note with anonymous user updated twice`() {
        val comment1 = comment("test", NoteComment.Action.OPENED, timestamp = 123)
        val comment2 = comment("test2", NoteComment.Action.COMMENTED, timestamp = 500, user = user)
        val comment3 = comment("test3", NoteComment.Action.COMMENTED, timestamp = 800, user = user)

        val note = note(id = 1, comments = listOf(comment1))
        val edits = listOf(
            noteEdit(noteId = 1, action = NoteEditAction.COMMENT, text = "test2", timestamp = 500),
            noteEdit(noteId = 1, action = NoteEditAction.COMMENT, text = "test3", timestamp = 800),
        )

        every { noteController.get(1) } returns note
        every { noteEditsSource.getAllUnsyncedForNote(1) } returns edits

        assertEquals(listOf(comment1, comment2, comment3), src.get(1)!!.comments)
    }

    @Test
    fun `get returns created note`() {
        val p = p(12.0, 46.0)
        val expectedNote = note(
            id = -12,
            comments = listOf(
                comment("test12", NoteComment.Action.OPENED, timestamp = 123, user = user)
            ),
            position = p,
            timestamp = 123
        )

        val edits = listOf(
            noteEdit(noteId = -12, pos = p, action = NoteEditAction.CREATE, text = "test12", timestamp = 123)
        )

        every { noteController.get(1) } returns null
        every { noteEditsSource.getAllUnsyncedForNote(1) } returns edits

        assertEquals(expectedNote, src.get(1)!!)
    }

    @Test
    fun `get returns created and then commented note`() {
        val p = p(12.0, 46.0)
        val expectedNote = note(
            id = -12,
            comments = listOf(
                comment("test12", NoteComment.Action.OPENED, timestamp = 123, user = user),
                comment("test34", NoteComment.Action.COMMENTED, timestamp = 234, user = user),
            ),
            position = p,
            timestamp = 123
        )

        val edits = listOf(
            noteEdit(noteId = -12, pos = p, action = NoteEditAction.CREATE, text = "test12", timestamp = 123),
            noteEdit(noteId = -12, pos = p, action = NoteEditAction.COMMENT, text = "test34", timestamp = 234),
        )

        every { noteController.get(1) } returns null
        every { noteEditsSource.getAllUnsyncedForNote(1) } returns edits

        assertEquals(expectedNote, src.get(1)!!)
    }

    //endregion

    //region getAllPositions

    @Test
    fun getAllPositions() {
        val ps1 = listOf(p(3.0, 2.0), p(1.0, 3.0))
        val ps2 = listOf(p(3.0, 2.0), p(5.0, 1.0))

        every { noteController.getAllPositions(any()) } returns ps1
        every { noteEditsSource.getAllUnsyncedPositions(any()) } returns ps2

        val positions = src.getAllPositions(bbox)

        assertTrue(positions.containsAll(ps1 + ps2))
    }

    //endregion

    //region getAll

    @Test
    fun `getAll returns nothing`() {
        every { noteController.getAll(any<BoundingBox>()) } returns emptyList()
        every { noteEditsSource.getAllUnsynced(any()) } returns emptyList()

        assertTrue(src.getAll(bbox).isEmpty())
    }

    @Test
    fun `getAll returns original notes`() {
        val notes = listOf(note(1), note(2))
        every { noteController.getAll(any<BoundingBox>()) } returns notes
        every { noteEditsSource.getAllUnsynced(any()) } returns emptyList()

        assertTrue(src.getAll(bbox).containsExactlyInAnyOrder(notes))
    }

    @Test
    fun `getAll returns updated notes`() {
        every { noteController.getAll(any<BoundingBox>()) } returns listOf(
            note(id = 1, position = p(1.0, 2.0), timestamp = 10, comments = listOf(
                comment("test", NoteComment.Action.OPENED, timestamp = 100)
            )),
            note(id = 3, position = p(0.0, 3.0), timestamp = 800)
        )
        every { noteEditsSource.getAllUnsynced(any()) } returns listOf(
            noteEdit(noteId = 2, action = NoteEditAction.CREATE, text = "xyz", timestamp = 300, pos = p(12.0, 1.0)),
            noteEdit(noteId = 1, action = NoteEditAction.COMMENT, text = "test2", timestamp = 500),
            noteEdit(noteId = 2, action = NoteEditAction.COMMENT, text = "abc", timestamp = 900),
        )

        assertTrue(
            listOf(
                note(id = 1, position = p(1.0, 2.0), timestamp = 10, comments = listOf(
                    comment("test", NoteComment.Action.OPENED, timestamp = 100),
                    comment("test2", NoteComment.Action.COMMENTED, timestamp = 500, user = user)
                )),
                note(id = 2, position = p(12.0, 1.0), timestamp = 300, comments = listOf(
                    comment("xyz", NoteComment.Action.OPENED, timestamp = 300, user = user),
                    comment("abc", NoteComment.Action.COMMENTED, timestamp = 900, user = user),
                )),
                note(id = 3, position = p(0.0, 3.0), timestamp = 800)
            ).containsExactlyInAnyOrder(src.getAll(bbox))
        )
    }

    //endregion

    //region NoteEditsSource.Listener

    @Test fun `onDeletedEdits relays updated note`() {
        val listener = mock<NotesWithEditsSource.Listener>()
        src.addListener(listener)

        val note = note()
        val edit = noteEdit(noteId = note.id)

        every { noteController.get(note.id) } returns note
        every { noteEditsSource.getAllUnsyncedForNote(note.id) } returns emptyList()

        noteEditsListener.onDeletedEdits(listOf(edit))

        verifyListenerCalledWith(listener, updated = listOf(note))
    }

    @Test fun `onDeletedEdits relays deleted note`() {
        val listener = mock<NotesWithEditsSource.Listener>()
        src.addListener(listener)

        val noteId = 1L
        val edit = noteEdit(noteId = noteId, action = NoteEditAction.CREATE)

        every { noteController.get(noteId) } returns null

        noteEditsListener.onDeletedEdits(listOf(edit))

        verifyListenerCalledWith(listener, deleted = listOf(noteId))
    }

    @Test fun `onAddedEdit relays updated note`() {
        val listener = mock<NotesWithEditsSource.Listener>()
        src.addListener(listener)

        val note = note()
        val edit = noteEdit(noteId = note.id)

        every { noteController.get(note.id) } returns note
        every { noteEditsSource.getAllUnsyncedForNote(note.id) } returns listOf(edit)

        noteEditsListener.onAddedEdit(edit)

        val noteEditComment = NoteComment(
            timestamp = edit.createdTimestamp,
            action = NoteComment.Action.COMMENTED,
            text = edit.text ?: "",
            user = user
        )
        val updatedNote = note.copy(
            comments = note.comments + noteEditComment
        )

        verifyListenerCalledWith(listener, updated = listOf(updatedNote))
    }

    @Test fun `onAddedEdit relays added note`() {
        val listener = mock<NotesWithEditsSource.Listener>()
        src.addListener(listener)

        val edit = noteEdit(action = NoteEditAction.CREATE)

        every { noteController.get(edit.noteId) } returns null
        every { noteEditsSource.getAllUnsyncedForNote(edit.noteId) } returns listOf(edit)

        noteEditsListener.onAddedEdit(edit)

        val expectedNote = note(
            id = edit.noteId,
            position = edit.position,
            timestamp = edit.createdTimestamp,
            comments = listOf(
                NoteComment(
                    timestamp = edit.createdTimestamp,
                    action = NoteComment.Action.OPENED,
                    text = edit.text ?: "",
                    user = user
                )
            )
        )

        verifyListenerCalledWith(listener, added = listOf(expectedNote))
    }

    //endregion

    //region NoteSource.Listener

    @Test fun `onUpdated passes through notes when there are no edits`() {
        val listener = mock<NotesWithEditsSource.Listener>()
        src.addListener(listener)

        val added = listOf(note(1), note(2))
        val updated = listOf(note(3), note(4))
        val deleted = listOf(1L, 2L)

        every { noteEditsSource.getAllUnsyncedForNotes(any()) } returns emptyList()

        noteListener.onUpdated(added, updated, deleted)

        verifyListenerCalledWith(listener, added = added, updated = updated, deleted = deleted)
    }

    @Test fun `onUpdated applies edits on top of passed added notes`() {
        val listener = mock<NotesWithEditsSource.Listener>()
        src.addListener(listener)

        // note edits: for notes 1 and 3, only note 1 was commented by the user
        every { noteEditsSource.getAllUnsyncedForNotes(listOf(1L, 3L)) } returns listOf(
            noteEdit(noteId = 1, action = NoteEditAction.COMMENT, text = "test2", timestamp = 500)
        )
        every { noteEditsSource.getAllUnsyncedForNotes(emptyList()) } returns emptyList()

        // server adds notes 1 and 3
        noteListener.onUpdated(
            added = listOf(
                note(id = 1, position = p(1.0, 2.0), timestamp = 10, comments = listOf(
                    comment("test", NoteComment.Action.OPENED, timestamp = 100)
                )),
                note(id = 3, position = p(0.0, 3.0), timestamp = 800)
            ),
            updated = emptyList(),
            deleted = emptyList()
        )

        verifyListenerCalledWith(listener, added = listOf(
            note(id = 1, position = p(1.0, 2.0), timestamp = 10, comments = listOf(
                comment("test", NoteComment.Action.OPENED, timestamp = 100),
                comment("test2", NoteComment.Action.COMMENTED, timestamp = 500, user = user)
            )),
            note(id = 3, position = p(0.0, 3.0), timestamp = 800)
        ))
    }

    @Test fun `onUpdated applies edits on top of passed updated notes`() {
        val listener = mock<NotesWithEditsSource.Listener>()
        src.addListener(listener)

        // note edits: for notes 1 and 3, only note 1 was commented by the user
        every { noteEditsSource.getAllUnsyncedForNotes(listOf(1L, 3L)) } returns listOf(
            noteEdit(noteId = 1, action = NoteEditAction.COMMENT, text = "test2", timestamp = 500)
        )
        every { noteEditsSource.getAllUnsyncedForNotes(emptyList()) } returns emptyList()

        // server updates notes 1 and 3
        noteListener.onUpdated(
            added = emptyList(),
            updated = listOf(
                note(id = 1, position = p(1.0, 2.0), timestamp = 10, comments = listOf(
                    comment("test", NoteComment.Action.OPENED, timestamp = 100)
                )),
                note(id = 3, position = p(0.0, 3.0), timestamp = 800)
            ),
            deleted = emptyList()
        )

        verifyListenerCalledWith(listener, updated = listOf(
            note(id = 1, position = p(1.0, 2.0), timestamp = 10, comments = listOf(
                comment("test", NoteComment.Action.OPENED, timestamp = 100),
                comment("test2", NoteComment.Action.COMMENTED, timestamp = 500, user = user)
            )),
            note(id = 3, position = p(0.0, 3.0), timestamp = 800)
        ))
    }

    @Test fun `onCleared passes through call`() {
        val listener = mock<NotesWithEditsSource.Listener>()
        src.addListener(listener)

        noteListener.onCleared()
        verify { listener.onCleared() }
    }

    //endregion
}

private fun verifyListenerCalledWith(
    listener: NotesWithEditsSource.Listener,
    added: Collection<Note> = emptyList(),
    updated: Collection<Note> = emptyList(),
    deleted: Collection<Long> = emptyList()
) {
    verify { listener.onUpdated(
        added = matches { added.containsExactlyInAnyOrder(it) },
        updated = matches { updated.containsExactlyInAnyOrder(it) },
        deleted = matches { deleted.containsExactlyInAnyOrder(it) },
    ) }
}
