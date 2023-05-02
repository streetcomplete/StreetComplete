package de.westnordost.streetcomplete.data.download.tiles

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DownloadedTilesDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: DownloadedTilesDao

    @Before fun createDao() {
        dao = DownloadedTilesDao(database)
    }

    @Test fun putGetOne() {
        dao.put(r(5, 8, 5, 8))

        assertTrue(dao.contains(r(5, 8, 5, 8), 0))

        assertEquals(
            listOf(TilePos(5, 8)),
            dao.getAll(0)
        )
    }

    @Test fun putGetOld() {
        dao.put(r(5, 8, 5, 8))
        val then = nowAsEpochMilliseconds() + 1000
        assertFalse(dao.contains(r(5, 8, 5, 8), then))
        assertTrue(dao.getAll(then).isEmpty())
    }

    @Test fun putSomeOld() {
        dao.put(r(0, 0, 1, 3))
        Thread.sleep(2000)
        dao.put(r(2, 0, 5, 5))
        val before = nowAsEpochMilliseconds() - 1000
        assertFalse(dao.contains(r(0, 0, 2, 2), before))
        assertEquals(24, dao.getAll(before).size)
    }

    @Test fun putMoreGetOne() {
        dao.put(r(5, 8, 6, 10))
        assertTrue(dao.contains(r(5, 8, 5, 8), 0))
        assertTrue(dao.contains(r(6, 10, 6, 10), 0))
        assertEquals(6, dao.getAll(0).size)
    }

    @Test fun putOneGetMore() {
        dao.put(r(5, 8, 5, 8))
        assertFalse(dao.contains(r(5, 8, 5, 9), 0))
    }

    @Test fun remove() {
        dao.put(r(0, 0, 3, 3))
        assertEquals(1, dao.delete(TilePos(0, 0)))
    }

    @Test fun putRemoveAll() {
        dao.put(r(0, 0, 3, 3))
        dao.deleteAll()
        assertTrue(dao.getAll(0).isEmpty())
    }

    @Test fun updateTime() {
        dao.put(r(0, 0, 0, 1))
        dao.updateTime(TilePos(0, 0), 0)
        assertEquals(
            listOf(TilePos(0, 1)),
            dao.getAll(1)
        )
    }

    @Test fun updateAllTimes() {
        dao.put(r(0, 0, 0, 1))
        dao.updateAllTimes(0)
        assertTrue(dao.getAll(1).isEmpty())
    }

    @Test fun deleteOlderThan() {
        dao.put(r(0, 0, 1, 0))
        dao.updateTime(TilePos(0, 0), 1)
        assertEquals(1, dao.deleteOlderThan(2))
    }

    private fun r(left: Int, top: Int, right: Int, bottom: Int) = TilesRect(left, top, right, bottom)
}
