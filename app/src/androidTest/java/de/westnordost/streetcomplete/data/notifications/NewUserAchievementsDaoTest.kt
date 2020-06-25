package de.westnordost.streetcomplete.data.notifications

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class NewUserAchievementsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: NewUserAchievementsDao

    @Before fun createDao() {
        dao = NewUserAchievementsDao(dbHelper)
    }

    @Test fun addPopFirst() {
        val listener: NewUserAchievementsDao.UpdateListener = mock(NewUserAchievementsDao.UpdateListener::class.java)
        dao.addListener(listener)
        dao.push(TWO to 2)
        dao.push(ONE to 1)
        dao.push(TWO to 1)
        dao.push(ONE to 8)

        assertEquals(ONE to 1, dao.pop())
        assertEquals(ONE to 8, dao.pop())
        assertEquals(TWO to 1, dao.pop())
        assertEquals(TWO to 2, dao.pop())
        assertEquals(null, dao.pop())

        verify(listener, times(8)).onNewUserAchievementsUpdated()
    }

    @Test fun addPop() {
        val listener: NewUserAchievementsDao.UpdateListener = mock(NewUserAchievementsDao.UpdateListener::class.java)
        dao.addListener(listener)

        assertEquals(0, dao.getCount())

        dao.push(ONE to 4)
        assertEquals(1, dao.getCount())
        verify(listener, times(1)).onNewUserAchievementsUpdated()

        dao.push(ONE to 4)
        assertEquals(1, dao.getCount())
        verify(listener, times(1)).onNewUserAchievementsUpdated()

        dao.push(ONE to 1)
        assertEquals(2, dao.getCount())
        verify(listener, times(2)).onNewUserAchievementsUpdated()


        dao.pop()
        assertEquals(1, dao.getCount())
        verify(listener, times(3)).onNewUserAchievementsUpdated()

        dao.pop()
        assertEquals(0, dao.getCount())
        verify(listener, times(4)).onNewUserAchievementsUpdated()
    }
}

private const val ONE = "one"
private const val TWO = "two"
