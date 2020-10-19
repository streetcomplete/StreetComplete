package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.quest.QuestStatus
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.osm.upload.ChangesetConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementDeletedException
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class OsmQuestsUploaderTest {
    private lateinit var osmQuestController: OsmQuestController
    private lateinit var changesetManager: OpenQuestChangesetsManager
    private lateinit var singleChangeUploader: SingleOsmElementTagChangesUploader
    private lateinit var statisticsUpdater: StatisticsUpdater
    private lateinit var elementUpdateController: OsmElementUpdateController
    private lateinit var uploader: OsmQuestsUploader

    @Before fun setUp() {
        osmQuestController = mock()
        changesetManager = mock()
        singleChangeUploader = mock()
        statisticsUpdater = mock()
        elementUpdateController = mock()
        uploader = OsmQuestsUploader(changesetManager, elementUpdateController,
            osmQuestController, singleChangeUploader, statisticsUpdater)
    }

    @Test fun `cancel upload works`() {
        uploader.upload(AtomicBoolean(true))
        verifyZeroInteractions(elementUpdateController, changesetManager, singleChangeUploader, statisticsUpdater, osmQuestController)
    }

    @Test fun `catches ElementConflict exception`() {
        on(osmQuestController.getAllAnswered()).thenReturn(listOf(createQuest()))
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ElementConflictException())
        on(elementUpdateController.get(any(), anyLong())).thenReturn(mock())

        uploader.upload(AtomicBoolean(false))

        // will not throw ElementConflictException
    }

    @Test fun `discard if element was deleted`() {
        val q = createQuest()
        on(osmQuestController.getAllAnswered()).thenReturn(listOf(q))
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ElementDeletedException())
        on(elementUpdateController.get(any(), anyLong())).thenReturn(mock())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(uploader.uploadedChangeListener)?.onDiscarded(q.osmElementQuestType.javaClass.simpleName, q.position)
        verify(elementUpdateController).delete(any(), anyLong())
    }

    @Test fun `catches ChangesetConflictException exception and tries again once`() {
        on(osmQuestController.getAllAnswered()).thenReturn(listOf(createQuest()))
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ChangesetConflictException())
            .thenReturn(createElement())
        on(elementUpdateController.get(any(), anyLong())).thenReturn(mock())

        uploader.upload(AtomicBoolean(false))

        // will not throw ChangesetConflictException but instead call single upload twice
        verify(changesetManager).getOrCreateChangeset(any(), any())
        verify(changesetManager).createChangeset(any(), any())
        verify(singleChangeUploader, times(2)).upload(anyLong(), any(), any())
    }

    @Test fun `close each uploaded quest in local DB and call listener`() {
        val quests = listOf(createQuest(), createQuest())

        on(osmQuestController.getAllAnswered()).thenReturn(quests)
        on(singleChangeUploader.upload(anyLong(), any(), any())).thenReturn(createElement())
        on(elementUpdateController.get(any(), anyLong())).thenReturn(mock())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(osmQuestController, times(2)).success(any())
        verify(uploader.uploadedChangeListener, times(2))?.onUploaded(any(), any())
        verify(elementUpdateController, times(2)).update(any(), isNull())
        verify(statisticsUpdater, times(2)).addOne(any(), any())
    }

    @Test fun `delete each unsuccessful upload from local DB and call listener`() {
        val quests = listOf(createQuest(), createQuest())

        on(osmQuestController.getAllAnswered()).thenReturn(quests)
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ElementConflictException())
        on(elementUpdateController.get(any(), anyLong())).thenReturn(mock())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(osmQuestController, times(2)).fail(any())
        verify(uploader.uploadedChangeListener,times(2))?.onDiscarded(any(), any())
        verify(elementUpdateController, times(2)).get(any(), anyLong())
        verify(elementUpdateController).cleanUp()
        verifyNoMoreInteractions(elementUpdateController)
        verifyZeroInteractions(statisticsUpdater)
    }

    @Test fun `delete unreferenced elements and clean metadata at the end`() {
        val quest = createQuest()

        on(osmQuestController.getAllAnswered()).thenReturn(listOf(quest))
        on(singleChangeUploader.upload(anyLong(), any(), any())).thenReturn(createElement())
        on(elementUpdateController.get(any(), anyLong())).thenReturn(mock())

        uploader.upload(AtomicBoolean(false))

        verify(elementUpdateController).cleanUp()
        verify(quest.osmElementQuestType).cleanMetadata()
    }
}

private fun createQuest() : OsmQuest {
    val changes = StringMapChanges(listOf(StringMapEntryAdd("surface","asphalt")))
    val geometry = ElementPointGeometry(OsmLatLon(0.0, 0.0))
    return OsmQuest(1L, mock(), Element.Type.NODE, 1L, QuestStatus.ANSWERED, changes, "survey",
            Date(), geometry)
}

private fun createElement() = OsmNode(1,1,OsmLatLon(0.0,0.0),null)
