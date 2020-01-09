package de.westnordost.streetcomplete.data.user

import org.junit.Before
import org.junit.Test

import de.westnordost.streetcomplete.data.ApplicationDbTestCase

import org.junit.Assert.*

class QuestStatisticsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: QuestStatisticsDao

    @Before fun createDao() {
        dao = QuestStatisticsDao(dbHelper)
    }

    @Test fun getZero() {
        assertEquals(0, dao.getAmount(ONE))
    }

    @Test fun getOne() {
        dao.addOne(ONE)
        assertEquals(1, dao.getAmount(ONE))
    }

    @Test fun getTwo() {
        dao.addOne(ONE)
        dao.addOne(ONE)
        assertEquals(2, dao.getAmount(ONE))
    }

    @Test fun getTotal() {
        dao.addOne(ONE)
        dao.addOne(ONE)
        dao.addOne(TWO)
        assertEquals(3, dao.getTotalAmount())
    }

    @Test fun replaceAll() {
        dao.addOne(ONE)
        dao.addOne(TWO)
        dao.replaceAll(mapOf(
                ONE to 4,
                THREE to 1
        ))
        assertEquals(4, dao.getAmount(ONE))
        assertEquals(0, dao.getAmount(TWO))
        assertEquals(1, dao.getAmount(THREE))
    }

    @Test fun getAll() {
        dao.addOne(ONE)
        dao.addOne(ONE)
        dao.addOne(TWO)
        assertEquals(mapOf(
            ONE to 2,
            TWO to 1
        ),dao.getAll())
    }
}

private const val ONE = "one"
private const val TWO = "two"
private const val THREE = "three"
