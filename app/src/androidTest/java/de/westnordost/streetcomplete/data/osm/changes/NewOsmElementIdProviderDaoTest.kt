package de.westnordost.streetcomplete.data.osm.changes

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NewOsmElementIdProviderDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: NewOsmElementIdProviderDao

    @Before fun createDao() {
        dao = NewOsmElementIdProviderDao(dbHelper)
    }

    @Test fun assign_get() {
        assertNull(dao.get(1L))

        val nodeIdSet = mutableSetOf<Long>()
        val wayIdSet = mutableSetOf<Long>()
        val relationIdSet = mutableSetOf<Long>()

        dao.assign(1L, 2, 3, 0)
        val p1 = dao.get(1L)!!

        nodeIdSet.add(p1.nextNodeId())
        nodeIdSet.add(p1.nextNodeId())
        assertThrows { p1.nextNodeId() }

        wayIdSet.add(p1.nextWayId())
        wayIdSet.add(p1.nextWayId())
        wayIdSet.add(p1.nextWayId())
        assertThrows { p1.nextWayId() }

        assertThrows { p1.nextRelationId() }

        dao.assign(2L, 1, 1, 2)
        val p2 = dao.get(2L)!!

        nodeIdSet.add(p2.nextNodeId())
        assertThrows { p2.nextNodeId() }

        wayIdSet.add(p2.nextWayId())
        assertThrows { p2.nextWayId() }

        relationIdSet.add(p2.nextWayId())
        relationIdSet.add(p2.nextWayId())
        assertThrows { p2.nextRelationId() }

        // test if ids are unique
        assertEquals(3, nodeIdSet.size)
        assertEquals(4, wayIdSet.size)
        assertEquals(2, relationIdSet.size)
    }

    @Test fun delete() {
        assertEquals(0, dao.delete(1L))
        dao.assign(1L, 1,1,1)
        assertEquals(3, dao.delete(1L))
        assertNull(dao.get(1L))
    }

    private fun assertThrows(block: () -> Unit) {
        try {
            block()
            fail("Expected exception")
        } catch (e: Throwable) {}
    }
}
