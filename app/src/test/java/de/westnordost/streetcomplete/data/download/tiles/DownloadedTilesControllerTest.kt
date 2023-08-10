package de.westnordost.streetcomplete.data.download.tiles

import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.verify

class DownloadedTilesControllerTest {

    private lateinit var dao: DownloadedTilesDao
    private lateinit var listener: DownloadedTilesSource.Listener
    private lateinit var ctrl: DownloadedTilesController

    @Before fun setUp() {
        dao = mock()
        listener = mock()
        ctrl = DownloadedTilesController(dao)
        ctrl.addListener(listener)
    }

    @Test fun put() {
        val r = TilesRect(0, 0, 1, 1)
        ctrl.put(r)
        verify(dao).put(r)
        verify(listener).onUpdated()
    }

    @Test fun clear() {
        ctrl.clear()
        verify(dao).deleteAll()
        verify(listener).onUpdated()
    }

    @Test fun invalidate() {
        val t = TilePos(0, 0)
        ctrl.invalidate(t)
        verify(dao).updateTimeNewerThan(eq(t), anyLong()) // hm, difficult to test the exact time...
        verify(listener).onUpdated()
    }

    @Test fun invalidateAll() {
        ctrl.invalidateAll()
        verify(dao).updateAllTimesNewerThan(anyLong()) // hm, difficult to test the exact time...
        verify(listener).onUpdated()
    }

    @Test fun deleteOlderThan() {
        ctrl.deleteOlderThan(123L)
        verify(dao).deleteOlderThan(123L)
        verify(listener).onUpdated()
    }

    @Test fun getAll() {
        val r = listOf(TilePos(0, 1))
        on(dao.getAll(123)).thenReturn(r)
        assertEquals(r, ctrl.getAll(123))
    }

    @Test fun contains() {
        val r = TilesRect(0, 0, 1, 1)
        on(dao.contains(r, 123L)).thenReturn(true)
        assertTrue(ctrl.contains(r, 123L))
    }
}
