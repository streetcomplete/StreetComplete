package de.westnordost.streetcomplete.data.download.tiles

import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.p
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TilesRectTest {

    @Test
    fun `convert bbox to tiles rect and back results in same bbox`() {
        val points = listOf(
            p(53.0, 9.0),
            p(0.0, 0.0),
            p(48.179, 16.414),
            //p(85.049, -179.989), // fails
        )
        for (p in points) {
            val tile = p.enclosingTilePos(15)
            val bbox = tile.asBoundingBox(15)
            assertTrue(bbox.min.latitude <= p.latitude)
            assertTrue(bbox.max.latitude >= p.latitude)
            assertTrue(bbox.min.longitude <= p.longitude)
            assertTrue(bbox.max.longitude >= p.longitude)
            val r = bbox.enclosingTilesRect(15)
            val bbox2 = r.asBoundingBox(15)
            assertEquals(bbox, bbox2)
        }
    }

    @Test
    fun `enclosingTilesRect of bbox that crosses 180th meridian does not`() {
        bbox(10.0, 170.0, 20.0, -170.0).enclosingTilesRect(4)
        // a TilesRect that is initialized crossing 180th meridian would throw an illegal argument
        // exception
    }

    @Test
    fun `asTileSequence returns sequence of contained tiles`() {
        assertEquals(listOf(
            TilePos(1, 1),
            TilePos(2, 1),
            TilePos(1, 2),
            TilePos(2, 2)
        ), TilesRect(1, 1, 2, 2).asTilePosSequence().toList())
    }

    @Test
    fun `minTileRect of empty list returns null`() {
        assertNull(listOf<TilePos>().minTileRect())
    }

    @Test
    fun `minTileRect of list with one entry returns tiles rect of size 1`() {
        assertEquals(TilesRect(1, 1, 1, 1), listOf(TilePos(1, 1)).minTileRect())
    }

    @Test
    fun `minTileRect returns correct minimum tiles rect`() {
        assertEquals(
            TilesRect(3, 2, 32, 15),
            listOf(
                TilePos(5, 8),
                TilePos(3, 2),
                TilePos(6, 15),
                TilePos(32, 12)
            ).minTileRect()
        )
    }

    @Test
    fun `TilesRect size returns correct size`() {
        assertEquals(12, TilesRect(0, 0, 3, 2).size)
    }
}
