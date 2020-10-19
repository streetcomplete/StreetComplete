package de.westnordost.streetcomplete.data.osm.osmquest.undo

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementUpdateController
import de.westnordost.streetcomplete.data.osm.osmquest.SingleOsmElementTagChangesUploader
import de.westnordost.streetcomplete.data.osm.upload.ChangesetConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementDeletedException
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*
import java.util.concurrent.atomic.AtomicBoolean

class UndoOsmQuestsUploaderTest {
    private lateinit var undoQuestDB: UndoOsmQuestDao
    private lateinit var changesetManager: OpenQuestChangesetsManager
    private lateinit var singleChangeUploader: SingleOsmElementTagChangesUploader
    private lateinit var statisticsUpdater: StatisticsUpdater
    private lateinit var elementUpdateController: OsmElementUpdateController
    private lateinit var uploader: UndoOsmQuestsUploader

    @Before fun setUp() {
        undoQuestDB = mock()
        changesetManager = mock()
        singleChangeUploader = mock()
        statisticsUpdater = mock()
        elementUpdateController = mock()
        uploader = UndoOsmQuestsUploader(changesetManager, elementUpdateController,
            undoQuestDB, singleChangeUploader, statisticsUpdater)
    }

    @Test fun `cancel upload works`() {
        val cancelled = AtomicBoolean(true)
        uploader.upload(cancelled)
        verifyZeroInteractions(changesetManager, singleChangeUploader, statisticsUpdater, elementUpdateController, undoQuestDB)
    }

    @Test fun `catches ElementConflict exception`() {
        on(undoQuestDB.getAll()).thenReturn(listOf(createUndoQuest()))
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ElementConflictException())
        on(elementUpdateController.get(any(), anyLong())).thenReturn(mock())

        uploader.upload(AtomicBoolean(false))

        // will not throw ElementConflictException
    }

    @Test fun `discard if element was deleted`() {
        val q = createUndoQuest()
        on(undoQuestDB.getAll()).thenReturn(listOf(q))
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ElementDeletedException())
        on(elementUpdateController.get(any(), anyLong())).thenReturn(mock())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(uploader.uploadedChangeListener)?.onDiscarded(q.osmElementQuestType.javaClass.simpleName, q.position)
        verify(elementUpdateController).delete(any(), anyLong())
    }

    @Test fun `catches ChangesetConflictException exception and tries again once`() {
        on(undoQuestDB.getAll()).thenReturn(listOf(createUndoQuest()))
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

    @Test fun `delete each uploaded quest from local DB and calls listener`() {
        val quests = listOf(createUndoQuest(), createUndoQuest())

        on(undoQuestDB.getAll()).thenReturn(quests)
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ElementConflictException())
            .thenReturn(createElement())
        on(elementUpdateController.get(any(), anyLong())).thenReturn(mock())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(undoQuestDB, times(2)).delete(anyLong())
        verify(uploader.uploadedChangeListener)?.onUploaded(quests[0].osmElementQuestType.javaClass.simpleName, quests[0].position)
        verify(uploader.uploadedChangeListener)?.onDiscarded(quests[1].osmElementQuestType.javaClass.simpleName, quests[1].position)

        verify(elementUpdateController, times(1)).update(any(), isNull())
        verify(elementUpdateController, times(1)).cleanUp()
        verify(elementUpdateController, times(2)).get(any(), anyLong())
        verify(statisticsUpdater).subtractOne(any(), any())
        verifyNoMoreInteractions(elementUpdateController)
    }

    @Test fun `delete unreferenced elements and clean metadata at the end`() {
        val quest = createUndoQuest()

        on(undoQuestDB.getAll()).thenReturn(listOf(quest))
        on(singleChangeUploader.upload(anyLong(), any(), any())).thenReturn(createElement())
        on(elementUpdateController.get(any(), anyLong())).thenReturn(mock())

        uploader.upload(AtomicBoolean(false))

        verify(elementUpdateController).cleanUp()
        verify(quest.osmElementQuestType).cleanMetadata()
    }
}

private fun createUndoQuest() : UndoOsmQuest {
    val changes = StringMapChanges(listOf(StringMapEntryAdd("surface","asphalt")))
    val geometry = ElementPointGeometry(OsmLatLon(0.0, 0.0))
    return UndoOsmQuest(1, mock(), Element.Type.NODE, 1, changes, "survey", geometry)
}

private fun createElement() = OsmNode(1,1,OsmLatLon(0.0,0.0),null)
