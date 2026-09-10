package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.extensions.meters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.TestTimeSource

class LocationTest {
    private val measurement = LocationMeasurement(
        position = Position(13.0, 52.0, 45.0),
        horizontalAccuracy = 7.meters,
        measuredAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
    )

    @Test fun `survey age uses the monotonic measurement mark`() {
        val time = TestTimeSource()
        val event = LocationEvent.Update(measurement, time.markNow())
        time += 12.seconds
        val location = event.toLocation()
        assertEquals(12.seconds, location.elapsedDuration)
        assertEquals(LatLon(52.0, 13.0), location.position)
        assertEquals(7f, location.accuracy)
    }

    @Test fun `convert measurement to trackpoint`() {
        val trackpoint = measurement.toTrackpoint()
        assertEquals(1_700_000_000_000L, trackpoint.time)
        assertEquals(LatLon(52.0, 13.0), trackpoint.position)
        assertEquals(7f, trackpoint.accuracy)
        assertEquals(45f, trackpoint.elevation)
    }
}
