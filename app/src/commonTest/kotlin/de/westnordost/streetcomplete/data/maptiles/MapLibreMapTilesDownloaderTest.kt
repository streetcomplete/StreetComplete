package de.westnordost.streetcomplete.data.maptiles

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MapLibreMapTilesDownloaderTest {

    @Test fun expiresOnlyTimestampsBeforeTheCutoff() {
        assertTrue(isOfflinePackExpired("99".encodeToByteArray(), 100L))
        assertFalse(isOfflinePackExpired("100".encodeToByteArray(), 100L))
        assertFalse(isOfflinePackExpired("101".encodeToByteArray(), 100L))
    }

    @Test fun expiresPacksWithoutAReadableTimestamp() {
        assertTrue(isOfflinePackExpired(null, 100L))
        assertTrue(isOfflinePackExpired(byteArrayOf(), 100L))
        assertTrue(isOfflinePackExpired("not a timestamp".encodeToByteArray(), 100L))
        assertTrue(isOfflinePackExpired("9223372036854775808".encodeToByteArray(), 100L))
    }

    @Test fun createsStreetCompleteTilePyramidWithoutSwappingAxes() {
        val definition = BoundingBox(37.5, -122.5, 38.0, -122.0)
            .toOfflinePackDefinition(pixelRatio = 3f)

        assertEquals("https://streetcomplete.app/map-jawg/streetcomplete.json", definition.styleUrl)
        assertEquals(-122.5, definition.bounds.west)
        assertEquals(37.5, definition.bounds.south)
        assertEquals(-122.0, definition.bounds.east)
        assertEquals(38.0, definition.bounds.north)
        assertEquals(0, definition.minZoom)
        assertEquals(16, definition.maxZoom)
        assertEquals(3f, definition.pixelRatio)
    }
}
