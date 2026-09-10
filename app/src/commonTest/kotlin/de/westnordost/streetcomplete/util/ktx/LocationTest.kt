package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.data.location.RecentLocations
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.spatialk.geojson.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.TimeSource

class LocationTest {
    @Test fun `survey locations follow monotonic measurement order`() {
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

        val recent = RecentLocations(60.seconds, 1.0, 1.seconds)
        recent.add(older)
        recent.add(newer)
        assertEquals(listOf(newer, older), recent.getAll().toList())
    }
}
