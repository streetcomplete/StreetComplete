package de.westnordost.streetcomplete.data.osmnotes

import dev.mokkery.matcher.any
import de.westnordost.streetcomplete.testutils.bbox
import dev.mokkery.mock
import de.westnordost.streetcomplete.testutils.note
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import kotlinx.coroutines.runBlocking
import dev.mokkery.verify
import kotlin.test.BeforeTest
import kotlin.test.Test

class NotesDownloaderTest {
    private lateinit var noteController: NoteController
    private lateinit var notesApi: NotesApiClient

    @BeforeTest fun setUp() {
        noteController = mock()
        notesApi = mock()
    }

    @Test fun `calls controller with all notes coming from the notes api`() = runBlocking {
        val note1 = note()
        val bbox = bbox()

        everySuspend { notesApi.getAllOpen(any(), any()) } returns listOf(note1)
        val dl = NotesDownloader(notesApi, noteController)
        dl.download(bbox)

        verify { noteController.putAllForBBox(bbox, listOf(note1)) }
    }
}
