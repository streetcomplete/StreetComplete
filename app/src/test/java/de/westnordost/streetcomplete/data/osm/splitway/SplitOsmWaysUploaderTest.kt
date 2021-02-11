package de.westnordost.streetcomplete.data.osm.splitway

import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.mapdata.OsmElementController
import de.westnordost.streetcomplete.data.osm.changes.SplitOsmWay
import de.westnordost.streetcomplete.data.osm.upload.ChangesetConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementDeletedException
import de.westnordost.streetcomplete.data.osm.upload.SplitSingleOsmWayUploader
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import de.westnordost.streetcomplete.mock
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.util.concurrent.atomic.AtomicBoolean

class SplitOsmWaysUploaderTest {
    private lateinit var splitWayDB: SplitOsmWayDao
    private lateinit var changesetManager: OpenQuestChangesetsManager
    private lateinit var splitSingleOsmOsmWayUploader: SplitSingleOsmWayUploader
    private lateinit var statisticsUpdater: StatisticsUpdater
    private lateinit var osmElementController: OsmElementController
    private lateinit var uploader: SplitOsmWaysUploader

    @Before fun setUp() {
        splitWayDB = mock()
        changesetManager = mock()
        splitSingleOsmOsmWayUploader = mock()
        osmElementController = mock()
        statisticsUpdater = mock()
        uploader = SplitOsmWaysUploader(changesetManager, osmElementController, splitWayDB,
            splitSingleOsmOsmWayUploader, statisticsUpdater)
    }

    @Test fun `cancel upload works`() {
        val cancelled = AtomicBoolean(true)
        uploader.upload(cancelled)
        verifyZeroInteractions(changesetManager, splitSingleOsmOsmWayUploader, osmElementController, statisticsUpdater, splitWayDB)
    }

    @Test fun `catches ElementConflict exception`() {
        on(splitWayDB.getAll()).thenReturn(listOf(createOsmSplitWay()))
        on(splitSingleOsmOsmWayUploader.upload(anyLong(), any(), anyList()))
            .thenThrow(ElementConflictException())
        on(osmElementController.get(any(), anyLong())).thenReturn(createElement())

        uploader.upload(AtomicBoolean(false))

        // will not throw ElementConflictException
    }

    @Test fun `discard if element was deleted`() {
        val q = createOsmSplitWay()
        on(splitWayDB.getAll()).thenReturn(listOf(q))
        on(splitSingleOsmOsmWayUploader.upload(anyLong(), any(), any()))
            .thenThrow(ElementDeletedException())
        on(osmElementController.get(any(), anyLong())).thenReturn(createElement())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(uploader.uploadedChangeListener)?.onDiscarded(q.questType.javaClass.simpleName, q.position)
    }

    @Test fun `catches ChangesetConflictException exception and tries again once`() {
        on(splitWayDB.getAll()).thenReturn(listOf(createOsmSplitWay()))
        on(splitSingleOsmOsmWayUploader.upload(anyLong(), any(), anyList()))
            .thenThrow(ChangesetConflictException())
            .thenReturn(listOf(createElement()))
        on(osmElementController.get(any(), anyLong())).thenReturn(createElement())

        uploader.upload(AtomicBoolean(false))

        // will not throw ChangesetConflictException but instead call single upload twice
        verify(changesetManager).getOrCreateChangeset(any(), any())
        verify(changesetManager).createChangeset(any(), any())
        verify(splitSingleOsmOsmWayUploader, times(2)).upload(anyLong(), any(), anyList())
    }

    @Test fun `delete each uploaded split from local DB and calls listener`() {
        val quests = listOf(createOsmSplitWay(), createOsmSplitWay())

        on(splitWayDB.getAll()).thenReturn(quests)
        on(splitSingleOsmOsmWayUploader.upload(anyLong(), any(), anyList()))
            .thenThrow(ElementConflictException())
            .thenReturn(listOf(createElement()))
        on(osmElementController.get(any(), anyLong())).thenReturn(createElement())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(splitWayDB, times(2)).delete(anyLong())
        verify(uploader.uploadedChangeListener)?.onUploaded(quests[0].questType.javaClass.simpleName, quests[0].position)
        verify(uploader.uploadedChangeListener)?.onDiscarded(quests[1].questType.javaClass.simpleName,quests[1].position)

        verify(osmElementController, times(1)).putAll(any())
        verify(osmElementController, times(2)).get(any(), anyLong())
        verify(statisticsUpdater).addOne(any(), any())
        verifyNoMoreInteractions(osmElementController)
    }
}

private fun createOsmSplitWay() = SplitOsmWay(
    1,
    mock(),
    1,
    "survey",
    listOf(SplitAtPoint(OsmLatLon(1.0,0.1))))

private fun createElement() = OsmWay(1,1, listOf(1,2,3), null)
