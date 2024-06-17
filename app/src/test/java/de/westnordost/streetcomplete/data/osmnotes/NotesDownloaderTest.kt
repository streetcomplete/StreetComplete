package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test

class NotesDownloaderTest {
    @Mock private lateinit var noteController: NoteController
    @Mock private lateinit var notesApi: NotesApi

    @BeforeTest fun setUp() {
        noteController = mock(classOf<NoteController>())
        notesApi = mock(classOf<NotesApi>())
    }

    @Test fun `calls controller with all notes coming from the notes api`() = runBlocking {
        val note1 = note()
        val bbox = bbox()

        every { notesApi.getAll(any(), any(), any()) }.returns(listOf(note1))
        val dl = NotesDownloader(notesApi, noteController)
        dl.download(bbox)

        verifyInvokedExactlyOnce { noteController.putAllForBBox(bbox, listOf(note1)) }
    }
}
