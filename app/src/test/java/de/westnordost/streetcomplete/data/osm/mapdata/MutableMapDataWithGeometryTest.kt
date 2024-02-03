package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MutableMapDataWithGeometryTest {

    @Test fun `put get remove node`() {
        val m = MutableMapDataWithGeometry()
        val p = p(0.0, 0.0)
        val node = node(1, p)
        val geom = ElementPointGeometry(p)

        m.put(node, null)
        assertEquals(node, m.getNode(node.id))
        assertNull(m.getNodeGeometry(node.id))

        m.put(node, geom)
        assertEquals(node, m.getNode(node.id))
        assertEquals(geom, m.getNodeGeometry(node.id))

        m.remove(ElementType.NODE, node.id)
        assertNull(m.getNode(node.id))
        assertNull(m.getNodeGeometry(node.id))
    }

    @Test fun `put get remove way`() {
        val m = MutableMapDataWithGeometry()
        val p = p(0.0, 0.0)
        val way = way(1)
        val geom = ElementPolylinesGeometry(listOf(listOf(p)), p)

        m.put(way, null)
        assertEquals(way, m.getWay(way.id))
        assertNull(m.getWayGeometry(way.id))

        m.put(way, geom)
        assertEquals(way, m.getWay(way.id))
        assertEquals(geom, m.getWayGeometry(way.id))

        m.remove(ElementType.WAY, way.id)
        assertNull(m.getWay(way.id))
        assertNull(m.getWayGeometry(way.id))
    }

    @Test fun `put get remove relation`() {
        val m = MutableMapDataWithGeometry()
        val p = p(0.0, 0.0)
        val rel = rel(1)
        val geom = ElementPolygonsGeometry(listOf(listOf(p)), p)

        m.put(rel, null)
        assertEquals(rel, m.getRelation(rel.id))
        assertNull(m.getRelationGeometry(rel.id))

        m.put(rel, geom)
        assertEquals(rel, m.getRelation(rel.id))
        assertEquals(geom, m.getRelationGeometry(rel.id))
    }

    @Test fun clear() {
        val m = MutableMapDataWithGeometry()
        m.put(node(0), ElementPointGeometry(p()))
        m.put(way(0), ElementPolylinesGeometry(listOf(), p()))
        m.put(rel(0), ElementPolygonsGeometry(listOf(), p()))

        assertEquals(3, m.size)

        m.clear()
        assertEquals(0, m.size)
        assertEquals(0, m.nodes.size)
        assertEquals(0, m.ways.size)
        assertEquals(0, m.relations.size)
        assertNull(m.getNodeGeometry(0))
        assertNull(m.getWayGeometry(0))
        assertNull(m.getRelationGeometry(0))
    }

    @Test fun putAll() {
        val m = MutableMapDataWithGeometry()
        m.put(node(0), ElementPointGeometry(p()))
        m.put(way(0), ElementPolylinesGeometry(listOf(), p()))
        m.put(rel(0), ElementPolygonsGeometry(listOf(), p()))

        val n = MutableMapDataWithGeometry()
        n.putAll(m)

        assertEquals(3, n.size)
        assertEquals(1, m.nodes.size)
        assertEquals(1, m.ways.size)
        assertEquals(1, m.relations.size)
        assertNotNull(m.getNodeGeometry(0))
        assertNotNull(m.getWayGeometry(0))
        assertNotNull(m.getRelationGeometry(0))
    }

    @Test fun removeAll() {
        val m = MutableMapDataWithGeometry()
        m.put(node(0), ElementPointGeometry(p()))
        m.put(node(1), ElementPointGeometry(p()))
        m.put(way(0), ElementPolylinesGeometry(listOf(), p()))

        m.removeAll(listOf(ElementKey(ElementType.NODE, 0), ElementKey(ElementType.WAY, 0)))

        assertNull(m.getNode(0))
        assertNull(m.getNodeGeometry(0))
        assertNull(m.getWay(0))
        assertNull(m.getWayGeometry(0))
        assertNotNull(m.getNode(1))
        assertNotNull(m.getNodeGeometry(1))
    }
}
