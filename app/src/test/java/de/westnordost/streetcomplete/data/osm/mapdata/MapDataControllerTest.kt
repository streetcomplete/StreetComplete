package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.download.tiles.asBoundingBoxOfEnclosingTiles
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsController
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.pGeom
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.doesNothing
import io.mockative.eq
import io.mockative.every
import io.mockative.mock
import java.lang.Thread.sleep
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MapDataControllerTest {

    @Mock private lateinit var nodeDB: NodeDao
    @Mock private lateinit var wayDB: WayDao
    @Mock private lateinit var relationDB: RelationDao
    @Mock private lateinit var geometryDB: ElementGeometryDao
    @Mock private lateinit var elementDB: ElementDao
    private lateinit var controller: MapDataController
    @Mock private lateinit var geometryCreator: ElementGeometryCreator
    @Mock private lateinit var createdElementsController: CreatedElementsController

    // dummy
    @Mock private lateinit var listener: MapDataController.Listener

    @BeforeTest fun setUp() {
        nodeDB = mock(classOf<NodeDao>())
        wayDB = mock(classOf<WayDao>())
        relationDB = mock(classOf<RelationDao>())
        geometryDB = mock(classOf<ElementGeometryDao>())
        elementDB = mock(classOf<ElementDao>())
        geometryCreator = mock(classOf<ElementGeometryCreator>())
        createdElementsController = mock(classOf<CreatedElementsController>())
        controller = MapDataController(nodeDB, wayDB, relationDB, elementDB, geometryDB, geometryCreator, createdElementsController)
    }

    @Test fun get() {
        val node = node(5)
        every { elementDB.get(NODE, 5L) }.returns(node)
        assertEquals(node, controller.get(NODE, 5L))
    }

    @Test fun getGeometry() {
        val pGeom = pGeom()
        every { geometryDB.get(NODE, 5L) }.returns(pGeom)
        assertEquals(pGeom, controller.getGeometry(NODE, 5L))
    }

    @Test fun getGeometries() {
        val pGeom = ElementGeometryEntry(NODE, 1, pGeom())
        val keys = listOf(ElementKey(NODE, 1))
        every { geometryDB.getAllEntries(keys) }.returns(listOf(pGeom))
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
        every { elementDB.getAll(bboxCacheWillRequest) }.returns(elements)
        every { geometryDB.getAllEntries(emptyList()) }.returns(emptyList())
        every { geometryDB.getAllEntries(elementKeys) }.returns(geomEntries)

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

        val expectedDeleteKeys = deleteKeys + idUpdates.map { ElementKey(it.elementType, it.oldElementId) }

        every { geometryCreator.create(any(), any(), any()) }.returns(pGeom())

        every { geometryDB.deleteAll(expectedDeleteKeys) }.returns(expectedDeleteKeys.size)
        every { elementDB.deleteAll(expectedDeleteKeys) }.returns(expectedDeleteKeys.size)
        every { elementDB.putAll(elements) }.doesNothing()
        every { geometryDB.putAll(eq(geomEntries)) }.doesNothing()
        every { createdElementsController.putAll(eq(idUpdates.map { ElementKey(it.elementType, it.newElementId) })) }.doesNothing()

        val listener = mock(classOf<MapDataController.Listener>())
        controller.addListener(listener)
        controller.updateAll(MapDataUpdates(
            updated = elements,
            deleted = deleteKeys,
            idUpdates = idUpdates
        ))

        verifyInvokedExactlyOnce { geometryDB.deleteAll(expectedDeleteKeys) }
        verifyInvokedExactlyOnce { elementDB.deleteAll(expectedDeleteKeys) }
        verifyInvokedExactlyOnce { elementDB.putAll(elements) }
        verifyInvokedExactlyOnce { geometryDB.putAll(eq(geomEntries)) }
        verifyInvokedExactlyOnce { createdElementsController.putAll(eq(idUpdates.map { ElementKey(it.elementType, it.newElementId) })) }

        sleep(100)
        verifyInvokedExactlyOnce { listener.onUpdated(any(), eq(expectedDeleteKeys)) }
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
        every { nodeDB.getIdsOlderThan(123L) }.returns(nodeKeys.map { it.id })
        every { wayDB.getIdsOlderThan(123L) }.returns(wayKeys.map { it.id })
        every { relationDB.getIdsOlderThan(123L) }.returns(relationKeys.map { it.id })
        every { wayDB.filterNodeIdsWithoutWays(nodeKeys.map { it.id }) }.returns(filteredNodeKeys.map { it.id })
        every { elementDB.deleteAll(wayKeys + relationKeys) }.returns((wayKeys + relationKeys).size)
        every { elementDB.deleteAll(filteredNodeKeys) }.returns(filteredNodeKeys.size)
        every { geometryDB.deleteAll(elementKeys) }.returns(elementKeys.size)
        every { createdElementsController.deleteAll(elementKeys) }.doesNothing()

        val listener = mock(classOf<MapDataController.Listener>())

        controller.addListener(listener)
        controller.deleteOlderThan(123L)

        verifyInvokedExactlyOnce { elementDB.deleteAll(wayKeys + relationKeys) }
        verifyInvokedExactlyOnce { elementDB.deleteAll(filteredNodeKeys) }
        verifyInvokedExactlyOnce { geometryDB.deleteAll(elementKeys) }
        verifyInvokedExactlyOnce { createdElementsController.deleteAll(elementKeys) }

        sleep(100)
        verifyInvokedExactlyOnce { listener.onUpdated(any(), eq(elementKeys)) }
    }

    @Test fun clear() {
        val listener = mock(classOf<MapDataController.Listener>())
        controller.addListener(listener)
        controller.clear()

        verifyInvokedExactlyOnce { elementDB.clear() }
        verifyInvokedExactlyOnce { geometryDB.clear() }
        verifyInvokedExactlyOnce { createdElementsController.clear() }
        verifyInvokedExactlyOnce { listener.onCleared() }
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

        every { elementDB.getAllKeys(bbox) }.returns(emptyList())
        every { geometryDB.getAllEntries(emptyList()) }.returns(emptyList())
        every { geometryCreator.create(any(), any(), any()) }.returns(pGeom())
        every { elementDB.deleteAll(eq(emptySet())) }.returns(0)
        every { geometryDB.deleteAll(eq(emptySet())) }.returns(0)
        every { geometryDB.putAll(eq(geomEntries)) }.doesNothing()
        every { elementDB.putAll(eq(mapData)) }.doesNothing()

        val listener = mock(classOf<MapDataController.Listener>())
        every { listener.onReplacedForBBox(eq(bbox), any()) }.doesNothing()

        controller.addListener(listener)
        controller.putAllForBBox(bbox, mapData)

        verifyInvokedExactlyOnce { elementDB.deleteAll(eq(emptySet())) }
        verifyInvokedExactlyOnce { geometryDB.deleteAll(eq(emptySet())) }
        verifyInvokedExactlyOnce { geometryDB.putAll(eq(geomEntries)) }
        verifyInvokedExactlyOnce { elementDB.putAll(eq(mapData)) }
        verifyInvokedExactlyOnce { listener.onReplacedForBBox(eq(bbox), any()) }
    }
}
