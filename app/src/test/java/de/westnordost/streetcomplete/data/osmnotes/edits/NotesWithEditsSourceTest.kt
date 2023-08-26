package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.NoteComment
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.user.User
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.comment
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.noteEdit
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import org.mockito.Mockito.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NotesWithEditsSourceTest {

    private lateinit var src: NotesWithEditsSource
    private lateinit var noteController: NoteController
    private lateinit var noteListener: NoteController.Listener
    private lateinit var noteEditsController: NoteEditsController
    private lateinit var noteEditsListener: NoteEditsSource.Listener
    private lateinit var userDataSource: UserDataSource

    @BeforeTest fun setUp() {
        noteController = mock()
        noteEditsController = mock()
        userDataSource = mock()

        on(noteController.addListener(any())).then { invocation ->
            noteListener = invocation.getArgument(0)
            Unit
        }

        on(noteEditsController.addListener(any())).then { invocation ->
            noteEditsListener = invocation.getArgument(0)
            Unit
        }

        src = NotesWithEditsSource(noteController, noteEditsController, userDataSource)
    }

    //region get

    @Test
    fun `get returns nothing`() {
        on(noteController.get(1)).thenReturn(null)
        on(noteEditsController.getAllUnsyncedForNote(1)).thenReturn(emptyList())

        assertNull(src.get(1))
    }

    @Test
    fun `get returns original note`() {
        val note = note(1)
        on(noteController.get(1)).thenReturn(note)
        on(noteEditsController.getAllUnsyncedForNote(1)).thenReturn(emptyList())

        assertEquals(note, src.get(1))
    }

    @Test
    fun `get returns updated note with anonymous user`() {
        val comment = comment("test", NoteComment.Action.OPENED, timestamp = 100)
        val addedComment = comment("test2", NoteComment.Action.COMMENTED, timestamp = 500, user = user)

        val note = note(id = 1, comments = arrayListOf(comment))
        val edits = listOf(
            noteEdit(noteId = 1, action = NoteEditAction.COMMENT, text = "test2", timestamp = 500)
        )

        on(userDataSource.userId).thenReturn(-1)
        on(noteController.get(1)).thenReturn(note)
        on(noteEditsController.getAllUnsyncedForNote(1)).thenReturn(edits)

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

        val note = note(id = 1, comments = arrayListOf(comment))
        val edits = listOf(noteEdit(
            noteId = 1,
            action = NoteEditAction.COMMENT,
            text = "test2",
            imagePaths = listOf("something"),
            timestamp = 500
        ))

        on(userDataSource.userId).thenReturn(-1)
        on(noteController.get(1)).thenReturn(note)
        on(noteEditsController.getAllUnsyncedForNote(1)).thenReturn(edits)

        assertEquals(listOf(comment, addedComment), src.get(1)!!.comments)
    }

    @Test
    fun `get returns updated note`() {
        val comment1 = comment("test", NoteComment.Action.OPENED, timestamp = 123)
        val comment2 = comment("test2", NoteComment.Action.COMMENTED, timestamp = 500, user = User(23, "test user"))

        val note = note(id = 1, comments = arrayListOf(comment1))
        val edits = listOf(
            noteEdit(noteId = 1, action = NoteEditAction.COMMENT, text = "test2", timestamp = 500)
        )

        on(userDataSource.userId).thenReturn(23)
        on(userDataSource.userName).thenReturn("test user")
        on(noteController.get(1)).thenReturn(note)
        on(noteEditsController.getAllUnsyncedForNote(1)).thenReturn(edits)

        assertEquals(listOf(comment1, comment2), src.get(1)!!.comments)
    }

    @Test
    fun `get returns note with anonymous user updated twice`() {
        val comment1 = comment("test", NoteComment.Action.OPENED, timestamp = 123)
        val comment2 = comment("test2", NoteComment.Action.COMMENTED, timestamp = 500, user = user)
        val comment3 = comment("test3", NoteComment.Action.COMMENTED, timestamp = 800, user = user)

        val note = note(id = 1, comments = arrayListOf(comment1))
        val edits = listOf(
            noteEdit(noteId = 1, action = NoteEditAction.COMMENT, text = "test2", timestamp = 500),
            noteEdit(noteId = 1, action = NoteEditAction.COMMENT, text = "test3", timestamp = 800),
        )

        on(userDataSource.userId).thenReturn(-1)
        on(noteController.get(1)).thenReturn(note)
        on(noteEditsController.getAllUnsyncedForNote(1)).thenReturn(edits)

        assertEquals(listOf(comment1, comment2, comment3), src.get(1)!!.comments)
    }

    @Test
    fun `get returns created note`() {
        val p = p(12.0, 46.0)
        val expectedNote = note(
            id = -12,
            comments = arrayListOf(
                comment("test12", NoteComment.Action.OPENED, timestamp = 123, user = user)
            ),
            position = p,
            timestamp = 123
        )

        val edits = listOf(
            noteEdit(noteId = -12, pos = p, action = NoteEditAction.CREATE, text = "test12", timestamp = 123)
        )

        on(userDataSource.userId).thenReturn(-1)
        on(noteController.get(1)).thenReturn(null)
        on(noteEditsController.getAllUnsyncedForNote(1)).thenReturn(edits)

        assertEquals(expectedNote, src.get(1)!!)
    }

    @Test
    fun `get returns created, then commented note`() {
        val p = p(12.0, 46.0)
        val expectedNote = note(
            id = -12,
            comments = arrayListOf(
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

        on(userDataSource.userId).thenReturn(-1)
        on(noteController.get(1)).thenReturn(null)
        on(noteEditsController.getAllUnsyncedForNote(1)).thenReturn(edits)

        assertEquals(expectedNote, src.get(1)!!)
    }

    //endregion

    //region getAllPositions

    @Test
    fun getAllPositions() {
        val ps1 = listOf(p(3.0, 2.0), p(1.0, 3.0))
        val ps2 = listOf(p(3.0, 2.0), p(5.0, 1.0))

        on(noteController.getAllPositions(any())).thenReturn(ps1)
        on(noteEditsController.getAllUnsyncedPositions(any())).thenReturn(ps2)

        val positions = src.getAllPositions(bbox)

        assertTrue(positions.containsAll(ps1 + ps2))
    }

    //endregion

    //region getAll

    @Test
    fun `getAll returns nothing`() {
        on(noteController.getAll(any<BoundingBox>())).thenReturn(emptyList())
        on(noteEditsController.getAllUnsynced(any())).thenReturn(emptyList())

        assertTrue(src.getAll(bbox).isEmpty())
    }

    @Test
    fun `getAll returns original notes`() {
        val notes = listOf(note(1), note(2))
        on(noteController.getAll(any<BoundingBox>())).thenReturn(notes)
        on(noteEditsController.getAllUnsynced(any())).thenReturn(emptyList())

        assertTrue(src.getAll(bbox).containsExactlyInAnyOrder(notes))
    }

    @Test
    fun `getAll returns updated notes`() {

        on(userDataSource.userId).thenReturn(-1)
        on(noteController.getAll(any<BoundingBox>())).thenReturn(initialNotes1)
        on(noteEditsController.getAllUnsynced(any())).thenReturn(edits1)

        assertEquals(expectedNotes1.toSet(), src.getAll(bbox).toSet())
    }

    //endregion

    //region NoteEditsSource.Listener

    @Test fun `onDeletedEdits relays updated note`() {
        val listener = mock<NotesWithEditsSource.Listener>()
        src.addListener(listener)

        val note = note(1)
        val edit = noteEdit(noteId = 1)

        on(noteController.get(1)).thenReturn(note)

        noteEditsListener.onDeletedEdits(listOf(edit))

        checkListenerCalledWith(listener, updated = listOf(note))
    }

    @Test fun `onDeletedEdits relays deleted note`() {
        val listener = mock<NotesWithEditsSource.Listener>()
        src.addListener(listener)

        val edit = noteEdit(noteId = 1)

        on(noteController.get(1)).thenReturn(null)

        noteEditsListener.onDeletedEdits(listOf(edit))

        checkListenerCalledWith(listener, deleted = listOf(1L))
    }

    @Test fun `onAddedEdit relays updated note`() {
        val listener = mock<NotesWithEditsSource.Listener>()
        src.addListener(listener)

        val note = note(id = 1)
        val edit = noteEdit(noteId = 1)

        on(noteController.get(1)).thenReturn(note)

        noteEditsListener.onAddedEdit(edit)

        checkListenerCalledWith(listener, updated = listOf(note))
    }

    @Test fun `onAddedEdit relays added note`() {
        val listener = mock<NotesWithEditsSource.Listener>()
        src.addListener(listener)

        val p = p(1.0, 1.0)
        val expectedNote = note(id = 1, position = p, timestamp = 123L, comments = arrayListOf(
            comment("abc", NoteComment.Action.OPENED, 123L)
        ))
        val edit = noteEdit(noteId = 1, id = -1, action = NoteEditAction.CREATE, text = "abc", timestamp = 123L, pos = p)

        on(noteController.get(1)).thenReturn(null)
        on(noteEditsController.getAllUnsyncedForNote(1)).thenReturn(listOf(edit))

        noteEditsListener.onAddedEdit(edit)

        checkListenerCalledWith(listener, added = listOf(expectedNote))
    }

    //endregion

    //region NoteController.Listener

    @Test fun `onUpdated passes through notes because there are no edits`() {
        val listener = mock<NotesWithEditsSource.Listener>()
        src.addListener(listener)

        val added = listOf(note(1), note(2))
        val updated = listOf(note(3), note(4))
        val deleted = listOf(1L, 2L)

        on(noteEditsController.getAllUnsynced()).thenReturn(emptyList())

        noteListener.onUpdated(added, updated, deleted)

        checkListenerCalledWith(listener, added = added, updated = updated, deleted = deleted)
    }

    @Test fun `onUpdated applies edits on top of passed added notes`() {
        val listener = mock<NotesWithEditsSource.Listener>()
        src.addListener(listener)

        on(noteEditsController.getAllUnsynced()).thenReturn(edits1)

        noteListener.onUpdated(initialNotes1, emptyList(), emptyList())

        checkListenerCalledWith(listener, added = expectedNotes1)
    }

    @Test fun `onUpdated applies edits on top of passed updated notes`() {
        val listener = mock<NotesWithEditsSource.Listener>()
        src.addListener(listener)

        on(noteEditsController.getAllUnsynced()).thenReturn(edits1)

        noteListener.onUpdated(emptyList(), initialNotes1, emptyList())

        checkListenerCalledWith(listener, updated = expectedNotes1)
    }

    @Test fun `onCleared passes through call`() {
        val listener = mock<NotesWithEditsSource.Listener>()
        src.addListener(listener)

        noteListener.onCleared()
        verify(listener).onCleared()
    }

    //endregion
}

private val bbox = bbox()

val user = User(id = -1, displayName = "")

val initialNotes1 = listOf(
    note(id = 1, position = p(1.0, 2.0), timestamp = 10, comments = arrayListOf(
        comment("test", NoteComment.Action.OPENED, timestamp = 100)
    )),
    note(id = 3, position = p(0.0, 3.0), timestamp = 800)
)

val expectedNotes1 = listOf(
    note(id = 1, position = p(1.0, 2.0), timestamp = 10, comments = arrayListOf(
        comment("test", NoteComment.Action.OPENED, timestamp = 100),
        comment("test2", NoteComment.Action.COMMENTED, timestamp = 500, user = user)
    )),
    note(id = 2, position = p(12.0, 1.0), timestamp = 300, comments = arrayListOf(
        comment("xyz", NoteComment.Action.OPENED, timestamp = 300, user = user),
        comment("abc", NoteComment.Action.COMMENTED, timestamp = 900, user = user),
    )),
    note(id = 3, position = p(0.0, 3.0), timestamp = 800)
)

val edits1 = listOf(
    noteEdit(noteId = 2, action = NoteEditAction.CREATE, text = "xyz", timestamp = 300, pos = p(12.0, 1.0)),
    noteEdit(noteId = 1, action = NoteEditAction.COMMENT, text = "test2", timestamp = 500),
    noteEdit(noteId = 2, action = NoteEditAction.COMMENT, text = "abc", timestamp = 900),
)

private fun checkListenerCalledWith(
    listener: NotesWithEditsSource.Listener,
    added: Collection<Note> = emptyList(),
    updated: Collection<Note> = emptyList(),
    deleted: Collection<Long> = emptyList()
) {
    on(listener).then { invocation ->
        val actuallyAdded = invocation.getArgument<Collection<Note>>(0)
        val actuallyUpdated = invocation.getArgument<Collection<Note>>(1)
        val actuallyDeleted = invocation.getArgument<Collection<Long>>(2)

        assertEquals(added, actuallyAdded)
        assertEquals(updated, actuallyUpdated)
        assertTrue(deleted.containsExactlyInAnyOrder(actuallyDeleted))
    }
}
