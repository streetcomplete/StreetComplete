package de.westnordost.streetcomplete.data.statistics

import de.westnordost.osmapi.changesets.ChangesetsDao
import org.junit.Before
import org.junit.Test

import de.westnordost.streetcomplete.data.ApplicationDbTestCase

import org.junit.Assert.*
import org.mockito.Mockito.mock

class QuestStatisticsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: QuestStatisticsDao

    @Before fun createDao() {
        dao = QuestStatisticsDao(dbHelper, UserChangesetsDao(mock(ChangesetsDao::class.java)))
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
}

private const val ONE = "one"
private const val TWO = "two"
