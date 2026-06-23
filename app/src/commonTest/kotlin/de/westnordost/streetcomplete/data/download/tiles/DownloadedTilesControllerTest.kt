package de.westnordost.streetcomplete.data.download.tiles

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DownloadedTilesControllerTest {

    private lateinit var dao: DownloadedTilesDao
    private lateinit var listener: DownloadedTilesSource.Listener
    private lateinit var ctrl: DownloadedTilesController

    @BeforeTest fun setUp() {
        dao = mock()
        listener = mock()
        ctrl = DownloadedTilesController(dao)
        ctrl.addListener(listener)
    }

    @Test fun put() {
        val r = TilesRect(0, 0, 1, 1)
        ctrl.put(r)
        verify { dao.put(r) }
        verify { listener.onUpdated() }
    }

    @Test fun clear() {
        ctrl.clear()
        verify { dao.deleteAll() }
        verify { listener.onUpdated() }
    }

    @Test fun invalidate() {
        val t = TilePos(0, 0)
        ctrl.invalidate(t)
        verify { dao.updateTimeNewerThan(t, any()) } // hm, difficult to test the exact time...
        verify { listener.onUpdated() }
    }

    @Test fun invalidateAll() {
        ctrl.invalidateAll()
        verify { dao.updateAllTimesNewerThan(any()) } // hm, difficult to test the exact time...
        verify { listener.onUpdated() }
    }

    @Test fun deleteOlderThan() {
        ctrl.deleteOlderThan(123L)
        verify { dao.deleteOlderThan(123L) }
        verify { listener.onUpdated() }
    }

    @Test fun getAll() {
        val r = listOf(TilePos(0, 1))
        every { dao.getAll(123) } returns r
        assertEquals(r, ctrl.getAll(123))
    }

    @Test fun contains() {
        val r = TilesRect(0, 0, 1, 1)
        every { dao.contains(r, 123L) } returns true
        assertTrue(ctrl.contains(r, 123L))
    }
}
