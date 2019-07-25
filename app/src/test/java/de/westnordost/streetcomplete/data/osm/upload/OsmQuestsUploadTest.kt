package de.westnordost.streetcomplete.data.osm.upload

import android.os.CancellationSignal
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.osm.OsmQuest
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.on
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*

class OsmQuestsUploadTest {
    private lateinit var questDB: OsmQuestDao
    private lateinit var elementDB: MergedElementDao
    private lateinit var changesetManager: OpenQuestChangesetsManager
    private lateinit var singleChangeUpload: SingleOsmElementTagChangesUpload
    private lateinit var uploader: OsmQuestsUpload

    @Before fun setUp() {
        questDB = mock(OsmQuestDao::class.java)
        elementDB = mock(MergedElementDao::class.java)
        changesetManager = mock(OpenQuestChangesetsManager::class.java)
        singleChangeUpload = mock(SingleOsmElementTagChangesUpload::class.java)
        uploader = OsmQuestsUpload(questDB, elementDB, changesetManager, singleChangeUpload)
    }

    @Test fun `cancel upload works`() {
        val signal = CancellationSignal()
        signal.cancel()
        uploader.upload(signal)
        verifyZeroInteractions(changesetManager, singleChangeUpload, elementDB, questDB)
    }

    @Test fun `catches ElementConflict exception`() {
        on(questDB.getAll(null, QuestStatus.ANSWERED)).thenReturn(listOf(
            mock(OsmQuest::class.java)
        ))
        on(singleChangeUpload.upload(anyLong(), any(), any()))
            .thenThrow(ElementConflictException())

        uploader.upload(CancellationSignal())

        // will not throw ElementConflictException
    }

    @Test fun `discard if element was deleted`() {
        on(questDB.getAll(null, QuestStatus.ANSWERED)).thenReturn(listOf(
            mock(OsmQuest::class.java)
        ))
        on(elementDB.get(any(), anyLong())).thenReturn(null)

        uploader.uploadedChangeListener = mock(OnUploadedChangeListener::class.java)
        uploader.upload(CancellationSignal())

        verify(uploader.uploadedChangeListener)?.onDiscarded()
    }

    @Test fun `catches ChangesetConflictException exception and tries again once`() {
        on(questDB.getAll(null, QuestStatus.ANSWERED)).thenReturn(listOf(
            mock(OsmQuest::class.java)
        ))
        on(singleChangeUpload.upload(anyLong(), any(), any()))
            .thenThrow(ChangesetConflictException())
            .thenReturn(mock(Element::class.java))

        uploader.upload(CancellationSignal())

        // will not throw ChangesetConflictException but instead call single upload twice
        verify(changesetManager).getOrCreateChangeset(any(), any())
        verify(changesetManager).createChangeset(any(), any())
        verify(singleChangeUpload, times(2)).upload(anyLong(), any(), any())
    }

    @Test fun `close each uploaded quest in local DB and call listener`() {
        val quests = listOf( mock(OsmQuest::class.java), mock(OsmQuest::class.java))

        on(questDB.getAll(null, QuestStatus.ANSWERED)).thenReturn(quests)
        on(singleChangeUpload.upload(anyLong(), any(), any()))
            .thenReturn(mock(Element::class.java))

        uploader.uploadedChangeListener = mock(OnUploadedChangeListener::class.java)
        uploader.upload(CancellationSignal())

        for (quest in quests) {
            verify(quest).status = QuestStatus.CLOSED
            verify(questDB).update(quest)
            verify(uploader.uploadedChangeListener)?.onUploaded()
        }
    }

    @Test fun `delete each unsuccessful upload from local DB and call listener`() {
        val quests = listOf( mock(OsmQuest::class.java), mock(OsmQuest::class.java))

        on(questDB.getAll(null, QuestStatus.ANSWERED)).thenReturn(quests)
        on(singleChangeUpload.upload(anyLong(), any(), any()))
            .thenThrow(ElementConflictException())

        uploader.uploadedChangeListener = mock(OnUploadedChangeListener::class.java)
        uploader.upload(CancellationSignal())

        for (quest in quests) {
            verify(questDB).delete(anyLong())
            verify(uploader.uploadedChangeListener)?.onDiscarded()
        }
    }
}
