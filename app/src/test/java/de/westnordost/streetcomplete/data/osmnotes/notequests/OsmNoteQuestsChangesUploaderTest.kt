package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.osmapi.notes.Note
import org.junit.Before

import de.westnordost.streetcomplete.data.quest.QuestStatus
import de.westnordost.streetcomplete.data.osm.upload.ConflictException
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.any
import org.junit.Test

import org.mockito.Mockito.*
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.osmnotes.ImageUploadException
import de.westnordost.streetcomplete.data.osmnotes.NoteDao
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteWithPhotosUploader
import de.westnordost.streetcomplete.mock
import org.junit.Assert.assertEquals
import java.util.concurrent.atomic.AtomicBoolean


class OsmNoteQuestsChangesUploaderTest {
    private lateinit var noteDB: NoteDao
    private lateinit var questDB: OsmNoteQuestDao
    private lateinit var singleNoteUploader: OsmNoteWithPhotosUploader
    private lateinit var uploader: OsmNoteQuestsChangesUploader

    @Before fun setUp() {
        noteDB = mock()
        questDB = mock()
        singleNoteUploader = mock()
        uploader = OsmNoteQuestsChangesUploader(questDB, noteDB, singleNoteUploader)
    }

    @Test fun `cancel upload works`() {
        uploader.upload(AtomicBoolean(true))
        verifyZeroInteractions(singleNoteUploader, questDB, noteDB)
    }

    @Test fun `catches conflict exception`() {
        on(questDB.getAll(statusIn = listOf(QuestStatus.ANSWERED))).thenReturn(listOf(createQuest()))
        on(singleNoteUploader.comment(anyLong(),any(),any())).thenThrow(ConflictException())

        uploader.upload(AtomicBoolean(false))

        // will not throw ElementConflictException
    }

    @Test fun `close each uploaded quest in local DB and call listener`() {
        val quests = listOf(createQuest(), createQuest())

        on(questDB.getAll(statusIn = listOf(QuestStatus.ANSWERED))).thenReturn(quests)
        on(singleNoteUploader.comment(anyLong(),any(),any())).thenReturn(createNote())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        for (quest in quests) {
            assertEquals(QuestStatus.CLOSED, quest.status)
            verify(questDB).update(quest)
        }
        verify(noteDB, times(quests.size)).put(any())
        verify(uploader.uploadedChangeListener, times(quests.size))?.onUploaded(any(), any())
    }

    @Test fun `delete each unsuccessfully uploaded quest in local DB and call listener`() {
        val quests = listOf(createQuest(), createQuest())

        on(questDB.getAll(statusIn = listOf(QuestStatus.ANSWERED))).thenReturn(quests)
        on(singleNoteUploader.comment(anyLong(),any(),any())).thenThrow(ConflictException())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(questDB, times(quests.size)).delete(anyLong())
        verify(noteDB, times(quests.size)).delete(anyLong())
        verify(uploader.uploadedChangeListener, times(2))?.onDiscarded(any(), any())
    }

    @Test fun `catches image upload exception`() {
        val quest = createQuest()
        quest.imagePaths = listOf("hello")
        on(questDB.getAll(statusIn = listOf(QuestStatus.ANSWERED))).thenReturn(listOf(quest))
        on(singleNoteUploader.comment(anyLong(),any(),any())).thenThrow(ImageUploadException())

        uploader.upload(AtomicBoolean(false))

        // will not throw ElementConflictException, nor delete the note from db, nor update it
        verify(questDB, never()).delete(anyLong())
        verify(questDB, never()).update(any())
        verify(noteDB, never()).delete(anyLong())
        verify(noteDB, never()).put(any())
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
