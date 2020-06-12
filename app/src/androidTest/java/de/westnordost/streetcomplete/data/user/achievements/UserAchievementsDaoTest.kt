package de.westnordost.streetcomplete.data.user.achievements

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserAchievementsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: UserAchievementsDao

    @Before fun createDao() {
        dao = UserAchievementsDao(dbHelper)
    }

    @Test fun putGetAll() {
        dao.put(ONE, 1)
        dao.put(ONE, 4)
        dao.put(TWO, 2)
        assertEquals(mapOf(
            ONE to 4,
            TWO to 2
        ),dao.getAll())
    }
}

private const val ONE = "one"
private const val TWO = "two"
