package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.NotesApi
import de.westnordost.streetcomplete.data.osmnotes.StreetCompleteImageUploader
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.testutils.*
import de.westnordost.streetcomplete.testutils.any
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*

class NoteEditsUploaderTest {

    private lateinit var noteController: NoteController
    private lateinit var noteEditsController: NoteEditsController
    private lateinit var notesApi: NotesApi
    private lateinit var imageUploader: StreetCompleteImageUploader

    private lateinit var uploader: NoteEditsUploader
    private lateinit var listener: OnUploadedChangeListener

    @Before fun setUp() {
        notesApi = mock()
        noteController = mock()
        noteEditsController = mock()

        on(noteEditsController.getOldestNeedingImagesActivation()).thenReturn(null)
        on(noteEditsController.getOldestUnsynced()).thenReturn(null)

        on(notesApi.comment(anyLong(), any())).thenReturn(note())
        on(notesApi.create(any(), any())).thenReturn(note())

        imageUploader = mock()
        listener = mock()

        uploader = NoteEditsUploader(noteEditsController, noteController, notesApi, imageUploader)
        uploader.uploadedChangeListener = listener
    }

    @Test fun `cancel upload works`() = runBlocking {
        val job = launch { uploader.upload() }
        job.cancelAndJoin()
        verifyNoInteractions(noteEditsController, noteController, notesApi, imageUploader)
    }

    @Test fun `upload note comment`() {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(noteId = 1L, action = NoteEditAction.COMMENT, text = "abc", pos = pos)
        val note = note(id = 1L)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(notesApi.comment(anyLong(), any())).thenReturn(note)

        upload()

        verify(notesApi).comment(1L, "abc")
        verify(noteController).put(note)
        verify(noteEditsController).synced(edit, note)
        verifyNoInteractions(imageUploader)
        verify(listener)!!.onUploaded("NOTE", pos)
    }

    @Test fun `upload create note`() {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(noteId = -5L, action = NoteEditAction.CREATE, text = "abc", pos = pos)
        val note = note(123)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(notesApi.create(any(), any())).thenReturn(note)

        upload()

        verify(notesApi).create(pos, "abc")
        verify(noteController).put(note)
        verify(noteEditsController).synced(edit, note)
        verifyNoInteractions(imageUploader)
        verify(listener)!!.onUploaded("NOTE", pos)
    }

    @Test fun `fail uploading note comment because of a conflict`() {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(noteId = 1L, action = NoteEditAction.COMMENT, text = "abc", pos = pos)
        val note = note(1)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(notesApi.comment(anyLong(), any())).thenThrow(ConflictException())
        on(notesApi.get(1L)).thenReturn(note)

        upload()

        verify(notesApi).comment(1L, "abc")
        verify(noteController).put(note)
        verify(noteEditsController).syncFailed(edit)
        verifyNoInteractions(imageUploader)
        verify(listener)!!.onDiscarded("NOTE", pos)
    }

    @Test fun `fail uploading note comment because note was deleted`() {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(noteId = 1L, action = NoteEditAction.COMMENT, text = "abc", pos = pos)
        val note = note(1)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(notesApi.comment(anyLong(), any())).thenThrow(ConflictException())
        on(notesApi.get(1L)).thenReturn(null)

        upload()

        verify(notesApi).comment(1L, "abc")
        verify(noteController).delete(note.id)
        verify(noteEditsController).syncFailed(edit)
        verifyNoInteractions(imageUploader)
        verify(listener)!!.onDiscarded("NOTE", pos)
    }

    @Test fun `upload several note edits`() {
        on(noteEditsController.getOldestUnsynced()).thenReturn(noteEdit()).thenReturn(noteEdit()).thenReturn(null)
        on(notesApi.comment(anyLong(), any())).thenReturn(note())

        upload()

        verify(notesApi, times(2)).comment(anyLong(), any())
        verify(noteController, times(2)).put(any())
        verify(noteEditsController, times(2)).synced(any(), any())
        verify(listener, times(2))!!.onUploaded(any(), any())
    }

    @Test fun `upload note comment with attached images`() {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(
            noteId = 1L,
            action = NoteEditAction.COMMENT,
            text = "test",
            pos = pos,
            imagePaths = listOf("a","b","c")
        )
        val note = note(1)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(notesApi.comment(anyLong(), any())).thenReturn(note)
        on(imageUploader.upload(any())).thenReturn(listOf("x","y","z"))

        upload()

        verify(notesApi).comment(1L, "test\n\nAttached photo(s):\nx\ny\nz")
        verify(noteController).put(note)
        verify(noteEditsController).synced(edit, note)
        verify(noteEditsController).imagesActivated(1L)
        verify(imageUploader).upload(listOf("a","b","c"))
        verify(imageUploader).activate(1L)
        verify(listener)!!.onUploaded("NOTE", pos)
    }

    @Test fun `upload create note with attached images`() {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(
            noteId = 1L,
            action = NoteEditAction.CREATE,
            text = "test",
            pos = pos,
            imagePaths = listOf("a","b","c")
        )
        val note = note(1)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(notesApi.create(any(), any())).thenReturn(note)
        on(imageUploader.upload(any())).thenReturn(listOf("x","y","z"))

        upload()

        verify(notesApi).create(pos, "test\n\nAttached photo(s):\nx\ny\nz")
        verify(noteController).put(note)
        verify(noteEditsController).synced(edit, note)
        verify(noteEditsController).imagesActivated(1L)
        verify(imageUploader).upload(listOf("a","b","c"))
        verify(imageUploader).activate(1L)
        verify(listener)!!.onUploaded("NOTE", pos)
    }

    @Test fun `upload missed image activations`() {
        val edit = noteEdit(noteId = 3)

        on(noteEditsController.getOldestNeedingImagesActivation()).thenReturn(edit).thenReturn(null)

        upload()

        verify(imageUploader).activate(3)
        verify(noteEditsController).imagesActivated(1L)
    }

    private fun upload() = runBlocking {
        uploader.upload()
    }
}
