package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.testutils.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.verify

class NotesDownloaderTest {
    private lateinit var noteController: NoteController
    private lateinit var notesApi: NotesApi

    @Before fun setUp() {
        noteController = mock()
        notesApi = mock()
    }

    @Test fun `calls controller with all notes coming from the notes api`() = runBlocking {
        val note1 = note()
        val bbox = bbox()

        on(notesApi.getAll(any(), anyInt(), anyInt())).thenReturn(listOf(note1))
        val dl = NotesDownloader(notesApi, noteController)
        dl.download(bbox)

        verify(noteController).putAllForBBox(eq(bbox), eq(listOf(note1)))
    }
}
