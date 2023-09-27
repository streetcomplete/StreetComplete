package de.westnordost.streetcomplete.data.osm.created_elements

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreatedElementsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: CreatedElementsDao

    @Before fun createDao() {
        dao = CreatedElementsDao(database)
    }

    @Test fun putGetDelete() {
        assertTrue(dao.getAll().isEmpty())

        val elements = listOf(
            ElementKey(ElementType.NODE, 1),
            ElementKey(ElementType.WAY, 1),
            ElementKey(ElementType.NODE, 3),
        )

        dao.putAll(elements)

        assertEquals(elements, dao.getAll())

        dao.deleteAll(listOf(
            ElementKey(ElementType.WAY, 1),
            ElementKey(ElementType.NODE, 3),
        ))

        assertEquals(listOf(ElementKey(ElementType.NODE, 1)), dao.getAll())
    }

    @Test fun clear() {
        dao.putAll(listOf(ElementKey(ElementType.NODE, 1)))
        dao.clear()
        assertTrue(dao.getAll().isEmpty())
    }
}
