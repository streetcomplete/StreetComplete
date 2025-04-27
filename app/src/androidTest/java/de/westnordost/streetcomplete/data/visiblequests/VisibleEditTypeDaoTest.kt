package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VisibleEditTypeDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: VisibleEditTypeDao

    @BeforeTest fun createDao() {
        dao = VisibleEditTypeDao(database)
    }

    @Test fun defaultEnabledEditType() {
        assertTrue(dao.get(0, "something"))
        assertTrue(dao.get(1, "something"))
    }

    @Test fun disableEditType() {
        dao.put(0, "no", false)
        dao.put(1, "blob", false)
        assertFalse(dao.get(0, "no"))
        assertTrue(dao.get(1, "no"))
        assertFalse(dao.get(1, "blob"))
    }

    @Test fun enableEditType() {
        dao.put(0, "no", false)
        dao.put(0, "no", true)
        assertTrue(dao.get(0, "no"))
    }

    @Test fun reset() {
        dao.put(0, "blurb", false)
        dao.put(1, "blurb", false)
        dao.put(1, "blarb", false)
        assertFalse(dao.get(0, "blurb"))
        assertFalse(dao.get(1, "blurb"))
        assertFalse(dao.get(1, "blarb"))

        dao.clear(0, listOf("blurb"))
        assertTrue(dao.get(0, "blurb"))
        assertFalse(dao.get(1, "blurb"))
        assertFalse(dao.get(1, "blarb"))

        dao.clear(1, listOf("blurb", "blarb"))
        assertTrue(dao.get(1, "blurb"))
        assertTrue(dao.get(1, "blarb"))
    }

    @Test fun putAllAndGetAll() {
        val visibilities = mapOf("a" to true, "b" to false)
        dao.putAll(0, visibilities)
        assertTrue(dao.get(0, "a"))
        assertFalse(dao.get(0, "b"))
        assertEquals(visibilities, dao.getAll(0))
        assertEquals(mapOf(), dao.getAll(1))
    }
}
