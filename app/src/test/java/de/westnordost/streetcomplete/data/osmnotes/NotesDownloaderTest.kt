package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.NotesApi
import de.westnordost.osmapi.common.Handler
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.p
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NoteComment
import de.westnordost.streetcomplete.data.user.UserStore
import de.westnordost.streetcomplete.eq
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class NotesDownloaderTest {
    private lateinit var notesApi: NotesApi
    private lateinit var noteController: NoteController
    private lateinit var userStore: UserStore

    @Before fun setUp() {
        notesApi = mock()
        noteController = mock()

        userStore = mock()
        on(userStore.userId).thenReturn(1L)
    }

    @Test fun `calls controller with all notes coming from the notes api`() {
        val note1 = createANote()
        val noteApi = TestListBasedNotesApi(listOf(note1))
        val dl = NotesDownloader(noteApi, userStore, noteController)
        val bbox = BoundingBox(0.0, 0.0, 1.0, 1.0)
        dl.download(bbox, AtomicBoolean(false))

        verify(noteController).putAllForBBox(eq(bbox), eq(listOf(note1)))
    }
}

private fun createANote(): Note {
    val note = Note()
    note.id = 1L
    note.position = p(6.0, 7.0)
    note.status = Note.Status.OPEN
    note.dateCreated = Date()
    val comment = NoteComment()
    comment.date = Date()
    comment.action = NoteComment.Action.OPENED
    comment.text = "hurp durp"
    note.comments.add(comment)
    return note
}

private class TestListBasedNotesApi(val notes: List<Note>) :  NotesApi(null) {
    override fun getAll(bounds: BoundingBox, handler: Handler<Note>, limit: Int, hideClosedNoteAfter: Int) {
        for (note in notes) {
            handler.handle(note)
        }
    }
}
