package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.testutils.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify

class NotesDownloaderTest {
    private lateinit var notesApi: NotesApi
    private lateinit var noteController: NoteController

    @Before fun setUp() {
        notesApi = mock()
        noteController = mock()
    }

    @Test fun `calls controller with all notes coming from the notes api`() = runBlocking {
        val note1 = note()
        val noteApi = TestListBasedNotesApi(listOf(note1))
        val dl = NotesDownloader(noteApi, noteController)
        val bbox = bbox()
        dl.download(bbox)

        verify(noteController).putAllForBBox(eq(bbox), eq(listOf(note1)))
    }
}

private class TestListBasedNotesApi(val notes: List<Note>) : NotesApiImpl(null) {
    override fun getAll(bounds: BoundingBox, handler: (Note) -> Unit, limit: Int, hideClosedNoteAfter: Int) {
        for (note in notes) {
            handler(note)
        }
    }
}
