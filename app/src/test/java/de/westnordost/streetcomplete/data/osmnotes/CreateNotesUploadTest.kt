package de.westnordost.streetcomplete.data.osmnotes

import android.os.CancellationSignal

import org.junit.Before
import org.junit.Test

import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.any

import org.mockito.Mockito.*

class CreateNotesUploadTest {
	private lateinit var createNoteDB: CreateNoteDao
	private lateinit var noteDB: NoteDao
	private lateinit var noteQuestDB: OsmNoteQuestDao
	private lateinit var mapDataDao: MapDataDao
	private lateinit var questType: OsmNoteQuestType
	private lateinit var statisticsDB: QuestStatisticsDao
	private lateinit var singleCreateNoteUpload: SingleCreateNoteUpload

    private lateinit var uploader: CreateNotesUpload

    @Before fun setUp() {
        mapDataDao = mock(MapDataDao::class.java)
	    noteQuestDB = mock(OsmNoteQuestDao::class.java)
	    noteDB = mock(NoteDao::class.java)
	    createNoteDB = mock(CreateNoteDao::class.java)
	    questType = mock(OsmNoteQuestType::class.java)
	    statisticsDB = mock(QuestStatisticsDao::class.java)
	    singleCreateNoteUpload = mock(SingleCreateNoteUpload::class.java)

        uploader = CreateNotesUpload(createNoteDB, noteDB, noteQuestDB, mapDataDao, questType,
	        statisticsDB, singleCreateNoteUpload)
    }

	@Test fun `cancel upload works`() {
		val signal = CancellationSignal()
		signal.cancel()
		uploader.upload(signal)
		verifyZeroInteractions(createNoteDB, noteDB, noteQuestDB, mapDataDao, questType,
			statisticsDB, singleCreateNoteUpload)
	}

	@Test fun `catches conflict exception`() {
		on(createNoteDB.getAll(null)).thenReturn(listOf(mock(CreateNote::class.java)))
		on(singleCreateNoteUpload.upload(any())).thenThrow(ConflictException())

		uploader.upload(CancellationSignal())

		// will not throw ElementConflictException
	}

	@Test fun `delete each uploaded quest in local DB and call listener`() {
		val createNotes = listOf( mock(CreateNote::class.java), mock(CreateNote::class.java))

		on(createNoteDB.getAll(null)).thenReturn(createNotes)
		on(singleCreateNoteUpload.upload(any())).thenReturn(mock(Note::class.java))

		uploader.uploadedChangeListener = mock(OnUploadedChangeListener::class.java)
		uploader.upload(CancellationSignal())

		for (createNote in createNotes) {
			verify(createNoteDB).delete(anyLong())
			verify(noteDB).put(any())
			verify(noteQuestDB).add(any())
			verify(statisticsDB).addOneNote()
			verify(uploader.uploadedChangeListener)?.onUploaded()
		}
	}

	@Test fun `delete each unsuccessfully uploaded quest in local DB and call listener`() {
		val createNotes = listOf( mock(CreateNote::class.java), mock(CreateNote::class.java))

		on(createNoteDB.getAll(null)).thenReturn(createNotes)
		on(singleCreateNoteUpload.upload(any())).thenThrow(ConflictException())

		uploader.uploadedChangeListener = mock(OnUploadedChangeListener::class.java)
		uploader.upload(CancellationSignal())

		for (createNote in createNotes) {
			verify(createNoteDB).delete(anyLong())
			verifyZeroInteractions(statisticsDB, noteQuestDB, noteDB)
			verify(uploader.uploadedChangeListener)?.onDiscarded()
		}
	}

	@Test fun `discard if element was deleted`() {
		val createNote = CreateNote()
		createNote.elementId = 1
		createNote.elementType = Element.Type.NODE

		on(createNoteDB.getAll(null)).thenReturn(listOf(createNote))
		on(mapDataDao.getNode(anyLong())).thenReturn(null)

		uploader.uploadedChangeListener = mock(OnUploadedChangeListener::class.java)
		uploader.upload(CancellationSignal())

		verify(uploader.uploadedChangeListener)?.onDiscarded()
	}
}
