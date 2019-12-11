package de.westnordost.streetcomplete.data.osm.download

import de.westnordost.osmapi.overpass.MapDataWithGeometryParser
import de.westnordost.osmapi.overpass.OsmTooManyRequestsException
import de.westnordost.osmapi.overpass.OverpassMapDataDao
import de.westnordost.osmapi.overpass.OverpassStatus
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Assert.assertFalse
import org.junit.Test
import org.mockito.Mockito.*
import javax.inject.Provider
import kotlin.concurrent.thread

class OverpassMapDataAndGeometryDaoTest {
    
    @Test fun handleOverpassQuota() {
        val provider = mock<Provider<MapDataWithGeometryParser>>()
        on(provider.get()).thenReturn(mock())

        val status = OverpassStatus()
        status.availableSlots = 0
        status.nextAvailableSlotIn = 1
        status.maxAvailableSlots = 2

        val overpass = mock<OverpassMapDataDao>()
        on(overpass.getStatus()).thenReturn(status)
        doThrow(OsmTooManyRequestsException::class.java).on(overpass).queryElementsWithGeometry(any(), any())
        val dao = OverpassMapDataAndGeometryDao(overpass, mock())
        // the dao will call get(), get an exception in return, ask its status
        // then and at least wait for the specified amount of time before calling again
        var result = false
        val dlThread = thread {
            result = dao.query("") { _, _ -> Unit }
        }
        // sleep the wait time: Downloader should not try to call
        // overpass again in this time
        Thread.sleep(status.nextAvailableSlotIn!! * 1000L)
        verify(overpass, times(1)).getStatus()
        verify(overpass, times(1)).queryElementsWithGeometry(any(), any())
        // now we test if dao will call overpass again after that time. It is not really
        // defined when the downloader must call overpass again, lets assume 1.5 secs here and
        // change it when it fails
        Thread.sleep(1500)
        verify(overpass, times(2)).getStatus()
        verify(overpass, times(2)).queryElementsWithGeometry(any(), any())
        // we are done here, interrupt thread (still part of the test though...)
        dlThread.interrupt()
        dlThread.join()
        assertFalse(result)
    }
}
