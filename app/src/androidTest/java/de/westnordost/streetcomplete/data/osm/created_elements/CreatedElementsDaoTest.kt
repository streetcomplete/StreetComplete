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
            CreatedElementKey(ElementType.NODE, 1, null),
            CreatedElementKey(ElementType.WAY, 1, 123),
            CreatedElementKey(ElementType.NODE, 3, 456),
        )

        dao.putAll(elements)

        assertEquals(elements, dao.getAll())

        dao.deleteAll(listOf(
            ElementKey(ElementType.WAY, 1),
            ElementKey(ElementType.NODE, 456),
        ))

        assertEquals(listOf(CreatedElementKey(ElementType.NODE, 1, null)), dao.getAll())
    }

    @Test fun putReplaces() {
        dao.putAll(listOf(
            CreatedElementKey(ElementType.NODE, 1, null)
        ))
        dao.putAll(listOf(
            CreatedElementKey(ElementType.NODE, 1, 123)
        ))
        assertEquals(
            listOf(CreatedElementKey(ElementType.NODE, 1, 123)),
            dao.getAll()
        )
    }

    @Test fun clear() {
        dao.putAll(listOf(CreatedElementKey(ElementType.NODE, 1, 123)))
        dao.clear()
        assertTrue(dao.getAll().isEmpty())
    }
}
