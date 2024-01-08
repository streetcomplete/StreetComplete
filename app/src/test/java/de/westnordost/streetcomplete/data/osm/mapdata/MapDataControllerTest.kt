package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.download.tiles.asBoundingBoxOfEnclosingTiles
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsController
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.pGeom
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.verify
import java.lang.Thread.sleep
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MapDataControllerTest {

    private lateinit var nodeDB: NodeDao
    private lateinit var wayDB: WayDao
    private lateinit var relationDB: RelationDao
    private lateinit var geometryDB: ElementGeometryDao
    private lateinit var elementDB: ElementDao
    private lateinit var controller: MapDataController
    private lateinit var geometryCreator: ElementGeometryCreator
    private lateinit var createdElementsController: CreatedElementsController

    @BeforeTest fun setUp() {
        nodeDB = mock()
        wayDB = mock()
        relationDB = mock()
        geometryDB = mock()
        elementDB = mock()
        geometryCreator = mock()
        createdElementsController = mock()
        controller = MapDataController(nodeDB, wayDB, relationDB, elementDB, geometryDB, geometryCreator, createdElementsController)
    }

    @Test fun get() {
        val node = node(5)
        on(elementDB.get(NODE, 5L)).thenReturn(node)
        assertEquals(node, controller.get(NODE, 5L))
    }

    @Test fun getGeometry() {
        val pGeom = pGeom()
        on(geometryDB.get(NODE, 5L)).thenReturn(pGeom)
        assertEquals(pGeom, controller.getGeometry(NODE, 5L))
    }

    @Test fun getGeometries() {
        val pGeom = ElementGeometryEntry(NODE, 1, pGeom())
        val keys = listOf(ElementKey(NODE, 1))
        on(geometryDB.getAllEntries(keys)).thenReturn(listOf(pGeom))
        assertEquals(
            listOf(pGeom),
            controller.getGeometries(keys)
        )
    }

    @Test fun getMapDataWithGeometry() {
        val bbox = bbox()
        val bboxCacheWillRequest = bbox.asBoundingBoxOfEnclosingTiles(17)
        val geomEntries = listOf(
            ElementGeometryEntry(NODE, 1L, pGeom()),
            ElementGeometryEntry(NODE, 2L, pGeom()),
        )
        val elementKeys = listOf(
            ElementKey(NODE, 1L),
            ElementKey(NODE, 2L),
        )
        val elements = listOf(node(1), node(2))
        on(elementDB.getAll(bboxCacheWillRequest)).thenReturn(elements)
        on(geometryDB.getAllEntries(elementKeys)).thenReturn(geomEntries)

        val mapData = controller.getMapDataWithGeometry(bbox)
        assertTrue(mapData.nodes.containsExactlyInAnyOrder(elements))
        assertEquals(0, mapData.ways.size)
        assertEquals(0, mapData.relations.size)
        assertNotNull(mapData.getGeometry(NODE, 1L))
        assertNotNull(mapData.getGeometry(NODE, 2L))
    }

    @Test fun updateAll() {
        val idUpdates = listOf(
            ElementIdUpdate(NODE, -1, 1)
        )
        val deleteKeys = listOf(
            ElementKey(NODE, 5L),
            ElementKey(NODE, 6L),
        )
        val elements = listOf(node(1), node(2))
        val geomEntries = listOf(
            ElementGeometryEntry(NODE, 1L, pGeom()),
            ElementGeometryEntry(NODE, 2L, pGeom()),
        )
        on(geometryCreator.create(any(), any(), anyBoolean())).thenReturn(pGeom())

        val listener = mock<MapDataController.Listener>()
        controller.addListener(listener)
        controller.updateAll(MapDataUpdates(
            updated = elements,
            deleted = deleteKeys,
            idUpdates = idUpdates
        ))

        val expectedDeleteKeys = deleteKeys + idUpdates.map { ElementKey(it.elementType, it.oldElementId) }
        verify(geometryDB).deleteAll(expectedDeleteKeys)
        verify(elementDB).deleteAll(expectedDeleteKeys)
        verify(elementDB).putAll(elements)
        verify(geometryDB).putAll(eq(geomEntries))
        verify(createdElementsController).putAll(eq(idUpdates.map { ElementKey(it.elementType, it.newElementId) }))

        sleep(100)
        verify(listener).onUpdated(any(), eq(expectedDeleteKeys))
    }

    @Test fun deleteOlderThan() {
        val nodeKeys = listOf(
            ElementKey(NODE, 1L),
            ElementKey(NODE, 2L),
            ElementKey(NODE, 3L),
        )
        val filteredNodeKeys = listOf(
            ElementKey(NODE, 1L),
            ElementKey(NODE, 3L),
        )
        val wayKeys = listOf(
            ElementKey(ElementType.WAY, 1L),
        )
        val relationKeys = listOf(
            ElementKey(ElementType.RELATION, 1L),
        )
        val elementKeys = relationKeys + wayKeys + filteredNodeKeys
        on(nodeDB.getIdsOlderThan(123L)).thenReturn(nodeKeys.map { it.id })
        on(wayDB.getIdsOlderThan(123L)).thenReturn(wayKeys.map { it.id })
        on(relationDB.getIdsOlderThan(123L)).thenReturn(relationKeys.map { it.id })
        on(wayDB.filterNodeIdsWithoutWays(nodeKeys.map { it.id })).thenReturn(filteredNodeKeys.map { it.id })
        val listener = mock<MapDataController.Listener>()

        controller.addListener(listener)
        controller.deleteOlderThan(123L)

        verify(elementDB).deleteAll(wayKeys + relationKeys)
        verify(elementDB).deleteAll(filteredNodeKeys)
        verify(geometryDB).deleteAll(elementKeys)
        verify(createdElementsController).deleteAll(elementKeys)

        sleep(100)
        verify(listener).onUpdated(any(), eq(elementKeys))
    }

    @Test fun clear() {
        val listener = mock<MapDataController.Listener>()
        controller.addListener(listener)
        controller.clear()

        verify(elementDB).clear()
        verify(geometryDB).clear()
        verify(createdElementsController).clear()
        verify(listener).onCleared()
    }

    @Test fun `putAllForBBox when nothing was there before`() {
        val bbox = bbox()
        val elements = listOf(
            node(1),
            node(2)
        )
        val geomEntries = listOf(
            ElementGeometryEntry(NODE, 1L, pGeom()),
            ElementGeometryEntry(NODE, 2L, pGeom()),
        )
        val mapData = TestMapDataWithGeometry(elements)
        mapData.nodeGeometriesById[1] = geomEntries[0].geometry as ElementPointGeometry
        mapData.nodeGeometriesById[2] = geomEntries[1].geometry as ElementPointGeometry

        on(elementDB.getAllKeys(bbox)).thenReturn(emptyList())
        on(geometryDB.getAllEntries(emptyList())).thenReturn(emptyList())
        on(geometryCreator.create(any(), any(), anyBoolean())).thenReturn(pGeom())

        val listener = mock<MapDataController.Listener>()

        controller.addListener(listener)
        controller.putAllForBBox(bbox, mapData)

        verify(elementDB).deleteAll(eq(emptySet()))
        verify(geometryDB).deleteAll(eq(emptySet()))
        verify(geometryDB).putAll(eq(geomEntries))
        verify(elementDB).putAll(eq(mapData))
        verify(listener).onReplacedForBBox(eq(bbox), any())
    }
}
