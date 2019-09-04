package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.OsmQuestGiver
import de.westnordost.streetcomplete.data.osm.UndoOsmQuest
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestDao
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.on
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*
import java.util.concurrent.atomic.AtomicBoolean

class UndoOsmQuestsUploadTest {
    private lateinit var undoQuestDB: UndoOsmQuestDao
    private lateinit var elementDB: MergedElementDao
    private lateinit var changesetManager: OpenQuestChangesetsManager
    private lateinit var elementGeometryDB: ElementGeometryDao
    private lateinit var questGiver: OsmQuestGiver
    private lateinit var statisticsDB: QuestStatisticsDao
    private lateinit var elementGeometryCreator: ElementGeometryCreator
    private lateinit var singleChangeUpload: SingleOsmElementTagChangesUpload
    private lateinit var uploader: UndoOsmQuestsUpload

    @Before fun setUp() {
        undoQuestDB = mock(UndoOsmQuestDao::class.java)
        elementDB = mock(MergedElementDao::class.java)
        on(elementDB.get(any(), anyLong())).thenReturn(mock(Element::class.java))
        changesetManager = mock(OpenQuestChangesetsManager::class.java)
        singleChangeUpload = mock(SingleOsmElementTagChangesUpload::class.java)
        elementGeometryDB = mock(ElementGeometryDao::class.java)
        questGiver = mock(OsmQuestGiver::class.java)
        on(questGiver.updateQuests(any())).thenReturn(OsmQuestGiver.QuestUpdates())
        statisticsDB = mock(QuestStatisticsDao::class.java)
        elementGeometryCreator = mock(ElementGeometryCreator::class.java)
        uploader = UndoOsmQuestsUpload(elementDB, elementGeometryDB, changesetManager, questGiver,
            statisticsDB, elementGeometryCreator, undoQuestDB, singleChangeUpload)
    }

    @Test fun `cancel upload works`() {
        val cancelled = AtomicBoolean(true)
        uploader.upload(cancelled)
        verifyZeroInteractions(changesetManager, singleChangeUpload, elementDB, undoQuestDB)
    }

    @Test fun `catches ElementConflict exception`() {
        on(undoQuestDB.getAll()).thenReturn(listOf(createUndoQuest()))
        on(singleChangeUpload.upload(anyLong(), any(), any()))
            .thenThrow(ElementConflictException())

        uploader.upload(AtomicBoolean(false))

        // will not throw ElementConflictException
    }

    @Test fun `discard if element was deleted`() {
        on(undoQuestDB.getAll()).thenReturn(listOf(createUndoQuest()))
        on(elementDB.get(any(), anyLong())).thenReturn(null)

        uploader.uploadedChangeListener = mock(OnUploadedChangeListener::class.java)
        uploader.upload(AtomicBoolean(false))

        verify(uploader.uploadedChangeListener)?.onDiscarded()
    }

    @Test fun `catches ChangesetConflictException exception and tries again once`() {
        on(undoQuestDB.getAll()).thenReturn(listOf(createUndoQuest()))
        on(singleChangeUpload.upload(anyLong(), any(), any()))
            .thenThrow(ChangesetConflictException())
            .thenReturn(mock(Element::class.java))

        uploader.upload(AtomicBoolean(false))

        // will not throw ChangesetConflictException but instead call single upload twice
        verify(changesetManager).getOrCreateChangeset(any(), any())
        verify(changesetManager).createChangeset(any(), any())
        verify(singleChangeUpload, times(2)).upload(anyLong(), any(), any())
    }

    @Test fun `delete each uploaded quest from local DB and calls listener`() {
        on(undoQuestDB.getAll()).thenReturn(listOf(createUndoQuest(), createUndoQuest()))
        on(singleChangeUpload.upload(anyLong(), any(), any()))
            .thenThrow(ElementConflictException())
            .thenReturn(mock(Element::class.java))

        uploader.uploadedChangeListener = mock(OnUploadedChangeListener::class.java)
        uploader.upload(AtomicBoolean(false))

        verify(undoQuestDB, times(2)).delete(anyLong())
        verify(uploader.uploadedChangeListener)?.onUploaded()
        verify(uploader.uploadedChangeListener)?.onDiscarded()

        verify(elementDB, times(1)).put(any())
        verify(elementGeometryDB, times(1)).put(any(), anyLong(), any())
        verify(questGiver, times(1)).updateQuests(any())
        verifyNoMoreInteractions(questGiver)
    }

    @Test fun `delete unreferenced elements and clean metadata at the end`() {
        val quest = createUndoQuest()

        on(undoQuestDB.getAll()).thenReturn(listOf(quest))
        on(singleChangeUpload.upload(anyLong(), any(), any()))
            .thenReturn(mock(Element::class.java))

        uploader.upload(AtomicBoolean(false))

        verify(elementGeometryDB).deleteUnreferenced()
        verify(elementDB).deleteUnreferenced()
        verify(quest.osmElementQuestType).cleanMetadata()
    }
}

private fun createUndoQuest() : UndoOsmQuest {
    val changes = StringMapChanges(listOf(StringMapEntryAdd("surface","asphalt")))
    val geometry = ElementGeometry(OsmLatLon(0.0,0.0))
    val questType = mock(OsmElementQuestType::class.java)
    return UndoOsmQuest(1, questType, Element.Type.NODE, 1, changes, "survey", geometry)
}
