package de.westnordost.streetcomplete.data.osm.upload

import android.os.CancellationSignal
import de.westnordost.streetcomplete.data.osm.OsmQuestSplitWay
import de.westnordost.streetcomplete.data.osm.persist.SplitWayDao
import de.westnordost.streetcomplete.data.osm.persist.WayDao
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.any
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class SplitWaysUploadTest {
    private lateinit var splitWayDB: SplitWayDao
    private lateinit var wayDao: WayDao
    private lateinit var changesetManager: OpenQuestChangesetsManager
    private lateinit var splitSingleOsmWayUpload: SplitSingleWayUpload
    private lateinit var uploader: SplitWaysUpload

    @Before fun setUp() {
        splitWayDB = mock(SplitWayDao::class.java)
        wayDao = mock(WayDao::class.java)
        changesetManager = mock(OpenQuestChangesetsManager::class.java)
        splitSingleOsmWayUpload = mock(SplitSingleWayUpload::class.java)
        uploader = SplitWaysUpload(splitWayDB, wayDao, changesetManager, splitSingleOsmWayUpload)
    }

    @Test fun `cancel upload works`() {
        val signal = CancellationSignal()
        signal.cancel()
        uploader.upload(signal)
        verifyZeroInteractions(changesetManager, splitSingleOsmWayUpload, wayDao, splitWayDB)
    }

    @Test fun `catches ElementConflict exception`() {
        on(splitWayDB.getAll()).thenReturn(listOf(mock(OsmQuestSplitWay::class.java)))
        on(splitSingleOsmWayUpload.upload(anyLong(), any(), anyList()))
            .thenThrow(ElementConflictException())

        uploader.upload(CancellationSignal())

        // will not throw ElementConflictException
    }

    @Test fun `discard if element was deleted`() {
        on(splitWayDB.getAll()).thenReturn(listOf(mock(OsmQuestSplitWay::class.java)))
        on(wayDao.get(anyLong())).thenReturn(null)

        uploader.uploadedChangeListener = mock(OnUploadedChangeListener::class.java)
        uploader.upload(CancellationSignal())

        verify(uploader.uploadedChangeListener)?.onDiscarded()
    }

    @Test fun `catches ChangesetConflictException exception and tries again once`() {
        on(splitWayDB.getAll()).thenReturn(listOf(mock(OsmQuestSplitWay::class.java)))
        on(splitSingleOsmWayUpload.upload(anyLong(), any(), anyList()))
            .thenThrow(ChangesetConflictException())
            .thenReturn(listOf())

        uploader.upload(CancellationSignal())

        // will not throw ChangesetConflictException but instead call single upload twice
        verify(changesetManager).getOrCreateChangeset(any(), any())
        verify(changesetManager).createChangeset(any(), any())
        verify(splitSingleOsmWayUpload, times(2)).upload(anyLong(), any(), anyList())
    }

    @Test fun `delete each uploaded split from local DB and calls listener`() {
        on(splitWayDB.getAll()).thenReturn(listOf(
            mock(OsmQuestSplitWay::class.java),
            mock(OsmQuestSplitWay::class.java)
        ))
        on(splitSingleOsmWayUpload.upload(anyLong(), any(), anyList()))
            .thenThrow(ElementConflictException())
            .thenReturn(listOf())

        uploader.uploadedChangeListener = mock(OnUploadedChangeListener::class.java)
        uploader.upload(CancellationSignal())

        verify(splitWayDB, times(2)).delete(0L)
        verify(uploader.uploadedChangeListener)?.onUploaded()
        verify(uploader.uploadedChangeListener)?.onDiscarded()
    }
}
