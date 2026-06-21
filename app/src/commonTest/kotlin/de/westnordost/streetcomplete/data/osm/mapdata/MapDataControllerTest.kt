package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.download.tiles.asBoundingBoxOfEnclosingTiles
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsController
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.testutils.TestMapDataWithGeometry
import dev.mokkery.matcher.any
import de.westnordost.streetcomplete.testutils.bbox
import dev.mokkery.mock
import de.westnordost.streetcomplete.testutils.node
import dev.mokkery.answering.returns
import dev.mokkery.every
import de.westnordost.streetcomplete.testutils.pGeom
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import dev.mokkery.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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
    private val geometryCreator = ElementGeometryCreator()
    private lateinit var createdElementsController: CreatedElementsController

    @BeforeTest fun setUp() {
        nodeDB = mock()
        wayDB = mock()
        relationDB = mock()
        geometryDB = mock()
        elementDB = mock()
        createdElementsController = mock()
        controller = MapDataController(nodeDB, wayDB, relationDB, elementDB, geometryDB, geometryCreator, createdElementsController)
    }

    @Test fun get() {
        val node = node(5)
        every { elementDB.get(NODE, 5L) } returns node
        assertEquals(node, controller.get(NODE, 5L))
    }

    @Test fun getGeometry() {
        val pGeom = pGeom()
        every { geometryDB.get(NODE, 5L) } returns pGeom
        assertEquals(pGeom, controller.getGeometry(NODE, 5L))
    }

    @Test fun getGeometries() {
        val pGeom = ElementGeometryEntry(NODE, 1, pGeom())
        val keys = listOf(ElementKey(NODE, 1))
        every { geometryDB.getAllEntries(keys) } returns listOf(pGeom)
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
        every { elementDB.getAll(bboxCacheWillRequest) } returns elements
        every { geometryDB.getAllEntries(elementKeys) } returns geomEntries
        every { geometryDB.getAllEntries(emptyList()) } returns emptyList()

        val mapData = controller.getMapDataWithGeometry(bbox)
        assertTrue(mapData.nodes.containsExactlyInAnyOrder(elements))
        assertEquals(0, mapData.ways.size)
        assertEquals(0, mapData.relations.size)
        assertNotNull(mapData.getGeometry(NODE, 1L))
        assertNotNull(mapData.getGeometry(NODE, 2L))
    }

    @Test fun updateAll(): Unit = runBlocking {
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

        val listener = mock<MapDataController.Listener>()
        controller.addListener(listener)
        controller.updateAll(MapDataUpdates(
            updated = elements,
            deleted = deleteKeys,
            idUpdates = idUpdates
        ))

        val expectedDeleteKeys = deleteKeys + idUpdates.map { ElementKey(it.elementType, it.oldElementId) }
        verify { geometryDB.deleteAll(expectedDeleteKeys) }
        verify { elementDB.deleteAll(expectedDeleteKeys) }
        verify { elementDB.putAll(elements) }
        verify { geometryDB.putAll(geomEntries) }
        verify { createdElementsController.putAll(idUpdates.map { ElementKey(it.elementType, it.newElementId) }) }

        delay(100)
        verify { listener.onUpdated(any(), expectedDeleteKeys) }
    }

    @Test fun deleteOlderThan(): Unit = runBlocking {
        val elementKeys = listOf(
            ElementKey(NODE, 1L),
            ElementKey(NODE, 2L),
        )
        every { elementDB.getIdsOlderThan(123L) } returns elementKeys
        val listener = mock<MapDataController.Listener>()

        controller.addListener(listener)
        controller.deleteOlderThan(123L)

        verify { elementDB.deleteAll(elementKeys) }
        verify { geometryDB.deleteAll(elementKeys) }
        verify { createdElementsController.deleteAll(elementKeys) }

        delay(100)
        verify { listener.onUpdated(any(), elementKeys) }
    }

    @Test fun clear() {
        val listener = mock<MapDataController.Listener>()
        controller.addListener(listener)
        controller.clear()

        verify { elementDB.clear() }
        verify { geometryDB.clear() }
        verify { createdElementsController.clear() }
        verify { listener.onCleared() }
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

        every { elementDB.getAllKeys(bbox) } returns emptyList()
        every { geometryDB.getAllEntries(emptyList()) } returns emptyList()

        val listener = mock<MapDataController.Listener>()

        controller.addListener(listener)
        controller.putAllForBBox(bbox, mapData)

        verify { elementDB.deleteAll(emptySet()) }
        verify { geometryDB.deleteAll(emptySet()) }
        verify { geometryDB.putAll(geomEntries) }
        verify { elementDB.putAll(mapData) }
        verify { listener.onReplacedForBBox(bbox, any()) }
    }
}
