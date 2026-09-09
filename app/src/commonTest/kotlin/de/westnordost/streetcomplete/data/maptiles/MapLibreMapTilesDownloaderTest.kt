package de.westnordost.streetcomplete.data.maptiles

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.maplibre.compose.offline.OfflineManager

class MapLibreMapTilesDownloaderTest {

    @Test fun clearingOfflineMapsPropagatesCancellation() = runTest {
        val manager = mock<OfflineManager>()
        val cancelled = CancellationException("screen left")
        every { manager.packs } returns emptySet()
        everySuspend { manager.clearAmbientCache() } throws cancelled

        val failure = assertFailsWith<CancellationException> {
            MapLibreMapTilesDownloader(manager, pixelRatio = 1f).clear()
        }

        assertSame(cancelled, failure)
    }

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
