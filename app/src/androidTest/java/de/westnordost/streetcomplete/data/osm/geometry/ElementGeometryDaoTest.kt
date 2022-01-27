package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ElementGeometryDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: ElementGeometryDao

    @Before fun createDao() {
        dao = ElementGeometryDao(database, PolylinesSerializer())
    }

    @Test fun testGetNull() {
        assertNull(dao.get(ElementType.NODE, 0))
    }

    @Test fun getNullDifferentPKey() {
        dao.put(ElementGeometryEntry(ElementType.NODE, 0, createSimpleGeometry()))
        assertNull(dao.get(ElementType.WAY, 0))
        assertNull(dao.get(ElementType.NODE, 1))
    }

    @Test fun putAll() {
        val geometry = createSimpleGeometry()
        dao.putAll(listOf(
            ElementGeometryEntry(ElementType.NODE, 1, geometry),
            ElementGeometryEntry(ElementType.WAY, 2, geometry)
        ))

        assertNotNull(dao.get(ElementType.WAY, 2))
        assertNotNull(dao.get(ElementType.NODE, 1))
    }

    @Test fun getAllKeys() {
        dao.putAll(listOf(
            ElementGeometryEntry(ElementType.NODE, 1, createPoint(0.0,0.0)),
            ElementGeometryEntry(ElementType.WAY, 2, createPoint(1.0,2.0)),
            ElementGeometryEntry(ElementType.NODE, 2, createPoint(0.5,1.0)),
            // these are outside
            ElementGeometryEntry(ElementType.NODE, 3, createPoint(-0.5,1.0)),
            ElementGeometryEntry(ElementType.NODE, 4, createPoint(1.5,1.0)),
            ElementGeometryEntry(ElementType.NODE, 5, createPoint(0.5,-0.5)),
            ElementGeometryEntry(ElementType.NODE, 6, createPoint(0.5,2.5))
        ))

        assertTrue(dao.getAllKeys(BoundingBox(0.0, 0.0, 1.0, 2.0))
            .containsExactlyInAnyOrder(listOf(
                ElementKey(ElementType.NODE, 1),
                ElementKey(ElementType.WAY, 2),
                ElementKey(ElementType.NODE, 2),
        )))
    }

    @Test fun getAllEntriesFoxBBox() {
        val insideElements = listOf(
            ElementGeometryEntry(ElementType.NODE, 1, createPoint(0.0,0.0)),
            ElementGeometryEntry(ElementType.WAY, 2, createPoint(1.0,2.0)),
            ElementGeometryEntry(ElementType.NODE, 2, createPoint(0.5,1.0))
        )
        val outsideElements = listOf(
            ElementGeometryEntry(ElementType.NODE, 3, createPoint(-0.5,1.0)),
            ElementGeometryEntry(ElementType.NODE, 4, createPoint(1.5,1.0)),
            ElementGeometryEntry(ElementType.NODE, 5, createPoint(0.5,-0.5)),
            ElementGeometryEntry(ElementType.NODE, 6, createPoint(0.5,2.5))
        )
        dao.putAll(insideElements + outsideElements)

        assertTrue(dao.getAllEntries(BoundingBox(0.0, 0.0, 1.0, 2.0))
            .containsExactlyInAnyOrder(insideElements))
    }

    @Test fun getAllEntriesForElementKeys() {
        val entries = listOf(
            ElementGeometryEntry(ElementType.NODE, 1, createSimpleGeometry()),
            ElementGeometryEntry(ElementType.NODE, 2, createSimpleGeometry()),
            ElementGeometryEntry(ElementType.WAY, 1, createSimpleGeometry()),
            ElementGeometryEntry(ElementType.WAY, 2, createSimpleGeometry()),
            ElementGeometryEntry(ElementType.RELATION, 1, createSimpleGeometry()),
        )
        dao.putAll(entries)

        val keys = listOf(
            ElementKey(ElementType.NODE, 1),
            ElementKey(ElementType.WAY, 2),
            ElementKey(ElementType.RELATION, 3),
        )

        val expectedEntries = listOf(
            ElementGeometryEntry(ElementType.NODE, 1, createSimpleGeometry()),
            ElementGeometryEntry(ElementType.WAY, 2, createSimpleGeometry())
        )

        assertTrue(dao.getAllEntries(keys)
            .containsExactlyInAnyOrder(expectedEntries))
    }

    @Test fun simplePutGet() {
        val geometry = createSimpleGeometry()
        dao.put(ElementGeometryEntry(ElementType.NODE, 0, geometry))
        val dbGeometry = dao.get(ElementType.NODE, 0)

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
        dao.put(ElementGeometryEntry(ElementType.NODE, 0, createSimpleGeometry()))
        assertTrue(dao.delete(ElementType.NODE, 0))

        assertNull(dao.get(ElementType.NODE, 0))
    }

    @Test fun clear() {
        dao.put(ElementGeometryEntry(ElementType.NODE, 0, createSimpleGeometry()))
        dao.clear()
        assertNull(dao.get(ElementType.NODE, 0))
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
