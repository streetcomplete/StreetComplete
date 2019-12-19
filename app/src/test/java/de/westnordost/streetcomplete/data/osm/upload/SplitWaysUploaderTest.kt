package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.data.osm.OsmQuestSplitWay
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestSplitWayDao
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.OsmQuestGiver
import de.westnordost.streetcomplete.data.osm.download.OsmApiElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import de.westnordost.streetcomplete.mock
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.util.concurrent.atomic.AtomicBoolean

class SplitWaysUploaderTest {
    private lateinit var splitWayDB: OsmQuestSplitWayDao
    private lateinit var elementDB: MergedElementDao
    private lateinit var changesetManager: OpenQuestChangesetsManager
    private lateinit var elementGeometryDB: ElementGeometryDao
    private lateinit var questGiver: OsmQuestGiver
    private lateinit var statisticsDB: QuestStatisticsDao
    private lateinit var elementGeometryCreator: OsmApiElementGeometryCreator
    private lateinit var splitSingleOsmWayUpload: SplitSingleWayUpload
    private lateinit var uploader: SplitWaysUploader

    @Before fun setUp() {
        splitWayDB = mock()
        elementDB = mock()
        on(elementDB.get(any(), ArgumentMatchers.anyLong())).thenReturn(createElement())
        changesetManager = mock()
        splitSingleOsmWayUpload = mock()
        elementGeometryDB = mock()
        questGiver = mock()
        on(questGiver.updateQuests(any())).thenReturn(OsmQuestGiver.QuestUpdates(listOf(), listOf()))
        statisticsDB = mock()
        elementGeometryCreator = mock()
        on(elementGeometryCreator.create(any())).thenReturn(mock())
        uploader = SplitWaysUploader(elementDB, elementGeometryDB, changesetManager, questGiver,
            statisticsDB, elementGeometryCreator, splitWayDB, splitSingleOsmWayUpload)
    }

    @Test fun `cancel upload works`() {
        val cancelled = AtomicBoolean(true)
        uploader.upload(cancelled)
        verifyZeroInteractions(changesetManager, splitSingleOsmWayUpload, elementDB, splitWayDB)
    }

    @Test fun `catches ElementConflict exception`() {
        on(splitWayDB.getAll()).thenReturn(listOf(createOsmSplitWay()))
        on(splitSingleOsmWayUpload.upload(anyLong(), any(), anyList()))
            .thenThrow(ElementConflictException())

        uploader.upload(AtomicBoolean(false))

        // will not throw ElementConflictException
    }

    @Test fun `discard if element was deleted`() {
        on(splitWayDB.getAll()).thenReturn(listOf(createOsmSplitWay()))
        on(elementDB.get(any(),anyLong())).thenReturn(null)

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(uploader.uploadedChangeListener)?.onDiscarded()
    }

    @Test fun `catches ChangesetConflictException exception and tries again once`() {
        on(splitWayDB.getAll()).thenReturn(listOf(createOsmSplitWay()))
        on(splitSingleOsmWayUpload.upload(anyLong(), any(), anyList()))
            .thenThrow(ChangesetConflictException())
            .thenReturn(listOf(createElement()))

        uploader.upload(AtomicBoolean(false))

        // will not throw ChangesetConflictException but instead call single upload twice
        verify(changesetManager).getOrCreateChangeset(any(), any())
        verify(changesetManager).createChangeset(any(), any())
        verify(splitSingleOsmWayUpload, times(2)).upload(anyLong(), any(), anyList())
    }

    @Test fun `delete each uploaded split from local DB and calls listener`() {
        on(splitWayDB.getAll()).thenReturn(listOf(createOsmSplitWay(), createOsmSplitWay()))
        on(splitSingleOsmWayUpload.upload(anyLong(), any(), anyList()))
            .thenThrow(ElementConflictException())
            .thenReturn(listOf(createElement()))

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(splitWayDB, times(2)).delete(anyLong())
        verify(uploader.uploadedChangeListener)?.onUploaded()
        verify(uploader.uploadedChangeListener)?.onDiscarded()

        verify(elementDB, times(1)).put(any())
        verify(elementGeometryDB, times(1)).put(any())
        verify(questGiver, times(1)).updateQuests(any())
        verifyNoMoreInteractions(questGiver)
    }

    @Test fun `delete unreferenced elements and clean metadata at the end`() {
        val quest = createOsmSplitWay()

        on(splitWayDB.getAll()).thenReturn(listOf(quest))
        on(splitSingleOsmWayUpload.upload(anyLong(), any(), any()))
            .thenReturn(listOf(createElement()))

        uploader.upload(AtomicBoolean(false))

        verify(elementGeometryDB).deleteUnreferenced()
        verify(elementDB).deleteUnreferenced()
        verify(quest.osmElementQuestType).cleanMetadata()
    }
}

private fun createOsmSplitWay() = OsmQuestSplitWay(1, mock(), 1, "survey", listOf())

private fun createElement() = OsmWay(1,1, listOf(1,2,3), null)
