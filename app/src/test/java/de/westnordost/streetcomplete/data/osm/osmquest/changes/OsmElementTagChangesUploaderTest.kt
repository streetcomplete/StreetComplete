package de.westnordost.streetcomplete.data.osm.osmquest.changes

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.mapdata.OsmElementController
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

class OsmElementTagChangesUploaderTest {
    private lateinit var osmElementTagChangesDB: OsmElementTagChangesDao
    private lateinit var changesetManager: OpenQuestChangesetsManager
    private lateinit var singleChangeUploader: SingleOsmElementTagChangesUploader
    private lateinit var statisticsUpdater: StatisticsUpdater
    private lateinit var osmElementController: OsmElementController
    private lateinit var uploader: ElementTagChangesUploader

    @Before fun setUp() {
        osmElementTagChangesDB = mock()
        changesetManager = mock()
        singleChangeUploader = mock()
        statisticsUpdater = mock()
        osmElementController = mock()
        uploader = ElementTagChangesUploader(changesetManager, osmElementController,
            osmElementTagChangesDB, singleChangeUploader, statisticsUpdater)
    }

    @Test fun `cancel upload works`() {
        val cancelled = AtomicBoolean(true)
        uploader.upload(cancelled)
        verifyZeroInteractions(changesetManager, singleChangeUploader, statisticsUpdater, osmElementController, osmElementTagChangesDB)
    }

    @Test fun `catches ElementConflict exception`() {
        on(osmElementTagChangesDB.getAll()).thenReturn(listOf(createElementTagChanges()))
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ElementConflictException())
        on(osmElementController.get(any(), anyLong())).thenReturn(mock())

        uploader.upload(AtomicBoolean(false))

        // will not throw ElementConflictException
    }

    @Test fun `discard if element was deleted`() {
        val q = createElementTagChanges()
        on(osmElementTagChangesDB.getAll()).thenReturn(listOf(q))
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ElementDeletedException())
        on(osmElementController.get(any(), anyLong())).thenReturn(mock())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(uploader.uploadedChangeListener)?.onDiscarded(q.osmElementQuestType.javaClass.simpleName, q.position)
        verify(osmElementController).deleteAll(any())
    }

    @Test fun `catches ChangesetConflictException exception and tries again once`() {
        on(osmElementTagChangesDB.getAll()).thenReturn(listOf(createElementTagChanges()))
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ChangesetConflictException())
            .thenReturn(createElement())
        on(osmElementController.get(any(), anyLong())).thenReturn(mock())

        uploader.upload(AtomicBoolean(false))

        // will not throw ChangesetConflictException but instead call single upload twice
        verify(changesetManager).getOrCreateChangeset(any(), any())
        verify(changesetManager).createChangeset(any(), any())
        verify(singleChangeUploader, times(2)).upload(anyLong(), any(), any())
    }

    @Test fun `delete each uploaded quest from local DB and calls listener`() {
        val quests = listOf(createElementTagChanges(), createElementTagChanges())

        on(osmElementTagChangesDB.getAll()).thenReturn(quests)
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ElementConflictException())
            .thenReturn(createElement())
        on(osmElementController.get(any(), anyLong())).thenReturn(mock())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(osmElementTagChangesDB, times(2)).delete(anyLong())
        verify(uploader.uploadedChangeListener)?.onUploaded(quests[0].osmElementQuestType.javaClass.simpleName, quests[0].position)
        verify(uploader.uploadedChangeListener)?.onDiscarded(quests[1].osmElementQuestType.javaClass.simpleName, quests[1].position)

        verify(osmElementController, times(1)).putAll(any())
        verify(osmElementController, times(2)).get(any(), anyLong())
        verify(statisticsUpdater).subtractOne(any(), any())
        verifyNoMoreInteractions(osmElementController)
    }
}

private fun createElementTagChanges() : OsmElementTagChanges {
    val changes = StringMapChanges(listOf(StringMapEntryAdd("surface","asphalt")))
    val point = OsmLatLon(0.0, 0.0)
    return OsmElementTagChanges(1, mock(), Element.Type.NODE, 1, changes, "survey", point, false)
}

private fun createElement() = OsmNode(1,1,OsmLatLon(0.0,0.0),null)
