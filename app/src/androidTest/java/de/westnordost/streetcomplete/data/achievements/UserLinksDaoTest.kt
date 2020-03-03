package de.westnordost.streetcomplete.data.achievements

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
}

private const val ONE = "one"
private const val TWO = "two"
