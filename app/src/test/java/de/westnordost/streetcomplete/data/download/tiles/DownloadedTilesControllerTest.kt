package de.westnordost.streetcomplete.data.download.tiles

import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.eq
import io.mockative.every
import io.mockative.mock

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DownloadedTilesControllerTest {

    // @Mock
    // val dao = mock(classOf<DownloadedTilesDao>())
    // @Mock val listener = mock(classOf<DownloadedTilesSource.Listener>())
    @Mock private lateinit var dao: DownloadedTilesDao
    @Mock private lateinit var listener: DownloadedTilesSource.Listener
    @Mock private lateinit var ctrl: DownloadedTilesController

    @BeforeTest fun setUp() {
        dao = mock(classOf<DownloadedTilesDao>())
        listener = mock(classOf<DownloadedTilesSource.Listener>())
        ctrl = DownloadedTilesController(dao)
        ctrl.addListener(listener)
    }

    @Test fun put() {
        val r = TilesRect(0, 0, 1, 1)
        ctrl.put(r)
        verifyInvokedExactlyOnce { dao.put(r) }
        verifyInvokedExactlyOnce { listener.onUpdated() }
    }

    @Test fun clear() {
        ctrl.clear()
        verifyInvokedExactlyOnce { dao.deleteAll() }
        verifyInvokedExactlyOnce { listener.onUpdated() }
    }

    @Test fun invalidate() {
        val t = TilePos(0, 0)
        ctrl.invalidate(t)
        verifyInvokedExactlyOnce { dao.updateTimeNewerThan(eq(t), any()) } // hm, difficult to test the exact time...
        verifyInvokedExactlyOnce { listener.onUpdated() }
    }

    @Test fun invalidateAll() {
        ctrl.invalidateAll()
        verifyInvokedExactlyOnce { dao.updateAllTimesNewerThan(any()) } // hm, difficult to test the exact time...
        verifyInvokedExactlyOnce { listener.onUpdated() }
    }

    @Test fun deleteOlderThan() {
        every { dao.deleteOlderThan(123) }.returns(1)
        ctrl.deleteOlderThan(123L)
        verifyInvokedExactlyOnce { dao.deleteOlderThan(123L) }
        verifyInvokedExactlyOnce { listener.onUpdated() }
    }

    @Test fun getAll() {
        val r = listOf(TilePos(0, 1))
        every { dao.getAll(123) }.returns(r)
        assertEquals(r, ctrl.getAll(123))
    }

    @Test fun contains() {
        val r = TilesRect(0, 0, 1, 1)
        every { dao.contains(r, 123L) }.returns(true)
        assertTrue(ctrl.contains(r, 123L))
    }
}
