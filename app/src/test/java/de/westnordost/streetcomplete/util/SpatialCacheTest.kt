package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.data.download.tiles.enclosingTilePos
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.testutils.node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class SpatialCacheTest {

    @Test fun `update doesn't put if tile doesn't exist`() {
        val node = node(1)
        val cache = SpatialCache<Long, Node>(
            16, 4, null, { emptyList() }, Node::id, Node::position
        )
        cache.update(updatedOrAdded = listOf(node))
        assertNull(cache.get(node.id))
    }

    @Test fun `get adds tile`() {
        val cache = SpatialCache<Long, Node>(
            16, 4, null, { emptyList() }, Node::id, Node::position
        )
        assertTrue(cache.getTiles().isEmpty())
        val tile = LatLon(1.0, 1.0).enclosingTilePos(16)
        cache.get(tile.asBoundingBox(16))
        assertTrue(cache.getTiles().contains(tile))
    }

    @Test fun `update puts if tile exists`() {
        val node = node(1)
        val nodeTile = node.position.enclosingTilePos(16)
        val cache = SpatialCache<Long, Node>(
            16, 4, null, { emptyList() }, Node::id, Node::position
        )
        cache.get(nodeTile.asBoundingBox(16))
        cache.update(updatedOrAdded = listOf(node))
        assertEquals(cache.get(node.id), node)
    }

    @Test fun `update removes element if moving to non-cached tile`() {
        val node = node(1)
        val nodeTile = node.position.enclosingTilePos(16)
        val cache = SpatialCache<Long, Node>(
            16, 4, null, { emptyList() }, Node::id, Node::position
        )
        cache.get(nodeTile.asBoundingBox(16))
        cache.update(updatedOrAdded = listOf(node))
        cache.update(updatedOrAdded = listOf(node.copy(position = LatLon(1.0, 1.0))))
        assertNull(cache.get(node.id))
    }

    @Test fun replaceAllInBBox() {
        val nodeInside = node(1)
        val nodeTile = nodeInside.position.enclosingTilePos(16)
        val nodeOutside = node(2, LatLon(1.0, 1.0))
        val cache = SpatialCache<Long, Node>(
            16, 4, null, { emptyList() }, Node::id, Node::position
        )
        cache.replaceAllInBBox(listOf(nodeInside, nodeOutside), nodeTile.asBoundingBox(16))

        assertTrue(cache.getKeys().contains(nodeInside.id))
        assertFalse(cache.getKeys().contains(nodeOutside.id))
    }

    @Test fun remove() {
        val node = node(1)
        val nodeTile = node.position.enclosingTilePos(16)
        val cache = SpatialCache<Long, Node>(
            16, 4, null, { emptyList() }, Node::id, Node::position
        )
        cache.get(nodeTile.asBoundingBox(16))
        cache.update(updatedOrAdded = listOf(node))
        cache.update(deleted = listOf(node.id))
        assertNull(cache.get(node.id))
    }

    @Test fun automaticTrim() {
        val cache = SpatialCache<Long, Node>(
            16, 2, null, { emptyList() }, Node::id, Node::position
        )
        cache.replaceAllInBBox(emptyList(), LatLon(0.0, 0.0).enclosingTilePos(16).asBoundingBox(16))
        cache.replaceAllInBBox(emptyList(), LatLon(1.0, 1.0).enclosingTilePos(16).asBoundingBox(16))
        cache.replaceAllInBBox(emptyList(), LatLon(-1.0, -1.0).enclosingTilePos(16).asBoundingBox(16))
        assertEquals(cache.getTiles().size, 2)
    }

}
