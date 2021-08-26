package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class QuestPresetDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: QuestPresetsDao

    @Before fun createDao() {
        dao = QuestPresetsDao(database)
    }

    @Test fun getEmpty() {
        assertTrue(dao.getAll().isEmpty())
        assertNull(dao.getName(123))
    }

    @Test fun addGetDelete() {
        dao.add("test")
        assertEquals(listOf(QuestPreset(1, "test")), dao.getAll())
        assertEquals("test", dao.getName(1))
        dao.delete(1)
        assertTrue(dao.getAll().isEmpty())
    }

    @Test fun addTwo() {
        dao.add("one")
        dao.add("two")
        assertEquals(listOf(
            QuestPreset(1, "one"),
            QuestPreset(2, "two")
        ), dao.getAll())
    }
}
