package de.westnordost.streetcomplete.data.osmnotes

import org.junit.Before
import org.junit.Test

import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.any

import org.mockito.Mockito.*
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.notes.NoteComment
import de.westnordost.streetcomplete.data.osm.ElementKey
import de.westnordost.streetcomplete.mock
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class CreateNotesUploaderTest {
    private lateinit var createNoteDB: CreateNoteDao
    private lateinit var noteDB: NoteDao
    private lateinit var noteQuestDB: OsmNoteQuestDao
    private lateinit var mapDataDao: MapDataDao
    private lateinit var questType: OsmNoteQuestType
    private lateinit var statisticsDB: QuestStatisticsDao
    private lateinit var singleCreateNoteUpload: SingleCreateNoteUpload

    private lateinit var uploader: CreateNotesUploader

    @Before fun setUp() {
        mapDataDao = mock()
        noteQuestDB = mock()
        noteDB = mock()
        createNoteDB = mock()
        questType = mock()
        statisticsDB = mock()
        singleCreateNoteUpload = mock()

        uploader = CreateNotesUploader(createNoteDB, noteDB, noteQuestDB, mapDataDao, questType,
            statisticsDB, singleCreateNoteUpload)
    }

    @Test fun `cancel upload works`() {
        val cancelled = AtomicBoolean(true)
        uploader.upload(cancelled)
        verifyZeroInteractions(createNoteDB, noteDB, noteQuestDB, mapDataDao, questType,
            statisticsDB, singleCreateNoteUpload)
    }

    @Test fun `catches conflict exception`() {
        on(createNoteDB.getAll()).thenReturn(listOf(newCreateNote()))
        on(singleCreateNoteUpload.upload(any())).thenThrow(ConflictException())

        uploader.upload(AtomicBoolean(false))

        // will not throw ElementConflictException
    }

    @Test fun `delete each uploaded quest in local DB and call listener`() {
        val createNotes = listOf( newCreateNote(), newCreateNote())

        on(createNoteDB.getAll()).thenReturn(createNotes)
        on(singleCreateNoteUpload.upload(any())).thenReturn(newNote())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(createNoteDB, times(createNotes.size)).delete(anyLong())
        verify(noteDB, times(createNotes.size)).put(any())
        verify(noteQuestDB, times(createNotes.size)).add(any())
        verify(statisticsDB, times(createNotes.size)).addOneNote()
        verify(uploader.uploadedChangeListener, times(createNotes.size))?.onUploaded()
    }

    @Test fun `delete each unsuccessfully uploaded quest in local DB and call listener`() {
        val createNotes = listOf( newCreateNote(), newCreateNote())

        on(createNoteDB.getAll()).thenReturn(createNotes)
        on(singleCreateNoteUpload.upload(any())).thenThrow(ConflictException())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(createNoteDB, times(createNotes.size)).delete(anyLong())
        verifyZeroInteractions(statisticsDB, noteQuestDB, noteDB)
        verify(uploader.uploadedChangeListener, times(createNotes.size))?.onDiscarded()
    }

    @Test fun `discard if element was deleted`() {
        val createNote = CreateNote(1, "jo ho", OsmLatLon(1.0, 2.0), null, ElementKey(Element.Type.NODE, 1))

        on(createNoteDB.getAll()).thenReturn(listOf(createNote))
        on(mapDataDao.getNode(anyLong())).thenReturn(null)

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(uploader.uploadedChangeListener)?.onDiscarded()
    }
}

private fun newNote(): Note {
    val note = Note()
    note.id = 2
    note.status = Note.Status.OPEN
    note.dateCreated = Date()
    note.position = OsmLatLon(1.0, 2.0)
    val comment = NoteComment()
    comment.text = "bla bla"
    comment.action = NoteComment.Action.OPENED
    comment.date = Date()
    note.comments.add(0, comment)
    return note
}

private fun newCreateNote() = CreateNote(1, "jo ho", OsmLatLon(1.0, 2.0), null, null, null)
