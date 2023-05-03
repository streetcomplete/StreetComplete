package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.download.tiles.asBoundingBoxOfEnclosingTiles
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.geometry.*
import de.westnordost.streetcomplete.testutils.*
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.isCompletelyInside
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions

internal class MapDataCacheTest {

    @Test fun `update puts way`() {
        val way = way(1)
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(way))
        val elementDB: ElementDao = mock()
        on(elementDB.get(ElementType.WAY, 1L)).thenThrow(IllegalStateException())
        assertEquals(way, cache.getElement(ElementType.WAY, 1L) { type, id -> elementDB.get(type, id) })
    }

    @Test fun `update puts relation`() {
        val relation = rel(1)
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(relation))
        val elementDB: ElementDao = mock()
        on(elementDB.get(ElementType.RELATION, 1L)).thenThrow(IllegalStateException())
        assertEquals(relation, cache.getElement(ElementType.RELATION, 1L) { type, id -> elementDB.get(type, id) })
    }

    @Test fun `update puts way geometry`() {
        val p = p(0.0, 0.0)
        val geo = ElementPolylinesGeometry(listOf(listOf(p)), p)

        val cache = getEmptyMapDataCache()
        cache.update(updatedGeometries = listOf(ElementGeometryEntry(ElementType.WAY, 1, geo)))
        val geometryDB: ElementGeometryDao = mock()
        on(geometryDB.get(ElementType.WAY, 1L)).thenThrow(IllegalStateException())
        assertEquals(geo, cache.getGeometry(ElementType.WAY, 1L) { type, id -> geometryDB.get(type, id) })
    }

    @Test fun `update puts relation geometry`() {
        val p = p(0.0, 0.0)
        val geo = ElementPolygonsGeometry(listOf(listOf(p)), p)

        val cache = getEmptyMapDataCache()
        cache.update(updatedGeometries = listOf(ElementGeometryEntry(ElementType.RELATION, 1, geo)))
        val geometryDB: ElementGeometryDao = mock()
        on(geometryDB.get(ElementType.RELATION, 1L)).thenThrow(IllegalStateException())
        assertEquals(geo, cache.getGeometry(ElementType.RELATION, 1L) { type, id -> geometryDB.get(type, id) })
    }

    @Test fun `getElement also caches node if not in spatialCache`() {
        val node = node(1)
        val cache = getEmptyMapDataCache()
        val elementDB: ElementDao = mock()
        on(elementDB.get(ElementType.NODE, 1L)).thenReturn(node).thenReturn(null)
        assertEquals(node, cache.getElement(ElementType.NODE, 1L) { type, id -> elementDB.get(type, id) })
        verify(elementDB).get(ElementType.NODE, 1L)

        // getting a second time does not fetches again
        val elementDB2: ElementDao = mock()
        on(elementDB2.get(ElementType.NODE, 1L)).thenReturn(node).thenReturn(null)
        assertEquals(node, cache.getElement(ElementType.NODE, 1L) { type, id -> elementDB2.get(type, id) })
        verifyNoInteractions(elementDB2)
    }

    @Test fun `getElement fetches and caches way`() {
        val way = way(2L)
        val cache = getEmptyMapDataCache()
        val elementDB: ElementDao = mock()
        on(elementDB.get(ElementType.WAY, 2L)).thenReturn(way).thenThrow(IllegalStateException())
        // get way 2 and verify the fetch function is called, but only once
        assertEquals(way, cache.getElement(ElementType.WAY, 2L) { type, id -> elementDB.get(type, id) })
        verify(elementDB).get(ElementType.WAY, 2L)

        // getting a second time does not fetch again
        assertEquals(way, cache.getElement(ElementType.WAY, 2L) { type, id -> elementDB.get(type, id) })
    }

    @Test fun `getElement fetches and caches relation`() {
        val rel = rel(1L)
        val cache = getEmptyMapDataCache()
        val elementDB: ElementDao = mock()
        on(elementDB.get(ElementType.RELATION, 1L)).thenReturn(rel).thenThrow(IllegalStateException())
        // get rel 1 and verify the fetch function is called, but only once
        assertEquals(rel, cache.getElement(ElementType.RELATION, 1L) { type, id -> elementDB.get(type, id) })
        verify(elementDB).get(ElementType.RELATION, 1L)

        // getting a second time does not fetch again
        assertEquals(rel, cache.getElement(ElementType.RELATION, 1L) { type, id -> elementDB.get(type, id) })
    }

    @Test fun `getGeometry fetches node geometry if not in spatialCache`() {
        val p = p(0.0, 0.0)
        val geo = ElementPointGeometry(p)

        val cache = getEmptyMapDataCache()
        val geometryDB: ElementGeometryDao = mock()
        on(geometryDB.get(ElementType.NODE, 2L)).thenReturn(geo).thenThrow(IllegalStateException())
        // get node 2 and verify the fetch function is called, but only once
        assertEquals(geo, cache.getGeometry(ElementType.NODE, 2L) { type, id -> geometryDB.get(type, id) })
        verify(geometryDB).get(ElementType.NODE, 2L)

        // getting a second time fetches again
        val geometryDB2: ElementGeometryDao = mock()
        on(geometryDB2.get(ElementType.NODE, 2L)).thenReturn(geo).thenThrow(IllegalStateException())
        assertEquals(geo, cache.getGeometry(ElementType.NODE, 2L) { type, id -> geometryDB2.get(type, id) })
        verify(geometryDB2).get(ElementType.NODE, 2L)
    }

    @Test fun `getGeometry fetches and caches way geometry`() {
        val p = p(0.0, 0.0)
        val geo = ElementPolylinesGeometry(listOf(listOf(p)), p)

        val cache = getEmptyMapDataCache()
        val geometryDB: ElementGeometryDao = mock()
        on(geometryDB.get(ElementType.WAY, 2L)).thenReturn(geo).thenThrow(IllegalStateException())
        // get geo and verify the fetch function is called, but only once
        assertEquals(geo, cache.getGeometry(ElementType.WAY, 2L) { type, id -> geometryDB.get(type, id) })
        verify(geometryDB).get(ElementType.WAY, 2L)

        // getting a second time does not fetch again
        assertEquals(geo, cache.getGeometry(ElementType.WAY, 2L) { type, id -> geometryDB.get(type, id) })
    }

    @Test fun `getGeometry fetches and caches relation geometry`() {
        val p = p(0.0, 0.0)
        val geo = ElementPolygonsGeometry(listOf(listOf(p)), p)

        val cache = getEmptyMapDataCache()
        val geometryDB: ElementGeometryDao = mock()
        on(geometryDB.get(ElementType.RELATION, 2L)).thenReturn(geo).thenThrow(IllegalStateException())
        // get way 2 and verify the fetch function is called, but only once
        assertEquals(geo, cache.getGeometry(ElementType.RELATION, 2L) { type, id -> geometryDB.get(type, id) })
        verify(geometryDB).get(ElementType.RELATION, 2L)

        // getting a second time does not fetch again
        assertEquals(geo, cache.getGeometry(ElementType.RELATION, 2L) { type, id -> geometryDB.get(type, id) })
    }

    @Test fun `getNodes doesn't fetch cached nodes`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val node3 = node(3, LatLon(0.0002, 0.0002))
        val nodes = listOf(node1, node2, node3)
        val nodesRect = listOf(node1.position, node2.position, node3.position).enclosingBoundingBox().enclosingTilesRect(16)
        assertTrue(nodesRect.size <= 4) // fits in cache

        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = nodes, bbox = nodesRect.asBoundingBox(16))

        assertTrue(cache.getNodes(nodes.map { it.id }) { throw IllegalStateException() }.containsExactlyInAnyOrder(nodes))
    }

    @Test fun `getNodes fetches only nodes not in cache`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val node3 = node(3, LatLon(0.0002, 0.0002))
        val outsideNode = node(4, LatLon(1.0, 1.0)) // will be put to nodeCache
        val uncachedNode = node(5, LatLon(1.0, 1.0)) // not in update, so needs to be fetched from DB
        val nodes = listOf(node1, node2, node3, outsideNode)
        val allNodes = nodes + uncachedNode
        val nodesRect = listOf(node1.position, node2.position, node3.position).enclosingBoundingBox().enclosingTilesRect(16)
        assertTrue(nodesRect.size <= 4) // fits in cache

        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = nodes, bbox = nodesRect.asBoundingBox(16))

        val nodeDB: NodeDao = mock()
        on(nodeDB.getAll(listOf(uncachedNode.id))).thenReturn(listOf(uncachedNode))
        assertTrue(cache.getNodes(allNodes.map { it.id }) { nodeDB.getAll(it) }.containsExactlyInAnyOrder(allNodes))
        verify(nodeDB).getAll(listOf(uncachedNode.id))
    }

    @Test fun `getNodes does not fetch cached nodes`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val node3 = node(3, LatLon(0.0002, 0.0002))
        val node4 = node(4, LatLon(1.0, 1.0))
        val nodes = listOf(node1, node2, node3, node4)

        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = nodes)

        val nodeDB: NodeDao = mock()
        assertTrue(cache.getNodes(nodes.map { it.id }) { nodeDB.getAll(it) }.containsExactlyInAnyOrder(nodes))
        verifyNoInteractions(nodeDB)
    }

    @Test fun `getNodes fetches formerly cached nodes outside spatial cache after trim`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val node3 = node(3, LatLon(0.0002, 0.0002))
        val node4 = node(4, LatLon(1.0, 1.0))
        val nodes = listOf(node1, node2, node3, node4)

        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = nodes)
        cache.trim(4)

        val nodeDB: NodeDao = mock()
        val nodeIds = nodes.map { it.id }.sorted() // use sorted to avoid issues with order
        on(nodeDB.getAll(nodeIds)).thenReturn(nodes)
        assertTrue(cache.getNodes(nodes.map { it.id }) { nodeDB.getAll(it.sorted()) }.containsExactlyInAnyOrder(nodes))
        verify(nodeDB).getAll(nodeIds)
    }

    @Test fun `getElements doesn't fetch cached elements`() {
        val node1 = node(1)
        val way1 = way(1)
        val way2 = way(2)
        val way3 = way(3)
        val rel1 = rel(1)
        val rel2 = rel(2)
        val elements = listOf(node1, way1, way2, way3, rel1, rel2)

        val cache = getEmptyMapDataCache()
        cache.update(bbox = node1.position.enclosingTilePos(16).asBoundingBox(16))
        cache.update(updatedElements = elements)

        assertTrue(cache.getElements(elements.map { ElementKey(it.type, it.id) }) { throw IllegalStateException() }.containsExactlyInAnyOrder(elements))
    }

    @Test fun `getElements fetches only elements not in cache`() {
        val node1 = node(1)
        val way1 = way(1)
        val way2 = way(2)
        val way3 = way(3)
        val rel1 = rel(1)
        val rel2 = rel(2)
        val elements = listOf(node1, way1, way2, way3, rel1, rel2)
        val cachedElements = listOf(way1, rel1)

        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = cachedElements)
        val keysNotInCache = elements.filterNot { it in cachedElements }.map { ElementKey(it.type, it.id) }.toHashSet()

        val elementDB: ElementDao = mock()
        on(elementDB.getAll(keysNotInCache)).thenReturn(elements.filterNot { it in cachedElements })
        assertTrue(cache.getElements(elements.map { ElementKey(it.type, it.id) }) { elementDB.getAll(it.toHashSet()) }.containsExactlyInAnyOrder(elements))
        verify(elementDB).getAll(keysNotInCache)

        // check whether elements are cached now
        on(elementDB.getAll(listOf(ElementKey(node1.type, node1.id)))).thenReturn(listOf(node1))
        assertTrue(cache.getElements(elements.map { ElementKey(it.type, it.id) }) { elementDB.getAll(it) }.containsExactlyInAnyOrder(elements))
        verifyNoMoreInteractions(elementDB)
    }

    @Test fun `getElements fetches all elements if none are cached`() {
        val node1 = node(1)
        val way1 = way(1)
        val way2 = way(2)
        val way3 = way(3)
        val rel1 = rel(1)
        val rel2 = rel(2)
        val elements = listOf(node1, way1, way2, way3, rel1, rel2)

        val cache = getEmptyMapDataCache()
        val keys = elements.map { ElementKey(it.type, it.id) }.toHashSet()

        val elementDB: ElementDao = mock()
        on(elementDB.getAll(keys)).thenReturn(elements)
        assertTrue(cache.getElements(elements.map { ElementKey(it.type, it.id) }) { elementDB.getAll(it.toHashSet()) }.containsExactlyInAnyOrder(elements))
        verify(elementDB).getAll(keys)

        // check whether elements are cached now
        on(elementDB.getAll(listOf(ElementKey(node1.type, node1.id)))).thenReturn(listOf(node1))
        assertTrue(cache.getElements(elements.map { ElementKey(it.type, it.id) }) { elementDB.getAll(it) }.containsExactlyInAnyOrder(elements))
        verifyNoMoreInteractions(elementDB)
    }

    @Test fun `getGeometries doesn't fetch cached geometries`() {
        val p = p(0.0, 0.0)
        val node = node(1L, p)
        val geo1 = ElementPolylinesGeometry(listOf(listOf(p)), p)
        val geo2 = ElementPolygonsGeometry(listOf(listOf(p)), p)
        val nodeGeo = ElementPointGeometry(p)
        val entry1 = ElementGeometryEntry(ElementType.RELATION, 1L, geo1)
        val entry2 = ElementGeometryEntry(ElementType.WAY, 1L, geo2)
        val nodeEntry = ElementGeometryEntry(ElementType.NODE, 1L, nodeGeo)
        val entries = listOf(entry1, entry2, nodeEntry)

        val cache = getEmptyMapDataCache()
        cache.update(bbox = node.position.enclosingTilePos(16).asBoundingBox(16))
        cache.update(updatedElements = listOf(node), updatedGeometries = entries)

        assertTrue(cache.getGeometries(entries.map { ElementKey(it.elementType, it.elementId) }) { throw IllegalStateException() }.containsExactlyInAnyOrder(entries))
    }

    @Test fun `getGeometries fetches only geometries not in cache`() {
        val p = p(0.0, 0.0)
        val node1 = node(1L, p)
        val geo1 = ElementPolylinesGeometry(listOf(listOf(p)), p)
        val geo2 = ElementPolygonsGeometry(listOf(listOf(p)), p)
        val nodeGeo = ElementPointGeometry(p)
        val entry1 = ElementGeometryEntry(ElementType.RELATION, 1L, geo1)
        val entry2 = ElementGeometryEntry(ElementType.WAY, 1L, geo2)
        val nodeEntry = ElementGeometryEntry(ElementType.NODE, 1L, nodeGeo)
        val entries = listOf(entry1, entry2, nodeEntry)
        val cachedEntries = listOf(entry1)

        val cache = getEmptyMapDataCache()
        cache.update(updatedGeometries = cachedEntries)
        val keysNotInCache = entries.filterNot { it in cachedEntries }.map { ElementKey(it.elementType, it.elementId) }.toHashSet()

        val geometryDB: ElementGeometryDao = mock()
        on(geometryDB.getAllEntries(keysNotInCache)).thenReturn(entries.filterNot { it in cachedEntries })
        assertTrue(cache.getGeometries(entries.map { ElementKey(it.elementType, it.elementId) }) { geometryDB.getAllEntries(it.toHashSet()) }.containsExactlyInAnyOrder(entries))
        verify(geometryDB).getAllEntries(keysNotInCache)

        // check whether geometries except nodeEntry are cached now
        on(geometryDB.getAllEntries(listOf(ElementKey(node1.type, node1.id)))).thenReturn(listOf(nodeEntry))
        assertTrue(cache.getGeometries(entries.map { ElementKey(it.elementType, it.elementId) }) { geometryDB.getAllEntries(it) }.containsExactlyInAnyOrder(entries))
        verify(geometryDB).getAllEntries(listOf(ElementKey(node1.type, node1.id)))
    }

    @Test fun `getGeometries fetches all geometries if none are cached`() {
        val p = p(0.0, 0.0)
        val node1 = node(1L, p)
        val geo1 = ElementPolylinesGeometry(listOf(listOf(p)), p)
        val geo2 = ElementPolygonsGeometry(listOf(listOf(p)), p)
        val nodeGeo = ElementPointGeometry(p)
        val entry1 = ElementGeometryEntry(ElementType.RELATION, 1L, geo1)
        val entry2 = ElementGeometryEntry(ElementType.WAY, 1L, geo2)
        val nodeEntry = ElementGeometryEntry(ElementType.NODE, 1L, nodeGeo)
        val entries = listOf(entry1, entry2, nodeEntry)

        val cache = getEmptyMapDataCache()
        val keys = entries.map { ElementKey(it.elementType, it.elementId) }.toHashSet()

        val geometryDB: ElementGeometryDao = mock()
        on(geometryDB.getAllEntries(keys)).thenReturn(entries)
        assertTrue(cache.getGeometries(entries.map { ElementKey(it.elementType, it.elementId) }) { geometryDB.getAllEntries(it.toHashSet()) }.containsExactlyInAnyOrder(entries))
        verify(geometryDB).getAllEntries(keys)

        // check whether geometries except nodeEntry are cached now
        on(geometryDB.getAllEntries(listOf(ElementKey(node1.type, node1.id)))).thenReturn(listOf(nodeEntry))
        assertTrue(cache.getGeometries(entries.map { ElementKey(it.elementType, it.elementId) }) { geometryDB.getAllEntries(it) }.containsExactlyInAnyOrder(entries))
        verify(geometryDB).getAllEntries(listOf(ElementKey(node1.type, node1.id)))
    }

    @Test fun `update removes elements`() {
        val node = node(1, LatLon(0.0, 0.0))
        val way = way(2)
        val rel = rel(3)
        val nodeKey = ElementKey(ElementType.NODE, 1L)
        val wayKey = ElementKey(ElementType.WAY, 2L)
        val relationKey = ElementKey(ElementType.RELATION, 3L)
        val cache = MapDataCache(16, 4, 10, { listOf(node) to emptyList() }, { emptyList() })
        cache.getMapDataWithGeometry(node.position.enclosingTilePos(16).asBoundingBox(16))
        cache.update(updatedElements = listOf(way, rel))
        assertTrue(
            cache.getElements(listOf(nodeKey, wayKey, relationKey)) { emptyList() }
                .containsExactlyInAnyOrder(listOf(node, way, rel))
        )
        cache.update(deletedKeys = listOf(nodeKey, wayKey, relationKey))
        assertNull(cache.getElement(ElementType.NODE, 1L) { _, _ -> null })
        assertNull(cache.getElement(ElementType.WAY, 2L) { _, _ -> null })
        assertNull(cache.getElement(ElementType.RELATION, 3L) { _, _ -> null })
    }

    @Test fun `getWaysForNode caches from db`() {
        val way1 = way(1, nodes = listOf(1L, 2L))
        val way2 = way(2, nodes = listOf(3L, 1L))
        val way3 = way(3, nodes = listOf(3L, 2L))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(way1, way2, way3))
        val wayDB: WayDao = mock()
        on(wayDB.getAllForNode(1L)).thenReturn(listOf(way1, way2)).thenThrow(IllegalStateException())
        // fetches from cache if we didn't put the node
        assertTrue(cache.getWaysForNode(1L) { wayDB.getAllForNode(it) }.containsExactlyInAnyOrder(listOf(way1, way2)))
        verify(wayDB).getAllForNode(1L)
        // now we have it cached
        assertTrue(cache.getWaysForNode(1L) { emptyList() }.containsExactlyInAnyOrder(listOf(way1, way2)))
    }

    @Test fun `getWaysForNode is cached for nodes inside bbox after updating with bbox`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val node3 = node(3, LatLon(0.0002, 0.0002))
        val nodesRect = listOf(node1.position, node2.position, node3.position).enclosingBoundingBox().enclosingTilesRect(16)
        assertTrue(nodesRect.size <= 4) // fits in cache
        val way1 = way(1, nodes = listOf(1L, 2L))
        val way2 = way(2, nodes = listOf(3L, 1L))
        val way3 = way(3, nodes = listOf(3L, 2L))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(node1, node2, node3, way1, way2, way3), bbox = nodesRect.asBoundingBox(16))

        assertTrue(cache.getWaysForNode(1L) { emptyList() }.containsExactlyInAnyOrder(listOf(way1, way2)))
        assertTrue(cache.getWaysForNode(2L) { emptyList() }.containsExactlyInAnyOrder(listOf(way1, way3)))
        assertTrue(cache.getWaysForNode(2L) { emptyList() }.containsExactlyInAnyOrder(listOf(way1, way3)))
    }

    @Test fun `getWaysForNode returns way after adding node id`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val node3 = node(3, LatLon(0.0002, 0.0002))
        val nodesRect = listOf(node1.position, node2.position, node3.position).enclosingBoundingBox().enclosingTilesRect(16)
        assertTrue(nodesRect.size <= 4) // fits in cache
        val way1 = way(1, nodes = listOf(1L, 2L))
        val way2 = way(2, nodes = listOf(3L, 1L))
        val way3 = way(3, nodes = listOf(3L, 2L))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(node1, node2, node3, way1, way2, way3), bbox = nodesRect.asBoundingBox(16))
        assertTrue(cache.getWaysForNode(1L) { emptyList() }.containsExactlyInAnyOrder(listOf(way1, way2)))

        val way3updated = way(3, nodes = listOf(3L, 2L, 1L))
        cache.update(updatedElements = listOf(way3updated))
        assertTrue(cache.getWaysForNode(1L) { emptyList() }.containsExactlyInAnyOrder(listOf(way1, way2, way3updated)))
    }

    @Test fun `getWaysForNode does not return just deleted way`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val node3 = node(3, LatLon(0.0002, 0.0002))
        val nodesRect = listOf(node1.position, node2.position, node3.position).enclosingBoundingBox().enclosingTilesRect(16)
        assertTrue(nodesRect.size <= 4) // fits in cache
        val way1 = way(1, nodes = listOf(1L, 2L))
        val way2 = way(2, nodes = listOf(3L, 1L))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(node1, node2, node3, way1, way2), bbox = nodesRect.asBoundingBox(16))
        assertTrue(cache.getWaysForNode(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way1, way2)))
        assertTrue(cache.getWaysForNode(2L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way1)))
        assertTrue(cache.getWaysForNode(3L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way2)))

        cache.update(deletedKeys = listOf(ElementKey(way2.type, way2.id)))
        assertTrue(cache.getWaysForNode(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way1)))
        assertTrue(cache.getWaysForNode(2L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way1)))
        assertTrue(cache.getWaysForNode(3L) { throw IllegalStateException() }.isEmpty())
    }

    @Test fun `getWaysForNode returns updated way that now does contain the node but before it didn't`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val node3 = node(3, LatLon(0.0002, 0.0002))
        val nodesRect = listOf(node1.position, node2.position, node3.position).enclosingBoundingBox().enclosingTilesRect(16)
        assertTrue(nodesRect.size <= 4) // fits in cache
        val way1 = way(1, nodes = listOf(1L, 2L))
        val way2 = way(2, nodes = listOf(3L, 1L))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(node1, node2, node3, way1, way2), bbox = nodesRect.asBoundingBox(16))
        assertTrue(cache.getWaysForNode(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way1, way2)))
        assertTrue(cache.getWaysForNode(2L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way1)))
        assertTrue(cache.getWaysForNode(3L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way2)))

        val way2updated = way(2, nodes = listOf(3L, 2L, 1L))
        cache.update(updatedElements = listOf(way2updated))
        assertTrue(cache.getWaysForNode(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way1, way2updated)))
        assertTrue(cache.getWaysForNode(2L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way1, way2updated)))
        assertTrue(cache.getWaysForNode(3L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way2updated)))
    }

    @Test fun `getWaysForNode doesn't return updated way that used to contain the node before but now it doesn't`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val node3 = node(3, LatLon(0.0002, 0.0002))
        val nodesRect = listOf(node1.position, node2.position, node3.position).enclosingBoundingBox().enclosingTilesRect(16)
        assertTrue(nodesRect.size <= 4) // fits in cache
        val way1 = way(1, nodes = listOf(1L, 2L))
        val way2 = way(2, nodes = listOf(3L, 1L))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(node1, node2, node3, way1, way2), bbox = nodesRect.asBoundingBox(16))
        assertTrue(cache.getWaysForNode(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way1, way2)))
        assertTrue(cache.getWaysForNode(2L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way1)))
        assertTrue(cache.getWaysForNode(3L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way2)))

        val way2updated = way(2, nodes = listOf(2L))
        cache.update(updatedElements = listOf(way2updated))
        assertTrue(cache.getWaysForNode(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way1)))
        assertTrue(cache.getWaysForNode(2L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way1, way2updated)))
        assertTrue(cache.getWaysForNode(3L) { throw IllegalStateException() }.isEmpty())
    }

    @Test fun `getRelationsForNode and Way returns correct relations`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val nodesRect = listOf(node1.position, node2.position).enclosingBoundingBox().enclosingTilesRect(16)
        assertTrue(nodesRect.size <= 4) // fits in cache
        val way1 = way(1, nodes = listOf(1L, 2L))
        val rel1 = rel(1, members = listOf(RelationMember(ElementType.NODE, 1L, "")))
        val rel2 = rel(2, members = listOf(RelationMember(ElementType.WAY, 1L, "")))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(node1, node2, way1, rel1, rel2), bbox = nodesRect.asBoundingBox(16))
        assertTrue(cache.getRelationsForNode(1L) { emptyList() }.containsExactlyInAnyOrder(listOf(rel1)))
        assertTrue(cache.getRelationsForWay(1L) { emptyList() }.containsExactlyInAnyOrder(listOf(rel2)))

        val rel2updated = rel(2, members = listOf(RelationMember(ElementType.NODE, 1L, "")))
        cache.update(updatedElements = listOf(rel2updated))
        assertTrue(cache.getRelationsForNode(1L) { emptyList() }.containsExactlyInAnyOrder(listOf(rel1, rel2updated)))
        assertTrue(cache.getRelationsForWay(1L) { emptyList() }.isEmpty())
    }

    @Test fun `getRelationsForNode and Way does not return just deleted relation`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val nodesRect = listOf(node1.position, node2.position).enclosingBoundingBox().enclosingTilesRect(16)
        assertTrue(nodesRect.size <= 4) // fits in cache
        val way1 = way(1, nodes = listOf(1L, 2L))
        val rel1 = rel(1, members = listOf(RelationMember(ElementType.NODE, 1L, "")))
        val rel2 = rel(2, members = listOf(RelationMember(ElementType.NODE, 1L, ""), RelationMember(ElementType.WAY, 1L, "")))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(node1, node2, way1, rel1, rel2), bbox = nodesRect.asBoundingBox(16))
        assertTrue(cache.getRelationsForNode(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(rel1, rel2)))
        assertTrue(cache.getRelationsForWay(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(rel2)))

        cache.update(deletedKeys = listOf(ElementKey(rel2.type, rel2.id)))
        assertTrue(cache.getRelationsForNode(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(rel1)))
        assertTrue(cache.getRelationsForWay(1L) { throw IllegalStateException() }.isEmpty())
    }

    @Test fun `getRelationsForNode and Way returns updated relation that now does contain the element but before it didn't`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val nodesRect = listOf(node1.position, node2.position).enclosingBoundingBox().enclosingTilesRect(16)
        assertTrue(nodesRect.size <= 4) // fits in cache
        val way1 = way(1, nodes = listOf(1L, 2L))
        val rel1 = rel(1, members = listOf(RelationMember(ElementType.NODE, 1L, "")))
        val rel2 = rel(2, members = listOf(RelationMember(ElementType.WAY, 1L, "")))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(node1, node2, way1, rel1, rel2), bbox = nodesRect.asBoundingBox(16))
        assertTrue(cache.getRelationsForNode(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(rel1)))
        assertTrue(cache.getRelationsForWay(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(rel2)))

        val rel1updated = rel(1, members = listOf(RelationMember(ElementType.NODE, 1L, ""), RelationMember(ElementType.WAY, 1L, "")))
        cache.update(updatedElements = listOf(rel1updated))
        assertTrue(cache.getRelationsForNode(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(rel1updated)))
        assertTrue(cache.getRelationsForWay(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(rel2, rel1updated)))
    }

    @Test fun `getRelationsForNode and Way doesn't return updated relation that used to contain the element before but now it doesn't`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val nodesRect = listOf(node1.position, node2.position).enclosingBoundingBox().enclosingTilesRect(16)
        assertTrue(nodesRect.size <= 4) // fits in cache
        val way1 = way(1, nodes = listOf(1L, 2L))
        val rel1 = rel(1, members = listOf(RelationMember(ElementType.NODE, 1L, ""), RelationMember(ElementType.WAY, 1L, "")))
        val rel2 = rel(2, members = listOf(RelationMember(ElementType.WAY, 1L, ""), RelationMember(ElementType.NODE, 1L, "")))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(node1, node2, way1, rel1, rel2), bbox = nodesRect.asBoundingBox(16))
        assertTrue(cache.getRelationsForNode(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(rel1, rel2)))
        assertTrue(cache.getRelationsForWay(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(rel1, rel2)))

        val rel1updated = rel(1, members = listOf(RelationMember(ElementType.WAY, 1L, "")))
        val rel2updated = rel(2, members = listOf(RelationMember(ElementType.NODE, 1L, "")))
        cache.update(updatedElements = listOf(rel1updated, rel2updated))
        assertTrue(cache.getRelationsForNode(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(rel2updated)))
        assertTrue(cache.getRelationsForWay(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(rel1updated)))
    }

    @Test fun `cache returns nodes outside the BBox if part of ways`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val node3 = node(3, LatLon(0.0002, 0.0002))
        val nodes = listOf(node1, node2, node3)
        val outsideNode = node(4, LatLon(1.0, 1.0))
        val fullyOutsideNode = node(5, LatLon(2.0, 2.0))
        val nodesRect = listOf(node1.position, node2.position, node3.position).enclosingBoundingBox().enclosingTilesRect(16)
        assertTrue(nodesRect.size <= 4) // fits in cache

        val way1 = way(1, nodes = listOf(1L, 2L))
        val way2 = way(2, nodes = listOf(3L, 1L))
        val way3 = way(3, nodes = listOf(3L, 2L, 4L))
        val ways = listOf(way1, way2, way3)
        val outsideWay = way(4, nodes = listOf(4L, 5L))
        val rel1 = rel(1, members = listOf(RelationMember(ElementType.NODE, 1L, ""), RelationMember(ElementType.WAY, 5L, "")))
        val outsideRel = rel(2, members = listOf(RelationMember(ElementType.NODE, 4L, ""), RelationMember(ElementType.WAY, 5L, "")))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = nodes + ways + listOf(outsideNode, fullyOutsideNode, outsideWay, rel1, outsideRel), bbox = nodesRect.asBoundingBox(16))

        // outsideNode is expected because it's part of returned way1
        val expectedMapData = MutableMapDataWithGeometry(nodes + ways + rel1 + outsideNode, emptyList())
        // node geometries get created when getting the data
        nodes.forEach { expectedMapData.putGeometry(it.type, it.id, ElementPointGeometry(it.position)) }
        expectedMapData.putGeometry(outsideNode.type, outsideNode.id, ElementPointGeometry(outsideNode.position))
        // way and relation geometries are also put, though null
        ways.forEach { expectedMapData.putGeometry(it.type, it.id, null) }
        expectedMapData.putGeometry(rel1.type, rel1.id, null)
        expectedMapData.boundingBox = nodesRect.asBoundingBox(16)
        assertEquals(expectedMapData, cache.getMapDataWithGeometry(nodesRect.asBoundingBox(16)))
    }

    @Test fun `trim removes everything not referenced by spatialCache`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val node3 = node(3, LatLon(0.0002, 0.0002))
        val nodes = listOf(node1, node2, node3)
        val outsideNode = node(4, LatLon(1.0, 1.0))
        val nodesRect = listOf(node1.position, node2.position, node3.position).enclosingBoundingBox().enclosingTilesRect(16)
        assertTrue(nodesRect.size <= 4) // fits in cache

        val way1 = way(1, nodes = listOf(1L, 2L))
        val way2 = way(2, nodes = listOf(3L, 1L))
        val way3 = way(3, nodes = listOf(3L, 2L, 4L))
        val ways = listOf(way1, way2, way3)
        val outsideWay = way(4, nodes = listOf(4L, 5L))
        val rel1 = rel(1, members = listOf(RelationMember(ElementType.NODE, 1L, ""), RelationMember(ElementType.WAY, 5L, "")))
        val outsideRel = rel(2, members = listOf(RelationMember(ElementType.NODE, 4L, ""), RelationMember(ElementType.WAY, 5L, "")))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = nodes + ways + listOf(outsideNode, outsideWay, rel1, outsideRel), bbox = nodesRect.asBoundingBox(16))

        // outsideNode is expected because it's part of returned way1
        val expectedMapData = MutableMapDataWithGeometry(nodes + ways + rel1 + outsideNode, emptyList())
        // node geometries get created when getting the data
        nodes.forEach { expectedMapData.putGeometry(it.type, it.id, ElementPointGeometry(it.position)) }
        expectedMapData.putGeometry(outsideNode.type, outsideNode.id, ElementPointGeometry(outsideNode.position))
        // way and relation geometries are also put, though null
        ways.forEach { expectedMapData.putGeometry(it.type, it.id, null) }
        expectedMapData.putGeometry(rel1.type, rel1.id, null)
        expectedMapData.boundingBox = nodesRect.asBoundingBox(16)
        assertEquals(expectedMapData, cache.getMapDataWithGeometry(nodesRect.asBoundingBox(16)))

        assertEquals(outsideNode, cache.getElement(ElementType.NODE, 4L) { _, _ -> null }) // node in nodeCache
        assertEquals(outsideWay, cache.getElement(ElementType.WAY, 4L) { _, _ -> null })
        assertEquals(outsideRel, cache.getElement(ElementType.RELATION, 2L) { _, _ -> null })
        cache.trim(4)
        // outside node, way and relation removed
        assertEquals(null, cache.getElement(ElementType.NODE, 4L) { _, _ -> null })
        assertEquals(null, cache.getElement(ElementType.WAY, 4L) { _, _ -> null })
        assertEquals(null, cache.getElement(ElementType.RELATION, 2L) { _, _ -> null })
        // but relation partially in spatial cache area is not removed
        assertEquals(rel1, cache.getElement(ElementType.RELATION, 1L) { _, _ -> null })
    }

    @Test fun `add relation with members already in spatial cache`() {
        val node = node(1, LatLon(0.0, 0.0))
        val nodeRectBbox = node.position.enclosingBoundingBox(0.1).asBoundingBoxOfEnclosingTiles(16)

        val way = way(1, nodes = listOf(1L, 2L))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(way, node), bbox = nodeRectBbox)

        val rel1 = rel(1, members = listOf(RelationMember(ElementType.NODE, 1, "")))
        val rel2 = rel(2, members = listOf(RelationMember(ElementType.WAY, 1, "")))

        cache.update(updatedElements = listOf(rel1, rel2))

        assertEquals(listOf(rel1), cache.getRelationsForNode(1) { throw(IllegalStateException()) })
        assertEquals(listOf(rel2), cache.getRelationsForWay(1) { throw(IllegalStateException()) })
    }

    @Test fun `clear clears`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val nodesRect = listOf(node1.position, node2.position).enclosingBoundingBox().enclosingTilesRect(16)
        val way1 = way(1, nodes = listOf(1L, 2L))
        val rel1 = rel(1, members = listOf(RelationMember(ElementType.NODE, 1L, "")))
        val rel2 = rel(2, members = listOf(RelationMember(ElementType.WAY, 1L, "")))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(node1, node2, way1, rel1, rel2), bbox = nodesRect.asBoundingBox(16))

        // not empty
        assertTrue(cache.getRelationsForNode(1L) { emptyList() }.containsExactlyInAnyOrder(listOf(rel1)))
        cache.clear()
        assertEquals(emptyList<Relation>(), cache.getRelationsForNode(1L) { emptyList() })
        assertEquals(emptyList<Element>(), cache.getElements(listOf(node1, node2, way1, rel1, rel2).map { ElementKey(it.type, it.id) }) { emptyList() })
    }

    @Test fun `update doesn't create waysByNodeId entry if node is not in spatialCache`() {
        val way1 = way(1, nodes = listOf(1L, 2L))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(way1))

        val wayDB: WayDao = mock()
        on(wayDB.getAllForNode(1L)).thenReturn(listOf(way1))
        assertTrue(cache.getWaysForNode(1L) { wayDB.getAllForNode(it) }.containsExactlyInAnyOrder(listOf(way1)))
        verify(wayDB).getAllForNode(1L) // was fetched from cache

        on(wayDB.getAllForNode(2L)).thenReturn(listOf(way1))
        assertTrue(cache.getWaysForNode(2L) { wayDB.getAllForNode(it) }.containsExactlyInAnyOrder(listOf(way1)))
        verify(wayDB).getAllForNode(2L) // was fetched from cache
    }

    @Test fun `update does create waysByNodeId entry if node is in spatialCache`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val node3 = node(3, LatLon(0.0002, 0.0002))
        val nodes = listOf(node1, node2, node3)
        val nodesRect = listOf(node1.position, node2.position, node3.position).enclosingBoundingBox().enclosingTilesRect(16)
        assertTrue(nodesRect.size <= 4) // fits in cache

        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = nodes, bbox = nodesRect.asBoundingBox(16))

        val way1 = way(1, nodes = listOf(1L, 2L))
        cache.update(updatedElements = listOf(way1))

        assertTrue(cache.getWaysForNode(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way1)))
        assertTrue(cache.getWaysForNode(2L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way1)))
    }

    @Test fun `update does add way to waysByNodeId entry if entry already exists`() {
        val way1 = way(1, nodes = listOf(1L, 2L))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(way1))

        val wayDB: WayDao = mock()
        on(wayDB.getAllForNode(1L)).thenReturn(listOf(way1))
        assertTrue(cache.getWaysForNode(1L) { wayDB.getAllForNode(it) }.containsExactlyInAnyOrder(listOf(way1)))
        verify(wayDB).getAllForNode(1L) // was fetched from cache

        val way2 = way(2, nodes = listOf(1L, 2L))
        cache.update(updatedElements = listOf(way2))
        assertTrue(cache.getWaysForNode(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(way1, way2)))

        on(wayDB.getAllForNode(2L)).thenReturn(listOf(way1, way2))
        assertTrue(cache.getWaysForNode(2L) { wayDB.getAllForNode(it) }.containsExactlyInAnyOrder(listOf(way1, way2)))
        verify(wayDB).getAllForNode(2L) // was fetched from cache
    }

    @Test fun `update doesn't create relationsByElementKey entry if element is not referenced by spatialCache`() {
        val node1 = node(1)
        val node2 = node(2)
        val way1 = way(1, nodes = listOf(1L))
        val rel1 = rel(1, members = listOf(RelationMember(ElementType.NODE, 2L, ""), RelationMember(ElementType.WAY, 1L, "")))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(node1, node2, way1, rel1))

        val relationDB: RelationDao = mock()
        on(relationDB.getAllForNode(2L)).thenReturn(listOf(rel1))
        on(relationDB.getAllForWay(1L)).thenReturn(listOf(rel1))
        assertTrue(cache.getRelationsForNode(2L) { relationDB.getAllForNode(it) }.containsExactlyInAnyOrder(listOf(rel1)))
        verify(relationDB).getAllForNode(2L) // was fetched from cache
        assertTrue(cache.getRelationsForWay(1L) { relationDB.getAllForWay(it) }.containsExactlyInAnyOrder(listOf(rel1)))
        verify(relationDB).getAllForWay(1L) // was fetched from cache
    }

    @Test fun `update does create relationsByElementKey entry if element is referenced by spatialCache`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val node3 = node(3, LatLon(0.0002, 0.0002))
        val nodes = listOf(node1, node2, node3)
        val nodesRect = listOf(node1.position, node2.position, node3.position).enclosingBoundingBox().enclosingTilesRect(16)
        assertTrue(nodesRect.size <= 4) // fits in cache

        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = nodes, bbox = nodesRect.asBoundingBox(16))

        val way1 = way(1, nodes = listOf(1L, 2L))
        val rel1 = rel(1, members = listOf(RelationMember(ElementType.NODE, 3L, ""), RelationMember(ElementType.WAY, 1L, "")))
        cache.update(updatedElements = listOf(way1, rel1))

        assertTrue(cache.getRelationsForNode(3L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(rel1)))
        assertTrue(cache.getRelationsForWay(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(rel1)))
    }

    @Test fun `update does add way to relationsByElementKey entry if entry already exists`() {
        val rel1 = rel(1, members = listOf(RelationMember(ElementType.WAY, 1L, "")))
        val cache = getEmptyMapDataCache()
        cache.update(updatedElements = listOf(rel1))

        val relationDB: RelationDao = mock()
        on(relationDB.getAllForWay(1L)).thenReturn(listOf(rel1))
        assertTrue(cache.getRelationsForWay(1L) { relationDB.getAllForWay(it) }.containsExactlyInAnyOrder(listOf(rel1)))
        verify(relationDB).getAllForWay(1L) // was fetched from cache

        val rel2 = rel(2, members = listOf(RelationMember(ElementType.NODE, 3L, ""), RelationMember(ElementType.WAY, 1L, "")))
        cache.update(updatedElements = listOf(rel2))
        assertTrue(cache.getRelationsForWay(1L) { throw IllegalStateException() }.containsExactlyInAnyOrder(listOf(rel1, rel2)))

        on(relationDB.getAllForNode(3L)).thenReturn(listOf(rel2))
        assertTrue(cache.getRelationsForNode(3L) { relationDB.getAllForNode(it) }.containsExactlyInAnyOrder(listOf(rel2)))
        verify(relationDB).getAllForNode(3L) // was fetched from cache
    }

    @Test fun `getMapDataWithGeometry fetches all and caches if nothing is cached`() {
        val node1 = node(1, LatLon(0.0, 0.0)) // TilePos(x=32768, y=32768)
        val node2 = node(2, LatLon(0.0001, 0.0001)) // TilePos(x=32768, y=32767)
        val node3 = node(3, LatLon(-0.0001, 0.0001)) // TilePos(x=32768, y=32768)
        val nodesBBox = listOf(node1.position, node2.position, node3.position).enclosingBoundingBox()
        val nodesRect = nodesBBox.enclosingTilesRect(16)
        assertTrue(nodesRect.size == 2)
        val way1 = way(1, nodes = listOf(1L, 2L))
        val way2 = way(2, nodes = listOf(3L, 1L))
        val way3 = way(3, nodes = listOf(3L, 2L))
        val rel1 = rel(1, members = listOf(RelationMember(ElementType.NODE, 1L, "")))
        val rel2 = rel(2, members = listOf(RelationMember(ElementType.WAY, 1L, "")))
        val elementsInsideBBox = listOf(node1, node2, node3, way1, way2, way3, rel1, rel2)
        val elementDB: ElementDao = mock()
        on(elementDB.getAll(nodesRect.asBoundingBox(16))).thenReturn(elementsInsideBBox)
        val cache = MapDataCache(16, 4, 10, { elementDB.getAll(it) to emptyList() }, { emptyList() })

        val expectedMapData = MutableMapDataWithGeometry().apply {
            listOf(node1, node2, node3).forEach { put(it, ElementPointGeometry(it.position)) }
            listOf(way1, way2, way3, rel1, rel2).forEach { put(it, null) }
            boundingBox = nodesBBox
        }
        assertEquals(expectedMapData, cache.getMapDataWithGeometry(nodesBBox))
        verify(elementDB).getAll(nodesRect.asBoundingBox(16))
        // second time it's cached
        assertEquals(expectedMapData, cache.getMapDataWithGeometry(nodesBBox))
        verifyNoMoreInteractions(elementDB)
    }

    @Test fun `getMapDataWithGeometry fetches only part if something is already cached`() {
        val node1 = node(1, LatLon(0.0, 0.0)) // TilePos(x=32768, y=32768)
        val node2 = node(2, LatLon(0.0001, 0.0001)) // TilePos(x=32768, y=32767)
        val node3 = node(3, LatLon(-0.0001, 0.0001)) // TilePos(x=32768, y=32768)
        val nodesBBox = listOf(node1.position, node2.position, node3.position).enclosingBoundingBox()
        val nodesRect = nodesBBox.enclosingTilesRect(16)
        val node1rect = node1.position.enclosingTilePos(16)
        val node2rect = node2.position.enclosingTilePos(16)
        assertTrue(nodesRect.size == 2)
        val way1 = way(1, nodes = listOf(2L))
        val way2 = way(2, nodes = listOf(3L, 1L))
        val way3 = way(3, nodes = listOf(3L, 2L))
        val rel1 = rel(1, members = listOf(RelationMember(ElementType.NODE, 1L, "")))
        val rel2 = rel(2, members = listOf(RelationMember(ElementType.WAY, 1L, "")))
        val elementDB: ElementDao = mock()
        on(elementDB.getAll(node1rect.asBoundingBox(16))).thenReturn(listOf(node1, node3, way2, way3, rel1))
        val cache = MapDataCache(16, 4, 10, { elementDB.getAll(it) to emptyList() }, { emptyList() })

        cache.update(updatedElements = listOf(node2, way1, rel2), bbox = node2rect.asBoundingBox(16))

        val expectedMapData = MutableMapDataWithGeometry().apply {
            listOf(node1, node2, node3).forEach { put(it, ElementPointGeometry(it.position)) }
            listOf(way1, way2, way3, rel1, rel2).forEach { put(it, null) }
            boundingBox = nodesBBox
        }
        assertEquals(expectedMapData, cache.getMapDataWithGeometry(nodesBBox))
        verify(elementDB).getAll(node1rect.asBoundingBox(16))
        // second time it's cached
        assertEquals(expectedMapData, cache.getMapDataWithGeometry(nodesBBox))
        verifyNoMoreInteractions(elementDB)
    }

    @Test fun `getMapDataWithGeometry fetches nothing if all is cached`() {
        val node1 = node(1, LatLon(0.0, 0.0))
        val node2 = node(2, LatLon(0.0001, 0.0001))
        val node3 = node(3, LatLon(-0.0001, 0.0001))
        val nodesBBox = listOf(node1.position, node2.position, node3.position).enclosingBoundingBox()
        val nodesRect = nodesBBox.enclosingTilesRect(16)
        assertEquals(2, nodesRect.size)
        val way1 = way(1, nodes = listOf(1L, 2L))
        val way2 = way(2, nodes = listOf(3L, 1L))
        val way3 = way(3, nodes = listOf(3L, 2L))
        val rel1 = rel(1, members = listOf(RelationMember(ElementType.NODE, 1L, "")))
        val rel2 = rel(2, members = listOf(RelationMember(ElementType.WAY, 1L, "")))
        val elementsInsideBBox = listOf(node1, node2, node3, way1, way2, way3, rel1, rel2)
        val outsideNode = node(4, LatLon(1.0, 1.0))
        val outsideWay = way(4)
        val outsideRel = rel(3)
        val cache = getEmptyMapDataCache()

        cache.update(updatedElements = elementsInsideBBox + outsideNode + outsideWay + outsideRel, bbox = nodesRect.asBoundingBox(16))
        assertTrue(cache.getMapDataWithGeometry(nodesBBox).toList().containsExactlyInAnyOrder(elementsInsideBBox))
    }

    @Test fun `cache is resistant to unexpected spatial cache tile removal`() {
        // see https://github.com/streetcomplete/StreetComplete/pull/4985 and the linked issue comment
        val x = 2
        val y = 5
        val rect = TilesRect(x, y, x, y + 1).asBoundingBox(16)
        val rect2 = TilesRect(x, y + 1, x, y + 2).asBoundingBox(16)
        val node1 = node(1, LatLon(rect.min.latitude + 0.00001, rect.min.longitude + 0.00001)) // node in rect and rect2
        val node2 = node(2, LatLon(rect.max.latitude - 0.00001, rect.max.longitude - 0.00001)) // node in rect only
        val cache = MapDataCache(16, 24, 10, {
            val elements = when {
                it == rect -> listOf(node1, node2) // may not be true if the bbox is extended because of the tilesRect issue (happens for x=2, y=5, but not for x=2, y=1)
                rect.isCompletelyInside(it) -> listOf(node1, node2) // fix for above
                it == TilesRect(x, y + 1, x, y + 1).asBoundingBox(16) -> listOf(node1)
                else -> emptyList()
            }
            elements to emptyList()
        }, { emptyList() })
        // fill cache
        assertTrue(cache.getMapDataWithGeometry(rect).toList().containsExactlyInAnyOrder(listOf(node1, node2)))

        // trigger the tilesRect issue, which may remove TilesRect(x, y + 1, x, y + 1) (only for some x and y values)
        assertTrue(cache.getMapDataWithGeometry(rect2).toList().containsExactlyInAnyOrder(listOf(node1)))

        // cache should still return the same result as initially, fetching TilesRect(x, y + 1, x, y + 1) if necessary
        assertTrue(cache.getMapDataWithGeometry(rect).toList().containsExactlyInAnyOrder(listOf(node1, node2)))
    }
}

private fun getEmptyMapDataCache() = MapDataCache(16, 4, 10, { emptyList<Element>() to emptyList() }, { emptyList() })
