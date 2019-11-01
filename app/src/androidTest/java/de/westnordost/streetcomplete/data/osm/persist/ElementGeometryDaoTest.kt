package de.westnordost.streetcomplete.data.osm.persist

import org.junit.Before
import org.junit.Test

import java.util.ArrayList

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.OsmQuest
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.QuestTypeRegistry

import org.junit.Assert.*
import org.mockito.Mockito.mock

class ElementGeometryDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: ElementGeometryDao
    private lateinit var elementGeometryMapping: ElementGeometryMapping

    @Before fun createDao() {
        elementGeometryMapping = ElementGeometryMapping(serializer)
        dao = ElementGeometryDao(dbHelper, elementGeometryMapping)
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
        dao.delete(Element.Type.NODE, 0)

        assertNull(dao.get(Element.Type.NODE, 0))
    }

    @Test fun deleteUnreferenced() {
        val type = Element.Type.WAY
        val id: Long = 0
        val geometry = createSimpleGeometry()

        dao.put(ElementGeometryEntry(type, id, geometry))
        assertEquals(1, dao.deleteUnreferenced())

        dao.put(ElementGeometryEntry(type, id, geometry))
        val questType = mock(OsmElementQuestType::class.java)
        val osmQuestMapping = OsmQuestMapping(serializer, QuestTypeRegistry(listOf(questType)), elementGeometryMapping)
        val questDao = OsmQuestDao(dbHelper, osmQuestMapping)
        questDao.add(OsmQuest(questType, type, id, geometry))
        assertEquals(0, dao.deleteUnreferenced())
    }

    private fun createSimpleGeometry() = ElementPointGeometry(OsmLatLon(50.0, 50.0))

    private fun createSomeLatLons(start: Double): List<LatLon> {
        val result = ArrayList<LatLon>(5)
        for (i in 0..4) {
            result.add(OsmLatLon(start + i, start + i))
        }
        return result
    }
}
