package de.westnordost.streetcomplete.util

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.OsmLatLon
import org.junit.Assert.*
import org.junit.Test

class SlippyMapMathTest {

    @Test fun `convert bbox to tiles rect and back results in same bbox`() {
        val p = OsmLatLon(53.0, 9.0)
        val tile = p.enclosingTile(15)
        val bbox = tile.asBoundingBox(15)
        assertTrue(bbox.minLatitude <= p.latitude)
        assertTrue(bbox.maxLatitude >= p.latitude)
        assertTrue(bbox.minLongitude <= p.longitude)
        assertTrue(bbox.maxLongitude >= p.longitude)
        val r = bbox.enclosingTilesRect(15)
        val bbox2 = r.asBoundingBox(15)
        assertEquals(bbox, bbox2)
    }

    @Test fun `enclosingTilesRect of bbox that crosses 180th meridian does not`() {
        BoundingBox(10.0, 170.0, 20.0, -170.0).enclosingTilesRect(4)
        // a TilesRect that is initialized crossing 180th meridian would throw an illegal argument
        // exception
    }

    @Test fun `asTileSequence returns sequence of contained tiles`() {
        assertEquals(listOf(
            Tile(1, 1),
            Tile(2, 1),
            Tile(1, 2),
            Tile(2, 2)
        ), TilesRect(1, 1, 2, 2).asTileSequence().toList())
    }

    @Test fun `minTileRect of empty list returns null`() {
        assertNull(listOf<Tile>().minTileRect())
    }

    @Test fun `minTileRect of list with one entry returns tiles rect of size 1`() {
        assertEquals(TilesRect(1,1,1,1), listOf(Tile(1,1)).minTileRect())
    }

    @Test fun `minTileRect returns correct minimum tiles rect`() {
        assertEquals(
            TilesRect(3, 2, 32, 15),
            listOf(
                Tile(5, 8),
                Tile(3, 2),
                Tile(6, 15),
                Tile(32, 12)
            ).minTileRect()
        )
    }

    @Test fun `TilesRect size returns correct size`() {
        assertEquals(12, TilesRect(0, 0, 3, 2).size)
    }
}