package de.westnordost.streetcomplete.data.user.achievements

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserLinksDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: UserLinksDao

    @Before fun createDao() {
        dao = UserLinksDao(dbHelper)
    }

    @Test fun putGetAll() {
        dao.add(ONE)
        dao.add(ONE)
        dao.add(TWO)
        assertEquals(listOf(ONE, TWO), dao.getAll())
    }

    @Test fun addAll() {
        dao.add(ONE)
        assertEquals(2, dao.addAll(listOf(ONE, TWO, THREE)))
    }
}

private const val ONE = "one"
private const val TWO = "two"
private const val THREE = "three"
