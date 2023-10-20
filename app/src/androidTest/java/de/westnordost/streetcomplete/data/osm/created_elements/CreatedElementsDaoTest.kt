package de.westnordost.streetcomplete.data.osm.created_elements

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreatedElementsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: CreatedElementsDao

    @BeforeTest fun createDao() {
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
