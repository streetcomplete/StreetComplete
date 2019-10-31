package de.westnordost.streetcomplete.data.osmnotes

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
import de.westnordost.osmapi.map.data.OsmLatLon
import org.junit.Assert.assertEquals
import java.util.concurrent.atomic.AtomicBoolean


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
        uploader.upload(AtomicBoolean(true))
        verifyZeroInteractions(singleNoteUploader, questStatisticsDb, questDB, noteDB)
    }

    @Test fun `catches conflict exception`() {
        on(questDB.getAll(null, QuestStatus.ANSWERED)).thenReturn(listOf(createQuest()))
        on(singleNoteUploader.upload(any())).thenThrow(ConflictException())

        uploader.upload(AtomicBoolean(false))

        // will not throw ElementConflictException
    }

    @Test fun `close each uploaded quest in local DB and call listener`() {
        val quests = listOf( createQuest(), createQuest())

        on(questDB.getAll(null, QuestStatus.ANSWERED)).thenReturn(quests)
        on(singleNoteUploader.upload(any())).thenReturn(createNote())

        uploader.uploadedChangeListener = mock(OnUploadedChangeListener::class.java)
        uploader.upload(AtomicBoolean(false))

        for (quest in quests) {
            assertEquals(QuestStatus.CLOSED, quest.status)
            verify(questDB).update(quest)
        }
        verify(noteDB, times(quests.size)).put(any())
        verify(questStatisticsDb, times(quests.size)).addOneNote()
        verify(uploader.uploadedChangeListener, times(quests.size))?.onUploaded()
    }

    @Test fun `delete each unsuccessfully uploaded quest in local DB and call listener`() {
        val quests = listOf( createQuest(), createQuest())

        on(questDB.getAll(null, QuestStatus.ANSWERED)).thenReturn(quests)
        on(singleNoteUploader.upload(any())).thenThrow(ConflictException())

        uploader.uploadedChangeListener = mock(OnUploadedChangeListener::class.java)
        uploader.upload(AtomicBoolean(false))

        verify(questDB, times(quests.size)).delete(anyLong())
        verify(noteDB, times(quests.size)).delete(anyLong())
        verifyZeroInteractions(questStatisticsDb)
        verify(uploader.uploadedChangeListener, times(quests.size))?.onDiscarded()
    }
}

private fun createNote(): Note {
    val note = Note()
    note.id = 1
    note.position = OsmLatLon(1.0, 2.0)
    return note
}

private fun createQuest(): OsmNoteQuest {
    val quest = OsmNoteQuest(createNote(), OsmNoteQuestType())
    quest.id = 3
    quest.status = QuestStatus.NEW
    quest.comment = "blablub"
    return quest
}
