package de.westnordost.streetcomplete.data.osmnotes.commentnotes

import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.notes.Note
import org.junit.Before

import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.any
import org.junit.Test

import org.mockito.Mockito.*
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.osmnotes.ImageUploadException
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteWithPhotosUploader
import de.westnordost.streetcomplete.mock
import java.util.concurrent.atomic.AtomicBoolean


class CommentNotesUploaderTest {
    private lateinit var commentNoteDB: CommentNoteDao
    private lateinit var singleNoteUploader: OsmNoteWithPhotosUploader
    private lateinit var noteController: NoteController

    private lateinit var uploader: CommentNotesUploader

    @Before fun setUp() {
        commentNoteDB = mock()
        noteController = mock()
        singleNoteUploader = mock()
        uploader = CommentNotesUploader(commentNoteDB, noteController, singleNoteUploader)
    }

    @Test fun `cancel upload works`() {
        uploader.upload(AtomicBoolean(true))
        verifyZeroInteractions(singleNoteUploader, commentNoteDB, noteController)
    }

    @Test fun `catches conflict exception`() {
        on(commentNoteDB.getAll()).thenReturn(listOf(note()))
        on(singleNoteUploader.comment(anyLong(),any(),any())).thenThrow(ConflictException())

        uploader.upload(AtomicBoolean(false))

        // will not throw ElementConflictException
    }

    @Test fun `delete each uploaded comment note in local DB and call listener`() {
        val notes = listOf(note(0L), note(1L))

        on(commentNoteDB.getAll()).thenReturn(notes)
        on(singleNoteUploader.comment(anyLong(),any(),any())).thenReturn(createNote())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(commentNoteDB, times(notes.size)).delete(anyLong())
        verify(noteController, times(notes.size)).put(any())
        verify(uploader.uploadedChangeListener, times(notes.size))?.onUploaded(any(), any())
    }

    @Test fun `delete each unsuccessfully uploaded comment note in local DB and call listener`() {
        val notes = listOf(note(0L), note(1L))

        on(commentNoteDB.getAll()).thenReturn(notes)
        on(singleNoteUploader.comment(anyLong(),any(),any())).thenThrow(ConflictException())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(commentNoteDB, times(notes.size)).delete(anyLong())
        verify(noteController, times(notes.size)).delete(any())
        verify(uploader.uploadedChangeListener, times(notes.size))?.onDiscarded(any(), any())
    }

    @Test fun `catches image upload exception`() {
        val note = note()
        on(commentNoteDB.getAll()).thenReturn(listOf(note))
        on(singleNoteUploader).thenThrow(ImageUploadException())

        uploader.upload(AtomicBoolean(false))

        verify(commentNoteDB, never()).delete(anyLong())
        // will not throw ElementConflictException and not delete the note from db
    }
}

private fun createNote(): Note {
    val note = Note()
    note.id = 1
    note.position = OsmLatLon(1.0, 2.0)
    return note
}

private fun note(
    noteId: Long = 123L,
    position: LatLon = OsmLatLon(1.5, 0.5),
    text: String = "bla",
    imagePaths: List<String>? = listOf("a", "b")
) = CommentNote(noteId, position, text, imagePaths)
