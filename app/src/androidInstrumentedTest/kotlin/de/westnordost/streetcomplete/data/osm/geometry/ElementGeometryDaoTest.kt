package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.NodeDao
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ElementGeometryDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: ElementGeometryDao
    private lateinit var nodeDao: NodeDao

    @BeforeTest fun createDao() {
        nodeDao = NodeDao(database)
        val relationGeometryDao = RelationGeometryDao(database, PolylinesSerializer())
        val wayGeometryDao = WayGeometryDao(database, PolylinesSerializer())
        dao = ElementGeometryDao(nodeDao, wayGeometryDao, relationGeometryDao)
    }

    @Test fun testGetNull() {
        assertNull(dao.get(ElementType.NODE, 0))
    }

    @Test fun getNullDifferentPKey() {
        dao.put(ElementGeometryEntry(ElementType.RELATION, 0, createSimpleGeometry()))
        assertNull(dao.get(ElementType.WAY, 0))
        assertNull(dao.get(ElementType.RELATION, 1))
    }

    @Test fun putAll() {
        val geometry = createSimpleGeometry()
        dao.putAll(listOf(
            ElementGeometryEntry(ElementType.RELATION, 1, geometry),
            ElementGeometryEntry(ElementType.WAY, 2, geometry)
        ))
        nodeDao.put(Node(1, LatLon(0.0, 0.0)))

        assertNotNull(dao.get(ElementType.WAY, 2))
        assertNotNull(dao.get(ElementType.RELATION, 1))
        assertNotNull(dao.get(ElementType.NODE, 1))
    }

    @Test fun dontPutNode() {
        val geometry = createSimpleGeometry()
        dao.putAll(listOf(
            ElementGeometryEntry(ElementType.NODE, 1, geometry),
            ElementGeometryEntry(ElementType.WAY, 2, geometry)
        ))

        assertNotNull(dao.get(ElementType.WAY, 2))
        assertNull(dao.get(ElementType.NODE, 1))
    }

    @Test fun getAllEntriesForElementKeys() {
        val entries = listOf(
            ElementGeometryEntry(ElementType.WAY, 1, createSimpleGeometry()),
            ElementGeometryEntry(ElementType.WAY, 2, createSimpleGeometry()),
            ElementGeometryEntry(ElementType.RELATION, 1, createSimpleGeometry()),
        )
        val nodes = listOf(
            Node(1, LatLon(0.0, 1.0)),
            Node(2, LatLon(0.0, -0.5))
        )
        dao.putAll(entries)
        nodeDao.putAll(nodes)

        val keys = listOf(
            ElementKey(ElementType.NODE, 1),
            ElementKey(ElementType.WAY, 2),
            ElementKey(ElementType.RELATION, 3),
        )

        val expectedEntries = listOf(
            ElementGeometryEntry(ElementType.NODE, 1, ElementPointGeometry(LatLon(0.0, 1.0))),
            ElementGeometryEntry(ElementType.WAY, 2, createSimpleGeometry())
        )

        assertTrue(dao.getAllEntries(keys)
            .containsExactlyInAnyOrder(expectedEntries))
    }

    @Test fun simplePutGet() {
        val geometry = createSimpleGeometry()
        dao.put(ElementGeometryEntry(ElementType.WAY, 0, geometry))
        val dbGeometry = dao.get(ElementType.WAY, 0)

        assertEquals(geometry, dbGeometry)
    }

    @Test fun polylineGeometryPutGet() {
        val polylines = arrayListOf(createSomeLatLons(0.0))
        val geometry = ElementPolylinesGeometry(polylines, LatLon(1.0, 2.0))
        dao.put(ElementGeometryEntry(ElementType.WAY, 0, geometry))
        val dbGeometry = dao.get(ElementType.WAY, 0)

        assertEquals(geometry, dbGeometry)
    }

    @Test fun polygonGeometryPutGet() {
        val polygons = arrayListOf(createSomeLatLons(0.0), createSomeLatLons(10.0))
        val geometry = ElementPolygonsGeometry(polygons, LatLon(1.0, 2.0))
        dao.put(ElementGeometryEntry(ElementType.RELATION, 0, geometry))
        val dbGeometry = dao.get(ElementType.RELATION, 0)

        assertEquals(geometry, dbGeometry)
    }

    @Test fun delete() {
        dao.put(ElementGeometryEntry(ElementType.WAY, 0, createSimpleGeometry()))
        assertTrue(dao.delete(ElementType.WAY, 0))

        assertNull(dao.get(ElementType.WAY, 0))
    }

    @Test fun clear() {
        dao.put(ElementGeometryEntry(ElementType.WAY, 0, createSimpleGeometry()))
        dao.clear()
        assertNull(dao.get(ElementType.WAY, 0))
    }

    private fun createSimpleGeometry() = createPoint(50.0, 50.0)

    private fun createPoint(lat: Double, lon: Double) = ElementPointGeometry(LatLon(lat, lon))

    private fun createSomeLatLons(start: Double): List<LatLon> {
        val result = ArrayList<LatLon>(5)
        for (i in 0..4) {
            result.add(LatLon(start + i, start + i))
        }
        return result
    }
}
