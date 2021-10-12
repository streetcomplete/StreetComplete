package de.westnordost.streetcomplete.data.notifications

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.user.achievements.NewUserAchievementsDao
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NewUserAchievementsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: NewUserAchievementsDao

    @Before fun createDao() {
        dao = NewUserAchievementsDao(database)
    }

    @Test fun addPopFirst() {
        dao.push(TWO to 2)
        dao.push(ONE to 1)
        dao.push(TWO to 1)
        dao.push(ONE to 8)

        assertEquals(ONE to 1, dao.pop())
        assertEquals(ONE to 8, dao.pop())
        assertEquals(TWO to 1, dao.pop())
        assertEquals(TWO to 2, dao.pop())
        assertEquals(null, dao.pop())
    }

    @Test fun addPop() {
        assertEquals(0, dao.getCount())

        dao.push(ONE to 4)
        assertEquals(1, dao.getCount())

        dao.push(ONE to 4)
        assertEquals(1, dao.getCount())

        dao.push(ONE to 1)
        assertEquals(2, dao.getCount())

        dao.pop()
        assertEquals(1, dao.getCount())

        dao.pop()
        assertEquals(0, dao.getCount())
    }
}

private const val ONE = "one"
private const val TWO = "two"
