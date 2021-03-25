package de.westnordost.streetcomplete.data.osm.mapdata

import org.junit.Before
import org.junit.Test

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import org.junit.Assert.*
import java.lang.System.currentTimeMillis
import java.util.Date


class NodeDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: NodeDao

    @Before fun createDao() {
        dao = NodeDao(database, serializer)
    }

    @Test fun putGetNoTags() {
        val node = nd(5, tags = null)
        dao.put(node)
        val dbNode = dao.get(5)

        checkEqual(node, dbNode!!)
    }

    @Test fun putGetWithTags() {
        val node = nd(5, 1, 2.0, 2.0, mapOf("a key" to "a value"))
        dao.put(node)
        val dbNode = dao.get(5)

        checkEqual(node, dbNode!!)
    }

    @Test fun putOverwrites() {
        dao.put(nd(6, 0))
        dao.put(nd(6, 5))
        assertEquals(5, dao.get(6)!!.version)
    }

    @Test fun getNull() {
        assertNull(dao.get(6))
    }

    @Test fun delete() {
        assertFalse(dao.delete(6))
        dao.put(nd(6))
        assertTrue(dao.delete(6))
        assertNull(dao.get(6))
        assertFalse(dao.delete(6))
    }

    @Test fun putAll() {
        dao.putAll(listOf(nd(1), nd(2)))
        assertNotNull(dao.get(1))
        assertNotNull(dao.get(2))
    }

    @Test fun getAll() {
        val e1 = nd(1)
        val e2 = nd(2)
        val e3 = nd(3)
        dao.putAll(listOf(e1,e2,e3))
        assertEquals(listOf(e1, e2).map { it.id }, dao.getAll(listOf(1,2,4)).map { it.id })
    }

    @Test fun deleteAll() {
        dao.putAll(listOf(nd(1), nd(2), nd(3)))
        assertEquals(2,dao.deleteAll(listOf(1,2,4)))
        assertNotNull(dao.get(3))
        assertNull(dao.get(1))
        assertNull(dao.get(2))
    }

    @Test fun getUnusedAndOldIds() {
        dao.putAll(listOf(nd(1L), nd(2L), nd(3L)))
        val unusedIds = dao.getIdsOlderThan(currentTimeMillis() + 10)
        assertTrue(unusedIds.containsExactlyInAnyOrder(listOf(1L, 2L, 3L)))
    }
}

private fun checkEqual(node: Node, dbNode: Node) {
    assertEquals(node.id, dbNode.id)
    assertEquals(node.version, dbNode.version)
    assertEquals(node.position, dbNode.position)
    assertEquals(node.tags, dbNode.tags)
}

private fun nd(
    id: Long = 1L,
    version: Int = 1,
    lat: Double = 1.0,
    lon: Double = 2.0,
    tags: Map<String,String>? = emptyMap(),
    timestamp: Long = 123
) = OsmNode(id, version, lat, lon, tags, null, Date(timestamp))
