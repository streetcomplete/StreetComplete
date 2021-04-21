package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.osmapi.map.data.*
import org.junit.Before
import org.junit.Test

import java.util.ArrayList

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.*
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder

import org.junit.Assert.*

class ElementGeometryDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: ElementGeometryDao

    @Before fun createDao() {
        dao = ElementGeometryDao(database, serializer)
    }

    @Test fun testGetNull() {
        assertNull(dao.get(Element.Type.NODE, 0))
    }

    @Test fun getNullDifferentPKey() {
        dao.put(ElementGeometryEntry(Element.Type.NODE, 0, createSimpleGeometry()))
        assertNull(dao.get(Element.Type.WAY, 0))
        assertNull(dao.get(Element.Type.NODE, 1))
    }

    @Test fun putAll() {
        val geometry = createSimpleGeometry()
        dao.putAll(listOf(
            ElementGeometryEntry(Element.Type.NODE, 1, geometry),
            ElementGeometryEntry(Element.Type.WAY, 2, geometry)
        ))

        assertNotNull(dao.get(Element.Type.WAY, 2))
        assertNotNull(dao.get(Element.Type.NODE, 1))
    }

    @Test fun getAllKeys() {
        dao.putAll(listOf(
            ElementGeometryEntry(Element.Type.NODE, 1, createPoint(0.0,0.0)),
            ElementGeometryEntry(Element.Type.WAY, 2, createPoint(1.0,2.0)),
            ElementGeometryEntry(Element.Type.NODE, 2, createPoint(0.5,1.0)),
            // these are outside
            ElementGeometryEntry(Element.Type.NODE, 3, createPoint(-0.5,1.0)),
            ElementGeometryEntry(Element.Type.NODE, 4, createPoint(1.5,1.0)),
            ElementGeometryEntry(Element.Type.NODE, 5, createPoint(0.5,-0.5)),
            ElementGeometryEntry(Element.Type.NODE, 6, createPoint(0.5,2.5))
        ))

        assertTrue(dao.getAllKeys(BoundingBox(0.0, 0.0, 1.0, 2.0))
            .containsExactlyInAnyOrder(listOf(
                ElementKey(Element.Type.NODE, 1),
                ElementKey(Element.Type.WAY, 2),
                ElementKey(Element.Type.NODE, 2),
        )))
    }

    @Test fun getAllEntriesFoxBBox() {
        val insideElements = listOf(
            ElementGeometryEntry(Element.Type.NODE, 1, createPoint(0.0,0.0)),
            ElementGeometryEntry(Element.Type.WAY, 2, createPoint(1.0,2.0)),
            ElementGeometryEntry(Element.Type.NODE, 2, createPoint(0.5,1.0))
        )
        val outsideElements = listOf(
            ElementGeometryEntry(Element.Type.NODE, 3, createPoint(-0.5,1.0)),
            ElementGeometryEntry(Element.Type.NODE, 4, createPoint(1.5,1.0)),
            ElementGeometryEntry(Element.Type.NODE, 5, createPoint(0.5,-0.5)),
            ElementGeometryEntry(Element.Type.NODE, 6, createPoint(0.5,2.5))
        )
        dao.putAll(insideElements + outsideElements)

        assertTrue(dao.getAllEntries(BoundingBox(0.0, 0.0, 1.0, 2.0))
            .containsExactlyInAnyOrder(insideElements))
    }

    @Test fun getAllEntriesForElementKeys() {
        val entries = listOf(
            ElementGeometryEntry(Element.Type.NODE, 1, createSimpleGeometry()),
            ElementGeometryEntry(Element.Type.NODE, 2, createSimpleGeometry()),
            ElementGeometryEntry(Element.Type.WAY, 1, createSimpleGeometry()),
            ElementGeometryEntry(Element.Type.WAY, 2, createSimpleGeometry()),
            ElementGeometryEntry(Element.Type.RELATION, 1, createSimpleGeometry()),
        )
        dao.putAll(entries)

        val keys = listOf(
            ElementKey(Element.Type.NODE, 1),
            ElementKey(Element.Type.WAY, 2),
            ElementKey(Element.Type.RELATION, 3),
        )

        val expectedEntries = listOf(
            ElementGeometryEntry(Element.Type.NODE, 1, createSimpleGeometry()),
            ElementGeometryEntry(Element.Type.WAY, 2, createSimpleGeometry())
        )

        assertTrue(dao.getAllEntries(keys)
            .containsExactlyInAnyOrder(expectedEntries))
    }

    @Test fun simplePutGet() {
        val geometry = createSimpleGeometry()
        dao.put(ElementGeometryEntry(Element.Type.NODE, 0, geometry))
        val dbGeometry = dao.get(Element.Type.NODE, 0)

        assertEquals(geometry, dbGeometry)
    }

    @Test fun polylineGeometryPutGet() {
        val polylines = arrayListOf(createSomeLatLons(0.0))
        val geometry = ElementPolylinesGeometry(polylines, OsmLatLon(1.0, 2.0))
        dao.put(ElementGeometryEntry(Element.Type.WAY, 0, geometry))
        val dbGeometry = dao.get(Element.Type.WAY, 0)

        assertEquals(geometry, dbGeometry)
    }

    @Test fun polygonGeometryPutGet() {
        val polygons = arrayListOf(createSomeLatLons(0.0), createSomeLatLons(10.0))
        val geometry = ElementPolygonsGeometry(polygons, OsmLatLon(1.0, 2.0))
        dao.put(ElementGeometryEntry(Element.Type.RELATION, 0, geometry))
        val dbGeometry = dao.get(Element.Type.RELATION, 0)

        assertEquals(geometry, dbGeometry)
    }

    @Test fun delete() {
        dao.put(ElementGeometryEntry(Element.Type.NODE, 0, createSimpleGeometry()))
        assertTrue(dao.delete(Element.Type.NODE, 0))

        assertNull(dao.get(Element.Type.NODE, 0))
    }

    private fun createSimpleGeometry() = createPoint(50.0, 50.0)

    private fun createPoint(lat: Double, lon: Double) = ElementPointGeometry(OsmLatLon(lat, lon))

    private fun createSomeLatLons(start: Double): List<LatLon> {
        val result = ArrayList<LatLon>(5)
        for (i in 0..4) {
            result.add(OsmLatLon(start + i, start + i))
        }
        return result
    }
}
