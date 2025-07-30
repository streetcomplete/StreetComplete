package de.westnordost.streetcomplete.data.presets

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EditTypePresetsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: EditTypePresetsDao

    @BeforeTest fun createDao() {
        dao = EditTypePresetsDao(database)
    }

    @Test fun getEmpty() {
        assertTrue(dao.getAll().isEmpty())
        assertNull(dao.getName(123))
    }

    @Test fun addGetDelete() {
        dao.add("test")
        assertEquals(listOf(EditTypePreset(1, "test")), dao.getAll())
        assertEquals("test", dao.getName(1))
        dao.delete(1)
        assertTrue(dao.getAll().isEmpty())
    }

    @Test fun addTwo() {
        dao.add("one")
        dao.add("two")
        assertEquals(listOf(
            EditTypePreset(1, "one"),
            EditTypePreset(2, "two")
        ), dao.getAll())
    }

    @Test fun rename() {
        val id = dao.add("one")
        assertEquals("one", dao.getName(id))
        dao.rename(id, "two")
        assertEquals("two", dao.getName(id))
    }
}
