package de.westnordost.streetcomplete.data.user.achievements

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.StreetCompleteDatabaseTestCase
import kotlin.test.Test
import kotlin.test.assertEquals

class UserAchievementsDaoTest : StreetCompleteDatabaseTestCase() {
    private lateinit var dao: UserAchievementsDao

    override fun onDatabaseInitialized(database: Database) {
        dao = UserAchievementsDao(database)
    }

    @Test fun putGetAll() {
        dao.putAll(listOf(ONE to 1))
        dao.putAll(listOf(ONE to 4, TWO to 2))
        assertEquals(mapOf(
            ONE to 4,
            TWO to 2
        ), dao.getAll())
    }

    @Test fun putSingle() {
        dao.put(ONE, 1)
        dao.put(ONE, 4)
        assertEquals(mapOf(ONE to 4), dao.getAll())
    }

    @kotlin.test.AfterTest
    override fun tearDownDatabase() = super.tearDownDatabase()
}

private const val ONE = "one"
private const val TWO = "two"
