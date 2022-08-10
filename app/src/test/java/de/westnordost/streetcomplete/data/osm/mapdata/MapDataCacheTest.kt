package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.download.tiles.enclosingTilePos
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.verify

internal class MapDataCacheTest {

    @Test fun `update puts way`() {
        val way = way(1)
        val cache = MapDataCache(16, 4, 10) { emptyList<Element>() to emptyList() }
        cache.update(addedOrUpdatedElements = listOf(way))
        val elementDB: ElementDao = mock()
        on(elementDB.get(ElementType.WAY, 1L)).thenThrow(IllegalStateException())
        assertEquals(way, cache.getElement(ElementType.WAY, 1L) { type, id -> elementDB.get(type, id) })
    }

    @Test fun `getting non-node fetches from DB if not cached, and then caches`() {
        val way = way(2L)
        val cache = MapDataCache(16, 4, 10) { emptyList<Element>() to emptyList() }
        val elementDB: ElementDao = mock()
        on(elementDB.get(ElementType.WAY, 2L)).thenReturn(way).thenThrow(IllegalStateException()) // throw on second call
        // get way 2 and verify the fetch function is called, but only once (put it twice and throw the second time!)
        assertEquals(way, cache.getElement(ElementType.WAY, 2L) { type, id -> elementDB.get(type, id) })
        verify(elementDB).get(ElementType.WAY, 2L)
        // getting a second time does not fetch again
        assertEquals(way, cache.getElement(ElementType.WAY, 2L) { type, id -> elementDB.get(type, id) })
    }

    @Test fun `getting node always fetches if node not in spatialCache`() {
        val node = node(1)
        val cache = MapDataCache(16, 4, 10) { emptyList<Element>() to emptyList() }
        val elementDB: ElementDao = mock()
        on(elementDB.get(ElementType.NODE, 1L)).thenReturn(node).thenReturn(null)
        assertEquals(node, cache.getElement(ElementType.NODE, 1L) { type, id -> elementDB.get(type, id) })
        verify(elementDB).get(ElementType.NODE, 1L)
        // getting a second time fetches again
        assertNull(cache.getElement(ElementType.NODE, 1L) { type, id -> elementDB.get(type, id) })
    }

    @Test fun `update only puts nodes if tile is cached`() {
        val node = node(1, LatLon(0.0, 0.0))
        val cache = MapDataCache(16, 4, 10) { emptyList<Element>() to emptyList() }
        val nodeTile = node.position.enclosingTilePos(16)
        assertEquals(0, cache.getMapDataWithGeometry(nodeTile.asBoundingBox(16)).size)
        // now we have nodeTile cached
        cache.update(addedOrUpdatedElements = listOf(node))
        assertTrue(cache.getMapDataWithGeometry(nodeTile.asBoundingBox(16)).nodes.containsExactlyInAnyOrder(listOf(node)))
        assertEquals(node, cache.getElement(ElementType.NODE, 1L) { _,_ -> null })

        val otherNode = node(2, LatLon(0.0, 1.0))
        val otherNodeTile = otherNode.position.enclosingTilePos(16)
        cache.update(addedOrUpdatedElements = listOf(otherNode))
        assertNull(cache.getElement(ElementType.NODE, 2L) { _,_ -> null })
        assertTrue(cache.getMapDataWithGeometry(otherNodeTile.asBoundingBox(16)).nodes.isEmpty())

        val movedNode = node(1, LatLon(1.0, 0.0))
        val movedNodeTile = movedNode.position.enclosingTilePos(16)
        cache.update(addedOrUpdatedElements = listOf(movedNode))
        assertNull(cache.getElement(ElementType.NODE, 1L) { _,_ -> null })
        assertTrue(cache.getMapDataWithGeometry(movedNodeTile.asBoundingBox(16)).nodes.isEmpty())
        assertTrue(cache.getMapDataWithGeometry(nodeTile.asBoundingBox(16)).nodes.isEmpty())
    }

    @Test fun `update removes elements`() {
        val node = node(1, LatLon(0.0, 0.0))
        val way = way(2)
        val cache = MapDataCache(16, 4, 10) { listOf(node) to emptyList() }
        cache.getMapDataWithGeometry(node.position.enclosingTilePos(16).asBoundingBox(16))
        cache.update(addedOrUpdatedElements = listOf(way))
        assertTrue(
            cache.getElements(listOf(ElementKey(ElementType.NODE, 1L), ElementKey(ElementType.WAY, 2L))) { emptyList() }
                .containsExactlyInAnyOrder(listOf(node, way))
        )
        cache.update(deletedKeys = listOf(ElementKey(ElementType.NODE, 1L), ElementKey(ElementType.WAY, 2L)))
        assertNull(cache.getElement(ElementType.NODE, 1L) { _,_ -> null })
        assertNull(cache.getElement(ElementType.WAY, 2L) { _,_ -> null })
    }

    @Test fun getWaysForNode() {
        val node1 = node(1)
        val node2 = node(2)
        val node3 = node(3)
        val way1 = way(1, nodes = listOf(1L, 2L))
        val way2 = way(2, nodes = listOf(3L, 1L))
        val way3 = way(3, nodes = listOf(3L, 2L))
        val cache = MapDataCache(16, 4, 10) { emptyList<Element>() to emptyList() }
        cache.update(addedOrUpdatedElements = listOf(way1, way2, way3))
        val wayDB: WayDao = mock()
        on(wayDB.getAllForNode(1L)).thenReturn(listOf(way1, way2)).thenThrow(IllegalStateException())
        // fetches from cache if we didn't put the node
        //  todo: add comment to cache why... or adjust the cache
        assertTrue(cache.getWaysForNode(1L) { wayDB.getAllForNode(it) }.containsExactlyInAnyOrder(listOf(way1, way2)))
        verify(wayDB).getAllForNode(1L)
        // now we have it cached
        assertTrue(cache.getWaysForNode(1L) { wayDB.getAllForNode(it) }.containsExactlyInAnyOrder(listOf(way1, way2)))

        cache.clear()
        // if we at least tried to put the node,
        cache.update(addedOrUpdatedElements = listOf(node2, way1, way2, way3))
        assertTrue(cache.getWaysForNode(2L) { wayDB.getAllForNode(it) }.containsExactlyInAnyOrder(listOf(way1, way3)))
    }

    // todo: further tests
    //  trim, especially non-spatial caches (should remove stuff that does not contain nodes in spatial cache)
    //  wayIdsByNodeIdCache should be updated if a way changes, same for relations
    //  geometry
    @Test fun `update affects wayIdsByNodeIdCache`() {
        val cache = MapDataCache(16, 4, 10) { emptyList<Element>() to emptyList() }
    }
}
