package de.westnordost.streetcomplete.data.user

import org.junit.Before
import org.junit.Test

import de.westnordost.streetcomplete.data.ApplicationDbTestCase

import org.junit.Assert.*
import org.mockito.Mockito.*

class QuestStatisticsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: QuestStatisticsDao

    @Before fun createDao() {
        dao = QuestStatisticsDao(dbHelper)
    }

    @Test fun getZero() {
        assertEquals(0, dao.getAmount(ONE))
    }

    @Test fun getOne() {
        val listener = mock(QuestStatisticsDao.Listener::class.java)
        dao.addListener(listener)
        dao.addOne(ONE)
        assertEquals(1, dao.getAmount(ONE))
        verify(listener).onAddedOne(ONE)
    }

    @Test fun getTwo() {
        val listener = mock(QuestStatisticsDao.Listener::class.java)
        dao.addListener(listener)
        dao.addOne(ONE)
        dao.addOne(ONE)
        assertEquals(2, dao.getAmount(ONE))
        verify(listener, times(2)).onAddedOne(ONE)
    }

    @Test fun getTotal() {
        dao.addOne(ONE)
        dao.addOne(ONE)
        dao.addOne(TWO)
        assertEquals(3, dao.getTotalAmount())
    }

    @Test fun subtract() {
        val listener = mock(QuestStatisticsDao.Listener::class.java)
        dao.addListener(listener)
        dao.addOne(ONE)
        verify(listener).onAddedOne(ONE)
        dao.subtractOne(ONE)
        verify(listener).onSubtractedOne(ONE)
        assertEquals(0, dao.getAmount(ONE))
    }

    @Test fun getAmountOfSeveral() {
        dao.addOne(ONE)
        dao.addOne(ONE)
        dao.addOne(TWO)
        dao.addOne(THREE)
        assertEquals(3, dao.getAmount(listOf(ONE, TWO)))
    }

    @Test fun replaceAll() {
        dao.addOne(ONE)
        dao.addOne(TWO)
        val listener = mock(QuestStatisticsDao.Listener::class.java)
        dao.addListener(listener)
        dao.replaceAll(mapOf(
                ONE to 4,
                THREE to 1
        ))
        verify(listener).onReplacedAll()
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
