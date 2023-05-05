package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ElementIdProviderDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: ElementIdProviderDao

    @Before fun createDao() {
        dao = ElementIdProviderDao(database)
    }

    @Test fun assign_get() {
        assertTrue(dao.get(1L).isEmpty())

        val nodeIdSet = mutableSetOf<Long>()
        val wayIdSet = mutableSetOf<Long>()
        val relationIdSet = mutableSetOf<Long>()

        dao.assign(1L, 2, 3, 0)
        val p1 = dao.get(1L)

        nodeIdSet.add(p1.nextNodeId())
        nodeIdSet.add(p1.nextNodeId())
        assertThrows { p1.nextNodeId() }

        wayIdSet.add(p1.nextWayId())
        wayIdSet.add(p1.nextWayId())
        wayIdSet.add(p1.nextWayId())
        assertThrows { p1.nextWayId() }

        assertThrows { p1.nextRelationId() }

        dao.assign(2L, 1, 1, 2)
        val p2 = dao.get(2L)

        nodeIdSet.add(p2.nextNodeId())
        assertThrows { p2.nextNodeId() }

        wayIdSet.add(p2.nextWayId())
        assertThrows { p2.nextWayId() }

        relationIdSet.add(p2.nextRelationId())
        relationIdSet.add(p2.nextRelationId())
        assertThrows { p2.nextRelationId() }

        // test if ids are unique
        assertEquals(3, nodeIdSet.size)
        assertEquals(4, wayIdSet.size)
        assertEquals(2, relationIdSet.size)
    }

    @Test fun startsWithMinus1() {
        dao.assign(1L, 2, 2, 2)
        val p = dao.get(1L)
        assertEquals(-1, p.nextNodeId())
        assertEquals(-2, p.nextNodeId())
        assertEquals(-3, p.nextWayId())
        assertEquals(-4, p.nextWayId())
        assertEquals(-5, p.nextRelationId())
        assertEquals(-6, p.nextRelationId())
    }

    @Test fun delete() {
        assertEquals(0, dao.delete(1L))
        dao.assign(1L, 1, 1, 1)
        assertEquals(3, dao.delete(1L))
        assertTrue(dao.get(1L).isEmpty())
    }

    @Test fun deleteAll() {
        assertEquals(0, dao.deleteAll(listOf(1L, 2L)))
        dao.assign(1L, 1, 2, 3)
        dao.assign(2L, 2, 1, 0)
        assertEquals(9, dao.deleteAll(listOf(1L, 2L)))
        assertTrue(dao.get(1L).isEmpty())
        assertTrue(dao.get(2L).isEmpty())
    }

    @Test fun updateIds() {
        dao.assign(1L, 1, 1, 1)
        dao.updateIds(listOf(
            ElementIdUpdate(ElementType.NODE, -1, 99),
            ElementIdUpdate(ElementType.WAY, -2, 999),
            ElementIdUpdate(ElementType.RELATION, -3, 9999),
        ))

        assertEquals(
            listOf(
                ElementKey(ElementType.NODE, 99),
                ElementKey(ElementType.WAY, 999),
                ElementKey(ElementType.RELATION, 9999),
            ),
            dao.get(1L).getAll()
        )
    }

    private fun assertThrows(block: () -> Unit) {
        try {
            block()
            fail("Expected exception")
        } catch (e: Throwable) {}
    }
}
