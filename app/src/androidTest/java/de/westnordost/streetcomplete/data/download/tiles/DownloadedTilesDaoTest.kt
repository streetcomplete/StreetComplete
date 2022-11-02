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
        dao.put(r(5, 8, 5, 8), "Huhu")
        val huhus = dao.get(r(5, 8, 5, 8), 0)

        assertEquals(1, huhus.size)
        assertTrue(huhus.contains("Huhu"))
    }

    @Test fun putGetOld() {
        dao.put(r(5, 8, 5, 8), "Huhu")
        val huhus = dao.get(r(5, 8, 5, 8), nowAsEpochMilliseconds() + 1000)
        assertTrue(huhus.isEmpty())
    }

    @Test fun putSomeOld() {
        dao.put(r(0, 0, 1, 3), "Huhu")
        Thread.sleep(2000)
        dao.put(r(1, 3, 5, 5), "Huhu")
        val huhus = dao.get(r(0, 0, 2, 2), nowAsEpochMilliseconds() - 1000)
        assertTrue(huhus.isEmpty())
    }

    @Test fun putMoreGetOne() {
        dao.put(r(5, 8, 6, 10), "Huhu")
        assertFalse(dao.get(r(5, 8, 5, 8), 0).isEmpty())
        assertFalse(dao.get(r(6, 10, 6, 10), 0).isEmpty())
    }

    @Test fun putOneGetMore() {
        dao.put(r(5, 8, 5, 8), "Huhu")
        assertTrue(dao.get(r(5, 8, 5, 9), 0).isEmpty())
    }

    @Test fun remove() {
        dao.put(r(0, 0, 3, 3), "Huhu")
        dao.put(r(0, 0, 0, 0), "Haha")
        dao.put(r(1, 1, 3, 3), "Hihi")
        assertEquals(2, dao.remove(TilePos(0, 0))) // removes huhu, haha at 0,0
    }

    @Test fun putSeveralQuestTypes() {
        dao.put(r(0, 0, 5, 5), "Huhu")
        dao.put(r(4, 4, 6, 6), "hoho")
        dao.put(r(4, 0, 4, 7), "hihi")

        var check = dao.get(r(0, 0, 2, 2), 0)
        assertEquals(1, check.size)
        assertTrue(check.contains("Huhu"))

        check = dao.get(r(4, 4, 4, 4), 0)
        assertEquals(3, check.size)

        check = dao.get(r(5, 5, 5, 5), 0)
        assertEquals(2, check.size)
        assertTrue(check.contains("hoho"))
        assertTrue(check.contains("Huhu"))

        check = dao.get(r(0, 0, 6, 6), 0)
        assertTrue(check.isEmpty())
    }

    private fun r(left: Int, top: Int, right: Int, bottom: Int) = TilesRect(left, top, right, bottom)
}
