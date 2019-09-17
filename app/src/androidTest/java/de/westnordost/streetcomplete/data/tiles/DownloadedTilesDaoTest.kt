package de.westnordost.streetcomplete.data.tiles

import android.graphics.Point
import android.graphics.Rect

import org.junit.Before
import org.junit.Test

import de.westnordost.streetcomplete.data.ApplicationDbTestCase

import org.junit.Assert.*

class DownloadedTilesDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: DownloadedTilesDao

    @Before fun createDao() {
        dao = DownloadedTilesDao(dbHelper)
    }

    @Test fun putGetOne() {
        dao.put(Rect(5, 8, 5, 8), "Huhu")
        val huhus = dao.get(Rect(5, 8, 5, 8), 0)

        assertEquals(1, huhus.size)
        assertTrue(huhus.contains("Huhu"))
    }

    @Test fun putGetOld() {
        dao.put(Rect(5, 8, 5, 8), "Huhu")
        val huhus = dao.get(Rect(5, 8, 5, 8), System.currentTimeMillis() + 1000)
        assertTrue(huhus.isEmpty())
    }

    @Test fun putSomeOld() {
        dao.put(Rect(0, 0, 1, 3), "Huhu")
        Thread.sleep(2000)
        dao.put(Rect(1, 3, 5, 5), "Huhu")
        val huhus = dao.get(Rect(0, 0, 2, 2), System.currentTimeMillis() - 1000)
        assertTrue(huhus.isEmpty())
    }

    @Test fun putMoreGetOne() {
        dao.put(Rect(5, 8, 6, 10), "Huhu")
        assertFalse(dao.get(Rect(5, 8, 5, 8), 0).isEmpty())
        assertFalse(dao.get(Rect(6, 10, 6, 10), 0).isEmpty())
    }

    @Test fun putOneGetMore() {
        dao.put(Rect(5, 8, 5, 8), "Huhu")
        assertTrue(dao.get(Rect(5, 8, 5, 9), 0).isEmpty())
    }

    @Test fun remove() {
        dao.put(Rect(0, 0, 3, 3), "Huhu")
        dao.put(Rect(0, 0, 0, 0), "Haha")
        dao.put(Rect(1, 1, 3, 3), "Hihi")
        assertEquals(2, dao.remove(Point(0, 0))) // removes huhu, haha at 0,0
    }

    @Test fun putSeveralQuestTypes() {
        dao.put(Rect(0, 0, 5, 5), "Huhu")
        dao.put(Rect(4, 4, 6, 6), "hoho")
        dao.put(Rect(4, 0, 4, 7), "hihi")

        var check = dao.get(Rect(0, 0, 2, 2), 0)
        assertEquals(1, check.size)
        assertTrue(check.contains("Huhu"))

        check = dao.get(Rect(4, 4, 4, 4), 0)
        assertEquals(3, check.size)

        check = dao.get(Rect(5, 5, 5, 5), 0)
        assertEquals(2, check.size)
        assertTrue(check.contains("hoho"))
        assertTrue(check.contains("Huhu"))

        check = dao.get(Rect(0, 0, 6, 6), 0)
        assertTrue(check.isEmpty())
    }
}
