package de.westnordost.streetcomplete.data.user.achievements

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserLinksDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: UserLinksDao

    @BeforeTest fun createDao() {
        dao = UserLinksDao(database)
    }

    @Test fun putGetAll() {
        dao.add(ONE)
        dao.add(ONE)
        dao.add(TWO)
        assertEquals(listOf(ONE, TWO), dao.getAll())
    }

    @Test fun addAll() {
        dao.add(ONE)
        dao.addAll(listOf(ONE, TWO, THREE))
        assertEquals(listOf(ONE, TWO, THREE), dao.getAll())
    }
}

private const val ONE = "one"
private const val TWO = "two"
private const val THREE = "three"
