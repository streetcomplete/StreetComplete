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
        val noteApi = TestListBasedNotesApi(arrayListOf(note1))
        val dl = NotesDownloader(noteApi, noteController)
        val bbox = bbox()
        dl.download(bbox)

        verify(noteController).putAllForBBox(eq(bbox), eq(listOf(note1)))
    }
}

private class TestListBasedNotesApi(val notes: ArrayList<Note>) : NotesApiImpl(null) {
    override suspend fun getAll(bounds: BoundingBox, limit: Int, hideClosedNoteAfter: Int) = notes
}
