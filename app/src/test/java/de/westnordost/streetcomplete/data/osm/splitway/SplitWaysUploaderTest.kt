package de.westnordost.streetcomplete.data.osm.splitway

import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestGiver
import de.westnordost.streetcomplete.data.osm.elementgeometry.OsmApiElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.mapdata.MergedElementDao
import de.westnordost.streetcomplete.data.osm.upload.ChangesetConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementConflictException
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
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
    private lateinit var elementGeometryCreator: OsmApiElementGeometryCreator
    private lateinit var splitSingleOsmWayUploader: SplitSingleWayUploader
    private lateinit var uploader: SplitWaysUploader

    @Before fun setUp() {
        splitWayDB = mock()
        elementDB = mock()
        on(elementDB.get(any(), ArgumentMatchers.anyLong())).thenReturn(createElement())
        changesetManager = mock()
        splitSingleOsmWayUploader = mock()
        elementGeometryDB = mock()
        questGiver = mock()
        elementGeometryCreator = mock()
        on(elementGeometryCreator.create(any())).thenReturn(mock())
        uploader = SplitWaysUploader(elementDB, elementGeometryDB, changesetManager, questGiver,
                elementGeometryCreator, splitWayDB, splitSingleOsmWayUploader)
    }

    @Test fun `cancel upload works`() {
        val cancelled = AtomicBoolean(true)
        uploader.upload(cancelled)
        verifyZeroInteractions(changesetManager, splitSingleOsmWayUploader, elementDB, splitWayDB)
    }

    @Test fun `catches ElementConflict exception`() {
        on(splitWayDB.getAll()).thenReturn(listOf(createOsmSplitWay()))
        on(splitSingleOsmWayUploader.upload(anyLong(), any(), anyList()))
            .thenThrow(ElementConflictException())

        uploader.upload(AtomicBoolean(false))

        // will not throw ElementConflictException
    }

    @Test fun `discard if element was deleted`() {
        val q = createOsmSplitWay()
        on(splitWayDB.getAll()).thenReturn(listOf(q))
        on(elementDB.get(any(),anyLong())).thenReturn(null)

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(uploader.uploadedChangeListener)?.onDiscarded(q.questType.javaClass.simpleName, q.position)
    }

    @Test fun `catches ChangesetConflictException exception and tries again once`() {
        on(splitWayDB.getAll()).thenReturn(listOf(createOsmSplitWay()))
        on(splitSingleOsmWayUploader.upload(anyLong(), any(), anyList()))
            .thenThrow(ChangesetConflictException())
            .thenReturn(listOf(createElement()))

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

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(splitWayDB, times(2)).delete(anyLong())
        verify(uploader.uploadedChangeListener)?.onUploaded(quests[0].questType.javaClass.simpleName, quests[0].position)
        verify(uploader.uploadedChangeListener)?.onDiscarded(quests[1].questType.javaClass.simpleName,quests[1].position)

        verify(elementDB, times(1)).put(any())
        verify(elementGeometryDB, times(1)).put(any())
        verify(questGiver, times(1)).updateQuests(any(), any())
        verifyNoMoreInteractions(questGiver)
    }

    @Test fun `delete unreferenced elements and clean metadata at the end`() {
        val quest = createOsmSplitWay()

        on(splitWayDB.getAll()).thenReturn(listOf(quest))
        on(splitSingleOsmWayUploader.upload(anyLong(), any(), any()))
            .thenReturn(listOf(createElement()))

        uploader.upload(AtomicBoolean(false))

        verify(elementDB).deleteUnreferenced()
        verify(quest.osmElementQuestType).cleanMetadata()
    }
}

private fun createOsmSplitWay() = OsmQuestSplitWay(1, mock(), 1, "survey", listOf(SplitAtPoint(OsmLatLon(1.0,0.1))))

private fun createElement() = OsmWay(1,1, listOf(1,2,3), null)
