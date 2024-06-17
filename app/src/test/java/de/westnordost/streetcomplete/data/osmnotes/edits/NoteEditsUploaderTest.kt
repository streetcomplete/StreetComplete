package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.NotesApi
import de.westnordost.streetcomplete.data.osmnotes.StreetCompleteImageUploader
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.osmtracks.TracksApi
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.testutils.coVerifyInvokedExactlyOnce
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.noteEdit
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.verifyInvokedExactly
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.coEvery
import io.mockative.every
import io.mockative.mock
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test

class NoteEditsUploaderTest {

    @Mock private lateinit var noteController: NoteController
    @Mock private lateinit var noteEditsController: NoteEditsController
    @Mock private lateinit var notesApi: NotesApi
    @Mock private lateinit var tracksApi: TracksApi
    // todo mocking HttpClient doesn't work, no mock class is generated because it's final
    // private val validResponseMockEngine = MockEngine { _ -> respondOk("simple response") }
    private lateinit var imageUploader: StreetCompleteImageUploader
    @Mock private lateinit var userDataSource: UserDataSource

    private lateinit var uploader: NoteEditsUploader
    @Mock private lateinit var listener: OnUploadedChangeListener

    @BeforeTest fun setUp() {
        notesApi = mock(classOf<NotesApi>())
        noteController = mock(classOf<NoteController>())
        noteEditsController = mock(classOf<NoteEditsController>())
        userDataSource = mock(classOf<UserDataSource>())

        every { noteEditsController.getOldestNeedingImagesActivation() }.returns(null)
        every { noteEditsController.getOldestUnsynced() }.returns(null)

        every { notesApi.comment(any(), any()) }.returns(note())
        every { notesApi.create(any(), any()) }.returns(note())

        tracksApi = mock(classOf<TracksApi>())
        // imageUploader = StreetCompleteImageUploader(HttpClient(validResponseMockEngine), "osm.org")
        imageUploader = mock(classOf<StreetCompleteImageUploader>())
        listener = mock(classOf<OnUploadedChangeListener>())

        uploader = NoteEditsUploader(noteEditsController, noteController, userDataSource, notesApi, tracksApi, imageUploader)
        uploader.uploadedChangeListener = listener
    }

    @Test fun `cancel upload works`() = runBlocking {
        val job = launch { uploader.upload() }
        job.cancelAndJoin()
        // verifyNoInteractions(noteEditsController, noteController, notesApi, imageUploader)
    }

    @Test fun `upload note comment`() {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(noteId = 1L, action = NoteEditAction.COMMENT, text = "abc", pos = pos)
        val note = note(id = 1L)

        every { noteEditsController.getOldestUnsynced() }.returnsMany(edit, null)
        every { notesApi.comment(any(), any()) }.returns(note)

        upload()

        verifyInvokedExactlyOnce { notesApi.comment(1L, "abc") }
        verifyInvokedExactlyOnce { noteController.put(note) }
        verifyInvokedExactlyOnce { noteEditsController.markSynced(edit, note) }
        // verifyNoInteractions(imageUploader)
        verifyInvokedExactlyOnce { listener.onUploaded("NOTE", pos) }
    }

    @Test fun `upload create note`() {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(noteId = -5L, action = NoteEditAction.CREATE, text = "abc", pos = pos)
        val note = note(123)

        every { noteEditsController.getOldestUnsynced() }.returnsMany(edit, null)
        every { notesApi.create(any(), any()) }.returns(note)

        upload()

        verifyInvokedExactlyOnce { notesApi.create(pos, "abc") }
        verifyInvokedExactlyOnce { noteController.put(note) }
        verifyInvokedExactlyOnce { noteEditsController.markSynced(edit, note) }
        // verifyNoInteractions(imageUploader)
        verifyInvokedExactlyOnce { listener.onUploaded("NOTE", pos) }
    }

    @Test fun `fail uploading note comment because of a conflict`() {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(noteId = 1L, action = NoteEditAction.COMMENT, text = "abc", pos = pos)
        val note = note(1)

        every { noteEditsController.getOldestUnsynced() }.returnsMany(edit, null)
        every { notesApi.comment(any(), any()) }.throws(ConflictException())
        every { notesApi.get(1L) }.returns(note)

        upload()

        verifyInvokedExactlyOnce { notesApi.comment(1L, "abc") }
        verifyInvokedExactlyOnce { noteController.put(note) }
        verifyInvokedExactlyOnce { noteEditsController.markSyncFailed(edit) }
        // verifyNoInteractions(imageUploader)
        verifyInvokedExactlyOnce { listener.onDiscarded("NOTE", pos) }
    }

    @Test fun `fail uploading note comment because note was deleted`() {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(noteId = 1L, action = NoteEditAction.COMMENT, text = "abc", pos = pos)
        val note = note(1)

        every { noteEditsController.getOldestUnsynced() }.returnsMany(edit, null)
        every { notesApi.comment(any(), any()) }.throws(ConflictException())
        every { notesApi.get(1L) }.returns(null)

        upload()

        verifyInvokedExactlyOnce { notesApi.comment(1L, "abc") }
        verifyInvokedExactlyOnce { noteController.delete(note.id) }
        verifyInvokedExactlyOnce { noteEditsController.markSyncFailed(edit) }
        // verifyNoInteractions(imageUploader)
        verifyInvokedExactlyOnce { listener.onDiscarded("NOTE", pos) }
    }

    @Test fun `upload several note edits`() {
        every { noteEditsController.getOldestUnsynced() }.returnsMany(noteEdit(), noteEdit(), null)
        every { notesApi.comment(any(), any()) }.returns(note())

        upload()

        verifyInvokedExactly(2) { notesApi.comment(any(), any()) }
        verifyInvokedExactly(2) { noteController.put(any()) }
        verifyInvokedExactly(2) { noteEditsController.markSynced(any(), any()) }
        verifyInvokedExactly(2) { listener.onUploaded(any(), any()) }
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

        every { noteEditsController.getOldestUnsynced() }.returnsMany(edit, null)
        every { notesApi.comment(any(), any()) }.returns(note)
        coEvery { imageUploader.upload(any()) }.returns(listOf("x", "y", "z"))

        upload()

        verifyInvokedExactlyOnce { notesApi.comment(1L, "test\n\nAttached photo(s):\nx\ny\nz") }
        verifyInvokedExactlyOnce { noteController.put(note) }
        verifyInvokedExactlyOnce { noteEditsController.markSynced(edit, note) }
        verifyInvokedExactlyOnce { noteEditsController.markImagesActivated(1L) }
        coVerifyInvokedExactlyOnce { imageUploader.upload(listOf("a", "b", "c")) }
        coVerifyInvokedExactlyOnce { imageUploader.activate(1L) }
        verifyInvokedExactlyOnce { listener.onUploaded("NOTE", pos) }
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

        every { noteEditsController.getOldestUnsynced() }.returnsMany(edit, null)
        every { notesApi.create(any(), any()) }.returns(note)
        coEvery { imageUploader.upload(any()) }.returns(listOf("x", "y", "z"))

        upload()

        verifyInvokedExactlyOnce { notesApi.create(pos, "test\n\nAttached photo(s):\nx\ny\nz") }
        verifyInvokedExactlyOnce { noteController.put(note) }
        verifyInvokedExactlyOnce { noteEditsController.markSynced(edit, note) }
        verifyInvokedExactlyOnce { noteEditsController.markImagesActivated(1L) }
        coVerifyInvokedExactlyOnce { imageUploader.upload(listOf("a", "b", "c")) }
        coVerifyInvokedExactlyOnce { imageUploader.activate(1L) }
        verifyInvokedExactlyOnce { listener.onUploaded("NOTE", pos) }
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

        every { noteEditsController.getOldestUnsynced() }.returnsMany(edit, null)
        every { userDataSource.userName }.returns("blah mc/Blah")
        every { notesApi.create(any(), any()) }.returns(note)
        every { tracksApi.create(edit.track, edit.text) }.returns(988)

        upload()

        verifyInvokedExactlyOnce { notesApi.create(pos, "test\n\nGPS Trace: https://www.openstreetmap.org/user/blah%20mc%2FBlah/traces/988\n") }
        verifyInvokedExactlyOnce { noteController.put(note) }
        verifyInvokedExactlyOnce { noteEditsController.markSynced(edit, note) }
        verifyInvokedExactlyOnce { listener.onUploaded("NOTE", pos) }
    }

    @Test fun `upload missed image activations`(): Unit = runBlocking {
        val edit = noteEdit(noteId = 3)

        every { noteEditsController.getOldestNeedingImagesActivation() }.returnsMany(edit, null)

        upload()

        coVerifyInvokedExactlyOnce { imageUploader.activate(3) }
        verifyInvokedExactlyOnce { noteEditsController.markImagesActivated(1L) }
    }

    private fun upload() = runBlocking {
        uploader.upload()
    }
}
