package de.westnordost.streetcomplete.data.osm.persist

import org.junit.Before
import org.junit.Test

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode

import org.junit.Assert.assertEquals


class NodeDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: NodeDao

    @Before fun createDao() {
        dao = NodeDao(dbHelper, NodeMapping(serializer))
    }

    @Test fun putGetNoTags() {
        val pos = OsmLatLon(2.0, 2.0)
        val node = OsmNode(5, 1, pos, null)
        dao.put(node)
        val dbNode = dao.get(5)

        checkEqual(node, dbNode!!)
    }

    @Test fun putGetWithTags() {
        val pos = OsmLatLon(2.0, 2.0)
        val node = OsmNode(5, 1, pos, mapOf("a key" to "a value"))
        dao.put(node)
        val dbNode = dao.get(5)

        checkEqual(node, dbNode!!)
    }

    private fun checkEqual(node: Node, dbNode: Node) {
        assertEquals(node.id, dbNode.id)
        assertEquals(node.version.toLong(), dbNode.version.toLong())
        assertEquals(node.position, dbNode.position)
        assertEquals(node.tags, dbNode.tags)
    }
}
