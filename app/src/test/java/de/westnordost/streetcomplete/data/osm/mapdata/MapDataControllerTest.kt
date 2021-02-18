package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.eq
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.verify

class MapDataControllerTest {

    private lateinit var nodeDB: NodeDao
    private lateinit var wayDB: WayDao
    private lateinit var elementDB: ElementDao
    private lateinit var geometryDB: ElementGeometryDao
    private lateinit var controller: MapDataController
    private lateinit var geometryCreator: ElementGeometryCreator

    @Before fun setUp() {
        nodeDB = mock()
        wayDB = mock()
        elementDB = mock()
        geometryDB = mock()
        geometryCreator = mock()
        controller = MapDataController(elementDB, wayDB, nodeDB, geometryDB, geometryCreator)
    }

    @Test fun get() {
        val node = node(5)
        on(elementDB.get(Element.Type.NODE, 5L)).thenReturn(node)
        assertEquals(node, controller.get(Element.Type.NODE,5L))
    }

    @Test fun getGeometry() {
        val geom = geom()
        on(geometryDB.get(Element.Type.NODE, 5L)).thenReturn(geom)
        assertEquals(geom, controller.getGeometry(Element.Type.NODE,5L))
    }

    @Test fun getMapDataWithGeometry() {
        val bbox = BoundingBox(0.0,1.0,2.0,3.0)
        val geomEntries = listOf(
            ElementGeometryEntry(Element.Type.NODE, 1L, geom()),
            ElementGeometryEntry(Element.Type.NODE, 2L, geom()),
        )
        val elementKeys = listOf(
            ElementKey(Element.Type.NODE, 1L),
            ElementKey(Element.Type.NODE, 2L),
        )
        val elements = listOf(node(1), node(2))
        on(geometryDB.getAllEntries(bbox)).thenReturn(geomEntries)
        on(elementDB.getAll(eq(elementKeys))).thenReturn(elements)

        val mapData = controller.getMapDataWithGeometry(bbox)
        assertTrue(mapData.nodes.containsExactlyInAnyOrder(elements))
        assertEquals(0, mapData.ways.size)
        assertEquals(0, mapData.relations.size)
        assertNotNull(mapData.getGeometry(Element.Type.NODE, 1L))
        assertNotNull(mapData.getGeometry(Element.Type.NODE, 2L))
    }

    @Test fun deleteAll() {
        val elementKeys = listOf(
            ElementKey(Element.Type.NODE, 1L),
            ElementKey(Element.Type.NODE, 2L),
        )
        val listener = mock<MapDataSource.Listener>()
        controller.addListener(listener)
        controller.deleteAll(elementKeys)

        verify(geometryDB).deleteAll(elementKeys)
        verify(elementDB).deleteAll(elementKeys)
        verify(listener).onUpdated(any(), eq(elementKeys))
    }

    @Test fun putAll() {
        val elements = listOf(node(1), node(2))
        val geomEntries = listOf(
            ElementGeometryEntry(Element.Type.NODE, 1L, geom()),
            ElementGeometryEntry(Element.Type.NODE, 2L, geom()),
        )
        val listener = mock<MapDataSource.Listener>()
        on(geometryCreator.create(any(), any(), anyBoolean())).thenReturn(geom())

        controller.addListener(listener)
        controller.putAll(elements)

        verify(elementDB).putAll(elements)
        verify(geometryDB).putAll(eq(geomEntries))
        verify(listener).onUpdated(any(), eq(emptyList()))
    }

    @Test fun deleteOlderThan() {
        val elementKeys = listOf(
            ElementKey(Element.Type.NODE, 1L),
            ElementKey(Element.Type.NODE, 2L),
        )
        on(elementDB.getIdsOlderThan(123L)).thenReturn(elementKeys)
        val listener = mock<MapDataSource.Listener>()

        controller.addListener(listener)
        controller.deleteUnreferencedOlderThan(123L)

        verify(elementDB).deleteAll(elementKeys)
        verify(geometryDB).deleteAll(elementKeys)
        verify(listener).onUpdated(any(), eq(elementKeys))
    }

    @Test fun `putAllForBBox when nothing was there before`() {
        val bbox = BoundingBox(0.0,1.0,2.0,3.0)
        val elements = listOf(
            node(1),
            node(2)
        )
        val geomEntries = listOf(
            ElementGeometryEntry(Element.Type.NODE, 1L, geom()),
            ElementGeometryEntry(Element.Type.NODE, 2L, geom()),
        )
        val mapData = TestMapDataWithGeometry(elements)
        mapData.nodeGeometriesById[1] = geomEntries[0].geometry as ElementPointGeometry
        mapData.nodeGeometriesById[2] = geomEntries[1].geometry as ElementPointGeometry

        on(geometryDB.getAllKeys(bbox)).thenReturn(emptyList())
        on(geometryDB.getAllEntries(bbox)).thenReturn(emptyList())
        on(geometryCreator.create(any(), any(), anyBoolean())).thenReturn(geom())

        val listener = mock<MapDataSource.Listener>()

        controller.addListener(listener)
        controller.putAllForBBox(bbox, mapData)

        verify(elementDB).deleteAll(eq(emptySet()))
        verify(geometryDB).deleteAll(eq(emptySet()))
        verify(geometryDB).putAll(eq(geomEntries))
        verify(elementDB).putAll(eq(mapData))
        verify(listener).onReplacedForBBox(eq(bbox), any())
    }
}

private fun node(id: Long) = OsmNode(id, 0, 0.0, 0.0, null)

private fun geom() = ElementPointGeometry(OsmLatLon(0.0, 0.0))
