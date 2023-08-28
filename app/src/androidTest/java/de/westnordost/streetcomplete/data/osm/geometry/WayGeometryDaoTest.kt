package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WayGeometryDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: WayGeometryDao

    @BeforeTest fun createDao() {
        dao = WayGeometryDao(database, PolylinesSerializer())
    }

    @Test fun testGetNull() {
        assertNull(dao.get(0))
    }

    @Test fun putWrongTypes() {
        val geometry = createSimpleGeometry()

        assertFailsWith<IllegalArgumentException> {
            dao.put(ElementGeometryEntry(ElementType.NODE, 0, geometry))
        }
        assertFailsWith<IllegalArgumentException> {
            dao.put(ElementGeometryEntry(ElementType.RELATION, 0, geometry))
        }
        assertFailsWith<IllegalArgumentException> {
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

        assertNotNull(dao.get(2))
        assertNotNull(dao.get(1))
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

        assertTrue(dao.getAllEntries(ids)
            .containsExactlyInAnyOrder(expectedEntries))
    }

    @Test fun simplePutGet() {
        val geometry = createSimpleGeometry()
        dao.put(ElementGeometryEntry(ElementType.WAY, 0, geometry))
        val dbGeometry = dao.get(0)

        assertEquals(geometry, dbGeometry)
    }

    @Test fun polylineGeometryPutGet() {
        val polylines = arrayListOf(createSomeLatLons(0.0))
        val geometry = ElementPolylinesGeometry(polylines, LatLon(1.0, 2.0))
        dao.put(ElementGeometryEntry(ElementType.WAY, 0, geometry))
        val dbGeometry = dao.get(0)

        assertEquals(geometry, dbGeometry)
    }

    @Test fun polygonGeometryPutGet() {
        val polygons = arrayListOf(createSomeLatLons(0.0), createSomeLatLons(10.0))
        val geometry = ElementPolygonsGeometry(polygons, LatLon(1.0, 2.0))
        dao.put(ElementGeometryEntry(ElementType.WAY, 0, geometry))
        val dbGeometry = dao.get(0)

        assertEquals(geometry, dbGeometry)
    }

    @Test fun delete() {
        dao.put(ElementGeometryEntry(ElementType.WAY, 0, createSimpleGeometry()))
        assertTrue(dao.delete(0))

        assertNull(dao.get(0))
    }

    @Test fun clear() {
        dao.put(ElementGeometryEntry(ElementType.WAY, 0, createSimpleGeometry()))
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
