package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.common.Handler
import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NoteComment
import de.westnordost.osmapi.notes.NotesDao
import de.westnordost.streetcomplete.ApplicationConstants.USER_AGENT
import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.util.ImageUploader
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.util.*

class SingleCreateNoteUploadTest {
    private lateinit var notesDao: NotesDao
    private lateinit var imageUploader: ImageUploader
    private lateinit var uploader: SingleCreateNoteUpload

    @Before fun setUp() {
        notesDao = mock(NotesDao::class.java)
        imageUploader = mock(ImageUploader::class.java)
        uploader = SingleCreateNoteUpload(notesDao, imageUploader)
    }

    @Test fun `upload createNote on existing note will comment on existing note`() {
        val createNote = newCreateNote()
        createNote.elementType = Element.Type.WAY
        createNote.elementId = 5L

        val existingNote = newNote(createNote)
        setUpExistingNote(existingNote)
        on(notesDao.comment(anyLong(),anyString())).thenReturn(existingNote)

        assertEquals(existingNote, uploader.upload(createNote))

        verify(notesDao).comment(existingNote.id, createNote.text)
    }

    @Test(expected = ConflictException::class)
    fun `upload createNote on existing closed note will throw conflict exception`() {
        val createNote = newCreateNote()
        createNote.elementType = Element.Type.WAY
        createNote.elementId = 5L

        val existingNote = newNote(createNote)
        existingNote.status = Note.Status.CLOSED
        setUpExistingNote(existingNote)

        uploader.upload(createNote)

        verify(notesDao).getAll(any(),any(),anyInt(),anyInt())
        verifyNoMoreInteractions(notesDao)
    }

    @Test(expected = ConflictException::class)
    fun `upload createNote on existing note will throw along a conflict exception`() {
        val createNote = newCreateNote()
        createNote.elementType = Element.Type.WAY
        createNote.elementId = 5L

        val existingNote = newNote(createNote)
        setUpExistingNote(existingNote)
        on(notesDao.comment(anyLong(),anyString()))
            .thenThrow(OsmConflictException::class.java)

        uploader.upload(createNote)

        verify(notesDao).getAll(any(),any(),anyInt(),anyInt())
        verify(notesDao).comment(existingNote.id, createNote.text)
    }

    @Test fun `upload createNote with no associated element works`() {
        val createNote = newCreateNote()
        val note = newNote(null)

        on(notesDao.create(any(), anyString())).thenReturn(note)

        assertEquals(note, uploader.upload(createNote))

        verify(notesDao).create(
            createNote.position,
            createNote.text + "\n\nvia " + USER_AGENT
        )
    }

    @Test fun `upload createNote with no quest title but associated element works`() {
        val createNote = newCreateNote()
        createNote.elementType = Element.Type.WAY
        createNote.elementId = 5L

        val note = newNote(null)

        on(notesDao.create(any(), anyString())).thenReturn(note)

        assertEquals(note, uploader.upload(createNote))

        verify(notesDao).create(
            createNote.position,
            "for https://osm.org/way/5 via " + USER_AGENT + ":\n\n" + createNote.text
        )
    }

    @Test fun `upload createNote with associated element and no note yet works`() {
        val createNote = newCreateNote()
        createNote.elementType = Element.Type.WAY
        createNote.elementId = 5L
        createNote.questTitle = "What?"

        val note = newNote(createNote)

        on(notesDao.create(any(), anyString())).thenReturn(note)

        assertEquals(note, uploader.upload(createNote))

        verify(notesDao).create(
            createNote.position,
            "Unable to answer \"What?\" for https://osm.org/way/5 via " + USER_AGENT + ":\n\n" + createNote.text
        )
    }

    @Test fun `upload createNote uploads images and displays links`() {
        val createNote = newCreateNote()
        createNote.imagePaths = listOf("hello")

        val note = newNote(null)
        on(notesDao.create(any(), anyString())).thenReturn(note)
        on(imageUploader.upload(createNote.imagePaths)).thenReturn(listOf("hello, too"))

        assertEquals(note, uploader.upload(createNote))

        verify(imageUploader).upload(createNote.imagePaths)
        verify(notesDao).create(
            createNote.position,
            createNote.text + "\n\nvia " + USER_AGENT + "\n\nAttached photo(s):\nhello, too"
        )
    }

    @Test fun `upload createNote as comment uploads images and displays links`() {
        val createNote = newCreateNote()
        createNote.elementType = Element.Type.WAY
        createNote.elementId = 5L
        createNote.imagePaths = listOf("hello")

        val existingNote = newNote(createNote)
        setUpExistingNote(existingNote)

        on(notesDao.comment(anyLong(),anyString())).thenReturn(existingNote)
        on(imageUploader.upload(createNote.imagePaths)).thenReturn(listOf("hello, too"))

        assertEquals(existingNote, uploader.upload(createNote))

        verify(notesDao).comment(existingNote.id, createNote.text + "\n\nAttached photo(s):\nhello, too")
    }

    private fun setUpExistingNote(note: Note) {
        doAnswer { invocation ->
            (invocation.arguments[1] as Handler<Note>).handle(note)
        }.on(notesDao).getAll(any(),any(),anyInt(),anyInt())
    }

    private fun newNote(fitsTo: CreateNote?): Note {
        val note = Note()
        note.id = 2
        note.status = Note.Status.OPEN
        note.dateCreated = Date()
        note.position = OsmLatLon(1.0, 2.0)
        val comment = NoteComment()
        comment.text = "bla bla"
        if (fitsTo != null) {
            comment.text += fitsTo.associatedElementString
        }
        comment.action = NoteComment.Action.OPENED
        comment.date = Date()
        note.comments.add(0, comment)
        return note
    }

    private fun newCreateNote(): CreateNote {
        val n = CreateNote()
        n.id = 1
        n.text = "jo ho"
        n.position = OsmLatLon(1.0, 2.0)
        return n
    }
}

private val CreateNote.associatedElementString: String? get() {
    val lowercaseTypeName = elementType?.name?.toLowerCase(Locale.UK) ?: return null
    val elementId = elementId ?: return null
    return "https://osm.org/$lowercaseTypeName/$elementId"
}
