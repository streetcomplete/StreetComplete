package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import kotlin.test.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class EditElementsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: EditElementsDao

    @BeforeTest fun createDao() {
        dao = EditElementsDao(database)
    }

    @Test fun get_delete_empty() {
        // nothing to delete
        assertEquals(0, dao.delete(0))
        assertEquals(0, dao.deleteAll(listOf(0, 1, 2)))
        // nothing to get
        assertTrue(dao.getAllByElement(ElementType.NODE, 0).isEmpty())
        assertTrue(dao.get(0).isEmpty())
    }

    @Test fun addGetDelete() {
        // add...
        dao.put(9, listOf(
            ElementKey(ElementType.NODE, 0),
            ElementKey(ElementType.NODE, 0),  // duplicate ignored
        ))

        dao.put(7, listOf(
            ElementKey(ElementType.NODE, 0),  // referring to same element
            ElementKey(ElementType.WAY, 1),  // but also another
        ))

        dao.put(3, listOf(
            ElementKey(ElementType.WAY, 2),
        ))

        // get...
        assertTrue(dao.getAllByElement(ElementType.NODE, 0).containsExactlyInAnyOrder(listOf(9, 7)))
        assertTrue(dao.getAllByElement(ElementType.WAY, 1).containsExactlyInAnyOrder(listOf(7)))
        assertTrue(dao.getAllByElement(ElementType.WAY, 3).containsExactlyInAnyOrder(listOf(3)))

        assertTrue(dao.getAllByElement(ElementType.WAY, 0).isEmpty())
        assertTrue(dao.getAllByElement(ElementType.NODE, 1).isEmpty())

        assertEquals(
            listOf(ElementKey(ElementType.NODE, 0)),
            dao.get(9)
        )

        assertEquals(
            listOf(ElementKey(ElementType.NODE, 0), ElementKey(ElementType.WAY, 1),),
            dao.get(7)
        )

        assertEquals(
            listOf(ElementKey(ElementType.WAY, 2)),
            dao.get(3)
        )

        // delete
        assertEquals(1, dao.delete(9))
        assertTrue(dao.getAllByElement(ElementType.NODE, 0).containsExactlyInAnyOrder(listOf(7)))

        assertEquals(3, dao.deleteAll(listOf(7, 3)))
        assertTrue(dao.getAllByElement(ElementType.NODE, 0).isEmpty())
        assertTrue(dao.getAllByElement(ElementType.WAY, 1).isEmpty())
        assertTrue(dao.getAllByElement(ElementType.WAY, 2).isEmpty())

        assertTrue(dao.get(9).isEmpty())
        assertTrue(dao.get(7).isEmpty())
        assertTrue(dao.get(3).isEmpty())
    }
}
