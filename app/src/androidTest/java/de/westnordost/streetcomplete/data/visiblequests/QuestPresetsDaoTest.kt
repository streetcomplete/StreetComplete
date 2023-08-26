package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuestPresetsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: QuestPresetsDao

    @BeforeTest fun createDao() {
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

    @Test fun rename() {
        val id = dao.add("one")
        assertEquals("one", dao.getName(id))
        dao.rename(id, "two")
        assertEquals("two", dao.getName(id))
    }
}
