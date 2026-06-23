package de.westnordost.streetcomplete.data.osmnotes.edits

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.NotesApiClient
import de.westnordost.streetcomplete.data.osmnotes.PhotoServiceApiClient
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.osmtracks.TracksApiClient
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.user.UserDataSource
import dev.mokkery.matcher.any
import dev.mokkery.mock
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.noteEdit
import dev.mokkery.answering.returns
import dev.mokkery.every
import de.westnordost.streetcomplete.testutils.p
import dev.mokkery.answering.repeat
import dev.mokkery.answering.sequentially
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.SystemFileSystem
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifyNoMoreCalls
import dev.mokkery.verifySuspend
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

    private val fileSystem = SystemFileSystem

    @BeforeTest fun setUp(): Unit = runBlocking {
        notesApi = mock() {
            everySuspend { comment(any(), any()) } returns note()
            everySuspend { create(any(), any()) } returns note()
        }
        noteController = mock()
        noteEditsController = mock() {
            every { getOldestNeedingImagesActivation() } returns null
            every { getOldestUnsynced() } returns null
        }
        userDataSource = mock()
        tracksApi = mock()
        imageUploader = mock()
        listener = mock()

        uploader = NoteEditsUploader(noteEditsController, noteController, userDataSource, notesApi, tracksApi, imageUploader, fileSystem)
        uploader.uploadedChangeListener = listener
    }

    @Test fun `cancel upload works`() = runBlocking {
        val job = launch { uploader.upload() }
        job.cancelAndJoin()
        verifyNoMoreCalls(noteEditsController, noteController, notesApi, imageUploader)
    }

    @Test fun `upload note comment`(): Unit = runBlocking {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(noteId = 1L, action = NoteEditAction.COMMENT, text = "abc", pos = pos)
        val note = note(id = 1L)

        every { noteEditsController.getOldestUnsynced() } sequentially {
            returns(edit)
            repeat { returns(null) }
        }
        everySuspend { notesApi.comment(any(), any()) } returns note

        upload()

        everySuspend { notesApi.comment(1L, "abc") }
        verify { noteController.put(note) }
        verify { noteEditsController.markSynced(edit, note) }
        verifyNoMoreCalls(imageUploader)
        verify { listener.onUploaded("NOTE", pos) }
    }

    @Test fun `upload create note`(): Unit = runBlocking {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(noteId = -5L, action = NoteEditAction.CREATE, text = "abc", pos = pos)
        val note = note(123)

        every { noteEditsController.getOldestUnsynced() } sequentially {
            returns(edit)
            repeat { returns(null) }
        }
        everySuspend { notesApi.create(any(), any()) } returns note

        upload()

        verifySuspend { notesApi.create(pos, "abc") }
        verify { noteController.put(note) }
        verify { noteEditsController.markSynced(edit, note) }
        verifyNoMoreCalls(imageUploader)
        verify { listener.onUploaded("NOTE", pos) }
    }

    @Test fun `fail uploading note comment because of a conflict`(): Unit = runBlocking {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(noteId = 1L, action = NoteEditAction.COMMENT, text = "abc", pos = pos)
        val note = note(1)

        every { noteEditsController.getOldestUnsynced() } sequentially {
            returns(edit)
            repeat { returns(null) }
        }
        everySuspend { notesApi.comment(any(), any()) } throws ConflictException()
        everySuspend { notesApi.get(1L) } returns note

        upload()

        verifySuspend { notesApi.comment(1L, "abc") }
        verify { noteController.put(note) }
        verify { noteEditsController.markSyncFailed(edit) }
        verifyNoMoreCalls(imageUploader)
        verify { listener.onDiscarded("NOTE", pos) }
    }

    @Test fun `fail uploading note comment because note was deleted`(): Unit = runBlocking {
        val pos = p(1.0, 13.0)
        val edit = noteEdit(noteId = 1L, action = NoteEditAction.COMMENT, text = "abc", pos = pos)
        val note = note(1)

        every { noteEditsController.getOldestUnsynced() } sequentially {
            returns(edit)
            repeat { returns(null) }
        }
        everySuspend { notesApi.comment(any(), any()) } throws ConflictException()
        everySuspend { notesApi.get(1L) } returns null

        upload()

        verifySuspend { notesApi.comment(1L, "abc") }
        verify { noteController.delete(note.id) }
        verify { noteEditsController.markSyncFailed(edit) }
        verifyNoMoreCalls(imageUploader)
        verify { listener.onDiscarded("NOTE", pos) }
    }

    @Test fun `upload several note edits`(): Unit = runBlocking {
        every { noteEditsController.getOldestUnsynced() } sequentially {
            returns(noteEdit())
            returns(noteEdit())
            repeat { returns(null) }
        }
        everySuspend { notesApi.comment(any(), any()) } returns note()

        upload()

        verifySuspend(exactly(2)) { notesApi.comment(any(), any()) }
        verify(exactly(2)) { noteController.put(any()) }
        verify(exactly(2)) { noteEditsController.markSynced(any(), any()) }
        verify(exactly(2)) { listener.onUploaded(any(), any()) }
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

        every { noteEditsController.getOldestUnsynced() } sequentially {
            returns(edit)
            repeat { returns(null) }
        }
        everySuspend { notesApi.comment(any(), any()) } returns note
        everySuspend { imageUploader.upload(any()) } returns listOf("x", "y", "z")

        upload()

        verifySuspend { notesApi.comment(1L, "test\n\nAttached photo(s):\nx\ny\nz") }
        verify { noteController.put(note) }
        verify { noteEditsController.markSynced(edit, note) }
        verify { noteEditsController.markImagesActivated(1L) }
        verifySuspend { imageUploader.upload(listOf("a", "b", "c")) }
        verifySuspend { imageUploader.activate(1L) }
        verify { listener.onUploaded("NOTE", pos) }
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

        every { noteEditsController.getOldestUnsynced() } sequentially {
            returns(edit)
            repeat { returns(null) }
        }
        everySuspend { notesApi.create(any(), any()) } returns note
        everySuspend { imageUploader.upload(any()) } returns listOf("x", "y", "z")

        upload()

        verifySuspend { notesApi.create(pos, "test\n\nAttached photo(s):\nx\ny\nz") }
        verify { noteController.put(note) }
        verify { noteEditsController.markSynced(edit, note) }
        verify { noteEditsController.markImagesActivated(1L) }
        verifySuspend { imageUploader.upload(listOf("a", "b", "c")) }
        verifySuspend { imageUploader.activate(1L) }
        verify { listener.onUploaded("NOTE", pos) }
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

        every { noteEditsController.getOldestUnsynced() } sequentially {
            returns(edit)
            repeat { returns(null) }
        }
        every { userDataSource.userName } returns "blah mc/Blah"
        everySuspend { notesApi.create(any(), any()) } returns note
        everySuspend {
            tracksApi.create(
                trackpoints = edit.track,
                creator = ApplicationConstants.USER_AGENT,
                description = edit.text,
                tags = listOf(ApplicationConstants.NAME)
            )
        } returns 988L

        upload()

        verifySuspend { notesApi.create(pos, "test\n\nGPS Trace: https://www.openstreetmap.org/user/blah%20mc%2FBlah/traces/988\n") }
        verify { noteController.put(note) }
        verify { noteEditsController.markSynced(edit, note) }
        verify { listener.onUploaded("NOTE", pos) }
    }

    @Test fun `upload missed image activations`(): Unit = runBlocking {
        val edit = noteEdit(noteId = 3)

        every { noteEditsController.getOldestNeedingImagesActivation() } sequentially {
            returns(edit)
            repeat { returns(null) }
        }

        upload()

        verifySuspend { imageUploader.activate(3) }
        verify { noteEditsController.markImagesActivated(1L) }
    }

    private fun upload() = runBlocking {
        uploader.upload()
    }
}
