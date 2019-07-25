package de.westnordost.streetcomplete.data.osmnotes

import android.os.CancellationSignal
import de.westnordost.osmapi.notes.Note
import org.junit.Before

import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.any
import org.junit.Test

import org.mockito.Mockito.*

class OsmNoteQuestsChangesUploadTest {
    private lateinit var noteDB: NoteDao
    private lateinit var questDB: OsmNoteQuestDao
    private lateinit var questStatisticsDb: QuestStatisticsDao
    private lateinit var singleNoteUploader: SingleOsmNoteQuestChangesUpload
    private lateinit var uploader: OsmNoteQuestsChangesUpload

    @Before fun setUp() {
        noteDB = mock(NoteDao::class.java)
        questDB = mock(OsmNoteQuestDao::class.java)
        questStatisticsDb = mock(QuestStatisticsDao::class.java)
        singleNoteUploader = mock(SingleOsmNoteQuestChangesUpload::class.java)
        uploader = OsmNoteQuestsChangesUpload(questDB, questStatisticsDb, noteDB, singleNoteUploader)
    }

    @Test fun `cancel upload works`() {
        val signal = CancellationSignal()
        signal.cancel()
        uploader.upload(signal)
        verifyZeroInteractions(singleNoteUploader, questStatisticsDb, questDB, noteDB)
    }

    @Test fun `catches conflict exception`() {
        on(questDB.getAll(null, QuestStatus.ANSWERED)).thenReturn(listOf(
            mock(OsmNoteQuest::class.java)
        ))
        on(singleNoteUploader.upload(any())).thenThrow(ConflictException())

        uploader.upload(CancellationSignal())

        // will not throw ElementConflictException
    }

    @Test fun `close each uploaded quest in local DB and call listener`() {
        val quests = listOf( mock(OsmNoteQuest::class.java), mock(OsmNoteQuest::class.java))

        on(questDB.getAll(null, QuestStatus.ANSWERED)).thenReturn(quests)
        on(singleNoteUploader.upload(any())).thenReturn(mock(Note::class.java))

        uploader.uploadedChangeListener = mock(OnUploadedChangeListener::class.java)
        uploader.upload(CancellationSignal())

        for (quest in quests) {
            verify(quest).status = QuestStatus.CLOSED
            verify(questDB).update(quest)
            verify(noteDB).put(any())
            verify(questStatisticsDb).addOneNote()
            verify(uploader.uploadedChangeListener)?.onUploaded()
        }
    }

    @Test fun `delete each unsuccessfully uploaded quest in local DB and call listener`() {
        val quests = listOf( mock(OsmNoteQuest::class.java), mock(OsmNoteQuest::class.java))

        on(questDB.getAll(null, QuestStatus.ANSWERED)).thenReturn(quests)
        on(singleNoteUploader.upload(any())).thenThrow(ConflictException())

        uploader.uploadedChangeListener = mock(OnUploadedChangeListener::class.java)
        uploader.upload(CancellationSignal())

        for (quest in quests) {
            verify(questDB).delete(any())
            verify(noteDB).delete(any())
            verifyZeroInteractions(questStatisticsDb)
            verify(uploader.uploadedChangeListener)?.onDiscarded()
        }
    }
}
