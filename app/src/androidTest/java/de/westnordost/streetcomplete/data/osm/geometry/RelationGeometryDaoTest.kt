package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class RelationGeometryDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: RelationGeometryDao

    @Before fun createDao() {
        dao = RelationGeometryDao(database, PolylinesSerializer())
    }

    @Test fun testGetNull() {
        assertNull(dao.get(0))
    }

    @Test fun putWrongTypes() {
        val geometry = createSimpleGeometry()

        assertThrows {
            dao.put(ElementGeometryEntry(ElementType.NODE, 0, geometry))
        }
        assertThrows {
            dao.put(ElementGeometryEntry(ElementType.WAY, 0, geometry))
        }
        assertThrows {
            dao.putAll(listOf(
                ElementGeometryEntry(ElementType.NODE, 1, geometry),
                ElementGeometryEntry(ElementType.WAY, 2, geometry)
            ))
        }
    }

    @Test fun putAll() {
        val geometry = createSimpleGeometry()
        dao.putAll(listOf(
            ElementGeometryEntry(ElementType.RELATION, 1, geometry),
            ElementGeometryEntry(ElementType.RELATION, 2, geometry)
        ))

        assertNotNull(dao.get(2))
        assertNotNull(dao.get(1))
    }

    @Test fun getAllIds() {
        dao.putAll(listOf(
            ElementGeometryEntry(ElementType.RELATION, 1, createPoint(0.0, 0.0)),
            ElementGeometryEntry(ElementType.RELATION, 2, createPoint(1.0, 2.0)),
            // these are outside
            ElementGeometryEntry(ElementType.RELATION, 3, createPoint(-0.5, 1.0)),
            ElementGeometryEntry(ElementType.RELATION, 4, createPoint(1.5, 1.0)),
            ElementGeometryEntry(ElementType.RELATION, 5, createPoint(0.5, -0.5)),
            ElementGeometryEntry(ElementType.RELATION, 6, createPoint(0.5, 2.5))
        ))

        assertTrue(dao.getAllIds(BoundingBox(0.0, 0.0, 1.0, 2.0))
            .containsExactlyInAnyOrder(listOf(1L,2L)))
    }

    @Test fun getAllEntriesForBBox() {
        val insideElements = listOf(
            ElementGeometryEntry(ElementType.RELATION, 1, createPoint(0.0, 0.0)),
            ElementGeometryEntry(ElementType.RELATION, 2, createPoint(1.0, 2.0)),
        )
        val outsideElements = listOf(
            ElementGeometryEntry(ElementType.RELATION, 3, createPoint(-0.5, 1.0)),
            ElementGeometryEntry(ElementType.RELATION, 4, createPoint(1.5, 1.0)),
            ElementGeometryEntry(ElementType.RELATION, 5, createPoint(0.5, -0.5)),
            ElementGeometryEntry(ElementType.RELATION, 6, createPoint(0.5, 2.5))
        )
        dao.putAll(insideElements + outsideElements)

        assertTrue(dao.getAllEntries(BoundingBox(0.0, 0.0, 1.0, 2.0))
            .containsExactlyInAnyOrder(insideElements))
    }

    @Test fun getAllEntriesForElementIds() {
        val entries = listOf(
            ElementGeometryEntry(ElementType.RELATION, 1, createSimpleGeometry()),
            ElementGeometryEntry(ElementType.RELATION, 3, createSimpleGeometry()),
            ElementGeometryEntry(ElementType.RELATION, 4, createSimpleGeometry()),
        )
        dao.putAll(entries)

        val ids = listOf(1L, 2L, 3L)

        val expectedEntries = listOf(
            ElementGeometryEntry(ElementType.RELATION, 1, createSimpleGeometry()),
            ElementGeometryEntry(ElementType.RELATION, 3, createSimpleGeometry())
        )

        assertTrue(dao.getAllEntries(ids)
            .containsExactlyInAnyOrder(expectedEntries))
    }

    @Test fun simplePutGet() {
        val geometry = createSimpleGeometry()
        dao.put(ElementGeometryEntry(ElementType.RELATION, 0, geometry))
        val dbGeometry = dao.get(0)

        assertEquals(geometry, dbGeometry)
    }

    @Test fun polylineGeometryPutGet() {
        val polylines = arrayListOf(createSomeLatLons(0.0))
        val geometry = ElementPolylinesGeometry(polylines, LatLon(1.0, 2.0))
        dao.put(ElementGeometryEntry(ElementType.RELATION, 0, geometry))
        val dbGeometry = dao.get(0)

        assertEquals(geometry, dbGeometry)
    }

    @Test fun polygonGeometryPutGet() {
        val polygons = arrayListOf(createSomeLatLons(0.0), createSomeLatLons(10.0))
        val geometry = ElementPolygonsGeometry(polygons, LatLon(1.0, 2.0))
        dao.put(ElementGeometryEntry(ElementType.RELATION, 0, geometry))
        val dbGeometry = dao.get(0)

        assertEquals(geometry, dbGeometry)
    }

    @Test fun delete() {
        dao.put(ElementGeometryEntry(ElementType.RELATION, 0, createSimpleGeometry()))
        assertTrue(dao.delete(0))

        assertNull(dao.get(0))
    }

    @Test fun clear() {
        dao.put(ElementGeometryEntry(ElementType.RELATION, 0, createSimpleGeometry()))
        dao.clear()
        assertNull(dao.get(0))
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

private fun assertThrows(block: () -> Unit) {
    try {
        block()
        fail("Expected exception")
    } catch (e: Throwable) {}
}
