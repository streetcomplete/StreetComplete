package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.NotesApiClient
import de.westnordost.streetcomplete.data.osmnotes.PhotoServiceApiClient
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.osmtracks.TracksApiClient
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.noteEdit
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.p
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import kotlin.test.BeforeTest
import kotlin.test.Test

class NoteEditsUploaderTest {

    private lateinit var noteController: NoteController
    private lateinit var noteEditsController: NoteEditsController
    private lateinit var notesApi: NotesApiClient
    private lateinit var tracksApi: TracksApiClient
    private lateinit var imageUploader: PhotoServiceApiClient
    private lateinit var userDataSource: UserDataSource

    private lateinit var uploader: NoteEditsUploader
    private lateinit var listener: OnUploadedChangeListener

    @BeforeTest fun setUp(): Unit = runBlocking {
        notesApi = mock()
        noteController = mock()
        noteEditsController = mock()
        userDataSource = mock()

        on(noteEditsController.getOldestNeedingImagesActivation()).thenReturn(null)
        on(noteEditsController.getOldestUnsynced()).thenReturn(null)

        on(notesApi.comment(anyLong(), any())).thenReturn(note())
        on(notesApi.create(any(), any())).thenReturn(note())

        tracksApi = mock()
        imageUploader = mock()
        listener = mock()

        uploader = NoteEditsUploader(noteEditsController, noteController, userDataSource, notesApi, tracksApi, imageUploader)
        uploader.uploadedChangeListener = listener
    }

    @Test fun `cancel upload works`() = runBlocking {
        val job = launch { uploader.upload() }
        job.cancelAndJoin()
        verifyNoInteractions(noteEditsController, noteController, notesApi, imageUploader)
    }

    @Test fun `upload note comment`(): Unit = runBlocking {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(noteId = 1L, action = NoteEditAction.COMMENT, text = "abc", pos = pos)
        val note = note(id = 1L)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(notesApi.comment(anyLong(), any())).thenReturn(note)

        upload()

        verify(notesApi).comment(1L, "abc")
        verify(noteController).put(note)
        verify(noteEditsController).markSynced(edit, note)
        verifyNoInteractions(imageUploader)
        verify(listener)!!.onUploaded("NOTE", pos)
    }

    @Test fun `upload create note`(): Unit = runBlocking {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(noteId = -5L, action = NoteEditAction.CREATE, text = "abc", pos = pos)
        val note = note(123)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(notesApi.create(any(), any())).thenReturn(note)

        upload()

        verify(notesApi).create(pos, "abc")
        verify(noteController).put(note)
        verify(noteEditsController).markSynced(edit, note)
        verifyNoInteractions(imageUploader)
        verify(listener)!!.onUploaded("NOTE", pos)
    }

    @Test fun `fail uploading note comment because of a conflict`(): Unit = runBlocking {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(noteId = 1L, action = NoteEditAction.COMMENT, text = "abc", pos = pos)
        val note = note(1)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(notesApi.comment(anyLong(), any())).thenThrow(ConflictException())
        on(notesApi.get(1L)).thenReturn(note)

        upload()

        verify(notesApi).comment(1L, "abc")
        verify(noteController).put(note)
        verify(noteEditsController).markSyncFailed(edit)
        verifyNoInteractions(imageUploader)
        verify(listener)!!.onDiscarded("NOTE", pos)
    }

    @Test fun `fail uploading note comment because note was deleted`(): Unit = runBlocking {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(noteId = 1L, action = NoteEditAction.COMMENT, text = "abc", pos = pos)
        val note = note(1)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(notesApi.comment(anyLong(), any())).thenThrow(ConflictException())
        on(notesApi.get(1L)).thenReturn(null)

        upload()

        verify(notesApi).comment(1L, "abc")
        verify(noteController).delete(note.id)
        verify(noteEditsController).markSyncFailed(edit)
        verifyNoInteractions(imageUploader)
        verify(listener)!!.onDiscarded("NOTE", pos)
    }

    @Test fun `upload several note edits`(): Unit = runBlocking {
        on(noteEditsController.getOldestUnsynced()).thenReturn(noteEdit()).thenReturn(noteEdit()).thenReturn(null)
        on(notesApi.comment(anyLong(), any())).thenReturn(note())

        upload()

        verify(notesApi, times(2)).comment(anyLong(), any())
        verify(noteController, times(2)).put(any())
        verify(noteEditsController, times(2)).markSynced(any(), any())
        verify(listener, times(2))!!.onUploaded(any(), any())
    }

    @Test fun `upload note comment with attached images`() = runBlocking {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(
            noteId = 1L,
            action = NoteEditAction.COMMENT,
            text = "test",
            pos = pos,
            imagePaths = listOf("a", "b", "c")
        )
        val note = note(1)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(notesApi.comment(anyLong(), any())).thenReturn(note)
        on(imageUploader.upload(any())).thenReturn(listOf("x", "y", "z"))

        upload()

        verify(notesApi).comment(1L, "test\n\nAttached photo(s):\nx\ny\nz")
        verify(noteController).put(note)
        verify(noteEditsController).markSynced(edit, note)
        verify(noteEditsController).markImagesActivated(1L)
        verify(imageUploader).upload(listOf("a", "b", "c"))
        verify(imageUploader).activate(1L)
        verify(listener)!!.onUploaded("NOTE", pos)
    }

    @Test fun `upload create note with attached images`() = runBlocking {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(
            noteId = 1L,
            action = NoteEditAction.CREATE,
            text = "test",
            pos = pos,
            imagePaths = listOf("a", "b", "c")
        )
        val note = note(1)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(notesApi.create(any(), any())).thenReturn(note)
        on(imageUploader.upload(any())).thenReturn(listOf("x", "y", "z"))

        upload()

        verify(notesApi).create(pos, "test\n\nAttached photo(s):\nx\ny\nz")
        verify(noteController).put(note)
        verify(noteEditsController).markSynced(edit, note)
        verify(noteEditsController).markImagesActivated(1L)
        verify(imageUploader).upload(listOf("a", "b", "c"))
        verify(imageUploader).activate(1L)
        verify(listener)!!.onUploaded("NOTE", pos)
    }

    @Test fun `upload create note with attached GPS trace`() = runBlocking {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(
            noteId = 1L,
            action = NoteEditAction.CREATE,
            text = "test",
            pos = pos,
            track = listOf(Trackpoint(LatLon(0.0, 0.0), 180, 0.0f, 100.0f))
        )
        val note = note(1)

        on(noteEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(userDataSource.userName).thenReturn("blah mc/Blah")
        on(notesApi.create(any(), any())).thenReturn(note)
        on(tracksApi.create(edit.track, edit.text)).thenReturn(988)

        upload()

        verify(notesApi).create(pos, "test\n\nGPS Trace: https://www.openstreetmap.org/user/blah%20mc%2FBlah/traces/988\n")
        verify(noteController).put(note)
        verify(noteEditsController).markSynced(edit, note)
        verify(listener)!!.onUploaded("NOTE", pos)
    }

    @Test fun `upload missed image activations`(): Unit = runBlocking {
        val edit = noteEdit(noteId = 3)

        on(noteEditsController.getOldestNeedingImagesActivation()).thenReturn(edit).thenReturn(null)

        upload()

        verify(imageUploader).activate(3)
        verify(noteEditsController).markImagesActivated(1L)
    }

    private fun upload() = runBlocking {
        uploader.upload()
    }
}
