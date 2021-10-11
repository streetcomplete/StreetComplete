package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class QuestTypeOrderDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: QuestTypeOrderDao

    @Before fun createDao() {
        dao = QuestTypeOrderDao(database)
    }

    @Test fun getEmpty() {
        assertTrue(dao.getAll(0).isEmpty())
        assertTrue(dao.getAll(1).isEmpty())
    }

    @Test fun putGetOne() {
        dao.put(0, "a" to "b")
        assertEquals(listOf("a" to "b"), dao.getAll(0))
        assertTrue(dao.getAll(1).isEmpty())
    }

    @Test fun putGetSeveral() {
        dao.put(1, "a" to "b")
        dao.put(1, "d" to "e")
        dao.put(1, "x" to "y")
        assertEquals(listOf("a" to "b", "d" to "e", "x" to "y"), dao.getAll(1))
        assertTrue(dao.getAll(0).isEmpty())
    }

    @Test fun clear() {
        dao.put(0, "x" to "y")
        dao.put(1, "a" to "b")
        dao.clear(1)
        assertTrue(dao.getAll(0).isNotEmpty())
        assertTrue(dao.getAll(1).isEmpty())
    }
}
