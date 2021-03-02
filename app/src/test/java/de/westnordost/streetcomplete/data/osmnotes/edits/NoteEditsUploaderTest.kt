package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.NotesApi
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.StreetCompleteImageUploader
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*
import java.util.concurrent.atomic.AtomicBoolean

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

    @Test fun `cancel upload works`() {
        val cancelled = AtomicBoolean(true)
        uploader.upload(cancelled)
        verifyNoInteractions(noteEditsController, noteController, notesApi, imageUploader)
    }

    @Test fun `upload note comment`() {
        val pos = OsmLatLon(1.0, 13.0)
        val edit = edit(noteId = 1L, action = NoteEditAction.COMMENT, text = "abc", pos = pos)
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
        val pos = OsmLatLon(1.0, 13.0)
        val edit = edit(noteId = -5L, action = NoteEditAction.CREATE, text = "abc", pos = pos)
        val note = note(id = 123L)

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
        val pos = OsmLatLon(1.0, 13.0)
        val edit = edit(noteId = 1L, action = NoteEditAction.COMMENT, text = "abc", pos = pos)
        val note = note(id = 1L)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(notesApi.comment(anyLong(), any())).thenThrow(OsmConflictException(403,"",""))
        on(notesApi.get(1L)).thenReturn(note)

        upload()

        verify(notesApi).comment(1L, "abc")
        verify(noteController).put(note)
        verify(noteEditsController).syncFailed(edit)
        verifyNoInteractions(imageUploader)
        verify(listener)!!.onDiscarded("NOTE", pos)
    }

    @Test fun `fail uploading note comment because note was deleted`() {
        val pos = OsmLatLon(1.0, 13.0)
        val edit = edit(noteId = 1L, action = NoteEditAction.COMMENT, text = "abc", pos = pos)
        val note = note(id = 1L)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(notesApi.comment(anyLong(), any())).thenThrow(OsmNotFoundException(410,"",""))
        on(notesApi.get(1L)).thenReturn(null)

        upload()

        verify(notesApi).comment(1L, "abc")
        verify(noteController).delete(note.id)
        verify(noteEditsController).syncFailed(edit)
        verifyNoInteractions(imageUploader)
        verify(listener)!!.onDiscarded("NOTE", pos)
    }

    @Test fun `upload several note edits`() {
        on(noteEditsController.getOldestUnsynced()).thenReturn(edit()).thenReturn(edit()).thenReturn(null)
        on(notesApi.comment(anyLong(), any())).thenReturn(note())

        upload()

        verify(notesApi, times(2)).comment(anyLong(), any())
        verify(noteController, times(2)).put(any())
        verify(noteEditsController, times(2)).synced(any(), any())
        verify(listener, times(2))!!.onUploaded(any(), any())
    }

    @Test fun `upload note comment with attached images`() {
        val pos = OsmLatLon(1.0, 13.0)
        val edit = edit(
            noteId = 1L,
            action = NoteEditAction.COMMENT,
            text = "test",
            pos = pos,
            imagePaths = listOf("a","b","c")
        )
        val note = note(id = 1L)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(notesApi.comment(anyLong(), any())).thenReturn(note)
        on(imageUploader.upload(any())).thenReturn(listOf("x","y","z"))

        upload()

        verify(notesApi).comment(1L, "test\n\nAttached photo(s):\nx\ny\nz")
        verify(noteController).put(note)
        verify(noteEditsController).synced(edit, note)
        verify(noteEditsController).imagesActivated(1L)
        verify(imageUploader.upload(eq(listOf("x","y","z"))))
        verify(imageUploader.activate(1L))
        verify(listener)!!.onUploaded("NOTE", pos)
    }

    @Test fun `upload create note with attached images`() {
        val pos = OsmLatLon(1.0, 13.0)
        val edit = edit(
            noteId = 1L,
            action = NoteEditAction.CREATE,
            text = "test",
            pos = pos,
            imagePaths = listOf("a","b","c")
        )
        val note = note(id = 1L)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(notesApi.create(any(), any())).thenReturn(note)
        on(imageUploader.upload(any())).thenReturn(listOf("x","y","z"))

        upload()

        verify(notesApi).create(pos, "test\n\nAttached photo(s):\nx\ny\nz")
        verify(noteController).put(note)
        verify(noteEditsController).synced(edit, note)
        verify(noteEditsController).imagesActivated(1L)
        verify(imageUploader.upload(eq(listOf("x","y","z"))))
        verify(imageUploader.activate(1L))
        verify(listener)!!.onUploaded("NOTE", pos)
    }

    @Test fun `upload missed image activations`() {
        val edit = edit(noteId = 3)

        on(noteEditsController.getOldestNeedingImagesActivation()).thenReturn(edit).thenReturn(null)

        upload()

        verify(imageUploader.activate(3))
        verify(noteEditsController).imagesActivated(3)
    }

    private fun upload() {
        uploader.upload(AtomicBoolean(false))
    }
}

private fun edit(
    noteId: Long = 1L,
    action: NoteEditAction = NoteEditAction.COMMENT,
    text: String = "test123",
    imagePaths: List<String> = emptyList(),
    pos: LatLon = OsmLatLon(1.0, 1.0)
) = NoteEdit(
    1L,
    noteId,
    pos,
    123L,
    false,
    text,
    imagePaths,
    imagePaths.isNotEmpty(),
    action
)

private fun note(id: Long = 1L): Note {
    val note = Note()
    note.id = id
    note.position = OsmLatLon(1.0, 2.0)
    return note
}
