package de.westnordost.streetcomplete.data.osm.splitway

import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.mapdata.OsmElementController
import de.westnordost.streetcomplete.data.osm.upload.ChangesetConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementDeletedException
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import de.westnordost.streetcomplete.mock
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.util.concurrent.atomic.AtomicBoolean

class SplitWaysUploaderTest {
    private lateinit var splitWayDB: OsmQuestSplitWayDao
    private lateinit var changesetManager: OpenQuestChangesetsManager
    private lateinit var splitSingleOsmWayUploader: SplitSingleWayUploader
    private lateinit var statisticsUpdater: StatisticsUpdater
    private lateinit var osmElementController: OsmElementController
    private lateinit var uploader: SplitWaysUploader

    @Before fun setUp() {
        splitWayDB = mock()
        changesetManager = mock()
        splitSingleOsmWayUploader = mock()
        osmElementController = mock()
        statisticsUpdater = mock()
        uploader = SplitWaysUploader(changesetManager, osmElementController, splitWayDB,
            splitSingleOsmWayUploader, statisticsUpdater)
    }

    @Test fun `cancel upload works`() {
        val cancelled = AtomicBoolean(true)
        uploader.upload(cancelled)
        verifyZeroInteractions(changesetManager, splitSingleOsmWayUploader, osmElementController, statisticsUpdater, splitWayDB)
    }

    @Test fun `catches ElementConflict exception`() {
        on(splitWayDB.getAll()).thenReturn(listOf(createOsmSplitWay()))
        on(splitSingleOsmWayUploader.upload(anyLong(), any(), anyList()))
            .thenThrow(ElementConflictException())
        on(osmElementController.get(any(), anyLong())).thenReturn(createElement())

        uploader.upload(AtomicBoolean(false))

        // will not throw ElementConflictException
    }

    @Test fun `discard if element was deleted`() {
        val q = createOsmSplitWay()
        on(splitWayDB.getAll()).thenReturn(listOf(q))
        on(splitSingleOsmWayUploader.upload(anyLong(), any(), any()))
            .thenThrow(ElementDeletedException())
        on(osmElementController.get(any(), anyLong())).thenReturn(createElement())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(uploader.uploadedChangeListener)?.onDiscarded(q.questType.javaClass.simpleName, q.position)
    }

    @Test fun `catches ChangesetConflictException exception and tries again once`() {
        on(splitWayDB.getAll()).thenReturn(listOf(createOsmSplitWay()))
        on(splitSingleOsmWayUploader.upload(anyLong(), any(), anyList()))
            .thenThrow(ChangesetConflictException())
            .thenReturn(listOf(createElement()))
        on(osmElementController.get(any(), anyLong())).thenReturn(createElement())

        uploader.upload(AtomicBoolean(false))

        // will not throw ChangesetConflictException but instead call single upload twice
        verify(changesetManager).getOrCreateChangeset(any(), any())
        verify(changesetManager).createChangeset(any(), any())
        verify(splitSingleOsmWayUploader, times(2)).upload(anyLong(), any(), anyList())
    }

    @Test fun `delete each uploaded split from local DB and calls listener`() {
        val quests = listOf(createOsmSplitWay(), createOsmSplitWay())

        on(splitWayDB.getAll()).thenReturn(quests)
        on(splitSingleOsmWayUploader.upload(anyLong(), any(), anyList()))
            .thenThrow(ElementConflictException())
            .thenReturn(listOf(createElement()))
        on(osmElementController.get(any(), anyLong())).thenReturn(createElement())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(splitWayDB, times(2)).delete(anyLong())
        verify(uploader.uploadedChangeListener)?.onUploaded(quests[0].questType.javaClass.simpleName, quests[0].position)
        verify(uploader.uploadedChangeListener)?.onDiscarded(quests[1].questType.javaClass.simpleName,quests[1].position)

        verify(osmElementController, times(1)).put(any())
        verify(osmElementController, times(2)).get(any(), anyLong())
        verify(statisticsUpdater).addOne(any(), any())
        verifyNoMoreInteractions(osmElementController)
    }

    @Test fun `clean metadata at the end`() {
        val quest = createOsmSplitWay()

        on(splitWayDB.getAll()).thenReturn(listOf(quest))
        on(splitSingleOsmWayUploader.upload(anyLong(), any(), any()))
            .thenReturn(listOf(createElement()))
        on(osmElementController.get(any(), anyLong())).thenReturn(createElement())

        uploader.upload(AtomicBoolean(false))

        verify(quest.osmElementQuestType).deleteMetadataOlderThan()
    }
}

private fun createOsmSplitWay() = OsmQuestSplitWay(
    1,
    mock(),
    1,
    "survey",
    listOf(SplitAtPoint(OsmLatLon(1.0,0.1))),
    listOf(mock(), mock()))

private fun createElement() = OsmWay(1,1, listOf(1,2,3), null)
