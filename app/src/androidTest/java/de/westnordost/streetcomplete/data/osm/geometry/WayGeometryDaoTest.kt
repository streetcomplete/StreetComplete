package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class WayGeometryDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: WayGeometryDao

    @Before fun createDao() {
        dao = WayGeometryDao(database, PolylinesSerializer())
    }

    @Test fun testGetNull() {
        Assert.assertNull(dao.get(0))
    }

    @Test fun putWrongTypes() {
        val geometry = createSimpleGeometry()

        assertThrows {
            dao.put(ElementGeometryEntry(ElementType.NODE, 0, geometry))
        }
        assertThrows {
            dao.put(ElementGeometryEntry(ElementType.RELATION, 0, geometry))
        }
        assertThrows {
            dao.putAll(listOf(
                ElementGeometryEntry(ElementType.NODE, 1, geometry),
                ElementGeometryEntry(ElementType.RELATION, 2, geometry)
            ))
        }
    }

    @Test fun putAll() {
        val geometry = createSimpleGeometry()
        dao.putAll(listOf(
            ElementGeometryEntry(ElementType.WAY, 1, geometry),
            ElementGeometryEntry(ElementType.WAY, 2, geometry)
        ))

        Assert.assertNotNull(dao.get(2))
        Assert.assertNotNull(dao.get(1))
    }

    @Test fun getAllKeys() {
        dao.putAll(listOf(
            ElementGeometryEntry(ElementType.WAY, 1, createPoint(0.0, 0.0)),
            ElementGeometryEntry(ElementType.WAY, 2, createPoint(1.0, 2.0)),
            // these are outside
            ElementGeometryEntry(ElementType.WAY, 3, createPoint(-0.5, 1.0)),
            ElementGeometryEntry(ElementType.WAY, 4, createPoint(1.5, 1.0)),
            ElementGeometryEntry(ElementType.WAY, 5, createPoint(0.5, -0.5)),
            ElementGeometryEntry(ElementType.WAY, 6, createPoint(0.5, 2.5))
        ))

        Assert.assertTrue(dao.getAllKeys(BoundingBox(0.0, 0.0, 1.0, 2.0))
            .containsExactlyInAnyOrder(listOf(
                ElementKey(ElementType.WAY, 1),
                ElementKey(ElementType.WAY, 2),
            )))
    }

    @Test fun getAllEntriesForBBox() {
        val insideElements = listOf(
            ElementGeometryEntry(ElementType.WAY, 1, createPoint(0.0, 0.0)),
            ElementGeometryEntry(ElementType.WAY, 2, createPoint(1.0, 2.0)),
        )
        val outsideElements = listOf(
            ElementGeometryEntry(ElementType.WAY, 3, createPoint(-0.5, 1.0)),
            ElementGeometryEntry(ElementType.WAY, 4, createPoint(1.5, 1.0)),
            ElementGeometryEntry(ElementType.WAY, 5, createPoint(0.5, -0.5)),
            ElementGeometryEntry(ElementType.WAY, 6, createPoint(0.5, 2.5))
        )
        dao.putAll(insideElements + outsideElements)

        Assert.assertTrue(dao.getAllEntries(BoundingBox(0.0, 0.0, 1.0, 2.0))
            .containsExactlyInAnyOrder(insideElements))
    }

    @Test fun getAllEntriesForElementIds() {
        val entries = listOf(
            ElementGeometryEntry(ElementType.WAY, 1, createSimpleGeometry()),
            ElementGeometryEntry(ElementType.WAY, 3, createSimpleGeometry()),
            ElementGeometryEntry(ElementType.WAY, 4, createSimpleGeometry()),
        )
        dao.putAll(entries)

        val ids = listOf(1L, 2L, 3L)

        val expectedEntries = listOf(
            ElementGeometryEntry(ElementType.WAY, 1, createSimpleGeometry()),
            ElementGeometryEntry(ElementType.WAY, 3, createSimpleGeometry())
        )

        Assert.assertTrue(dao.getAllEntries(ids)
            .containsExactlyInAnyOrder(expectedEntries))
    }

    @Test fun simplePutGet() {
        val geometry = createSimpleGeometry()
        dao.put(ElementGeometryEntry(ElementType.WAY, 0, geometry))
        val dbGeometry = dao.get(0)

        Assert.assertEquals(geometry, dbGeometry)
    }

    @Test fun polylineGeometryPutGet() {
        val polylines = arrayListOf(createSomeLatLons(0.0))
        val geometry = ElementPolylinesGeometry(polylines, LatLon(1.0, 2.0))
        dao.put(ElementGeometryEntry(ElementType.WAY, 0, geometry))
        val dbGeometry = dao.get(0)

        Assert.assertEquals(geometry, dbGeometry)
    }

    @Test fun polygonGeometryPutGet() {
        val polygons = arrayListOf(createSomeLatLons(0.0), createSomeLatLons(10.0))
        val geometry = ElementPolygonsGeometry(polygons, LatLon(1.0, 2.0))
        dao.put(ElementGeometryEntry(ElementType.WAY, 0, geometry))
        val dbGeometry = dao.get(0)

        Assert.assertEquals(geometry, dbGeometry)
    }

    @Test fun delete() {
        dao.put(ElementGeometryEntry(ElementType.WAY, 0, createSimpleGeometry()))
        Assert.assertTrue(dao.delete(0))

        Assert.assertNull(dao.get(0))
    }

    @Test fun clear() {
        dao.put(ElementGeometryEntry(ElementType.WAY, 0, createSimpleGeometry()))
        dao.clear()
        Assert.assertNull(dao.get(0))
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
        Assert.fail("Expected exception")
    } catch (e: Throwable) {}
}
