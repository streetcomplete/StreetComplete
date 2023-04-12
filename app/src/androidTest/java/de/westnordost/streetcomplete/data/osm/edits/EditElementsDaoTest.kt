package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EditElementsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: EditElementsDao

    @Before fun createDao() {
        dao = EditElementsDao(database)
    }

    @Test fun get_delete_empty() {
        // nothing to delete
        assertEquals(0, dao.delete(0))
        assertEquals(0, dao.deleteAll(listOf(0, 1, 2)))
        // nothing to get
        assertTrue(dao.getAll(ElementType.NODE, 0).isEmpty())
    }

    @Test fun addGetDelete() {
        // add...
        dao.add(9, listOf(
            ElementKey(ElementType.NODE, 0),
            ElementKey(ElementType.NODE, 0),  // duplicate ignored
        ))

        dao.add(7, listOf(
            ElementKey(ElementType.NODE, 0),  // referring to same element
            ElementKey(ElementType.WAY, 1),  // but also another
        ))

        dao.add(3, listOf(
            ElementKey(ElementType.WAY, 2),
        ))

        // get...
        assertTrue(dao.getAll(ElementType.NODE, 0).containsExactlyInAnyOrder(listOf(9, 7)))
        assertTrue(dao.getAll(ElementType.WAY, 1).containsExactlyInAnyOrder(listOf(7)))
        assertTrue(dao.getAll(ElementType.WAY, 3).containsExactlyInAnyOrder(listOf(3)))

        assertTrue(dao.getAll(ElementType.WAY, 0).isEmpty())
        assertTrue(dao.getAll(ElementType.NODE, 1).isEmpty())

        // delete
        assertEquals(1, dao.delete(9))
        assertTrue(dao.getAll(ElementType.NODE, 0).containsExactlyInAnyOrder(listOf(7)))

        assertEquals(3, dao.deleteAll(listOf(7,3)))
        assertTrue(dao.getAll(ElementType.NODE, 0).isEmpty())
        assertTrue(dao.getAll(ElementType.WAY, 1).isEmpty())
        assertTrue(dao.getAll(ElementType.WAY, 2).isEmpty())
    }
}
