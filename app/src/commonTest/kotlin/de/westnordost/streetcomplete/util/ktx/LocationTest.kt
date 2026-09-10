package de.westnordost.streetcomplete.util.ktx

import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.spatialk.geojson.Position
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.TimeSource

class LocationTest {
    @Test fun `conversion orders fixes by monotonic measurement time`() {
        // Using a fix's age as its timestamp made RecentLocations treat older fixes as newer.
        val mark = TimeSource.Monotonic.markNow()
        val measurement = LocationMeasurement(
            position = Position(2.0, 1.0),
            measuredAt = Instant.fromEpochSeconds(100),
        )
        val older = LocationEvent.Update(measurement, mark - 10.seconds).toLocation()
        val newer = LocationEvent.Update(
            measurement.copy(position = Position(3.0, 1.0), measuredAt = Instant.fromEpochSeconds(90)),
            mark,
        ).toLocation()

        assertTrue(newer.elapsedDuration > older.elapsedDuration)
    }
}
