package de.westnordost.streetcomplete.data.location

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.asSequenceOfPairs
import de.westnordost.streetcomplete.util.ktx.toLocation
import de.westnordost.streetcomplete.util.math.translate
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.spatialk.geojson.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.TimeSource

class RecentLocationsTest {
    @Test fun `getAll returns nothing when empty`() {
        val r = RecentLocations(10.seconds, 1.0, 1.seconds)
        assertTrue(r.getAll().toList().isEmpty())
    }

    @Test fun `getAll returns one`() {
        val r = RecentLocations(10.seconds, 1.0, 1.seconds)
        val l1 = Location(LatLon(0.0, 0.0), 1f, 1.seconds)
        r.add(l1)

        assertEquals(
            l1,
            r.getAll().toList().single()
        )
    }

    @Test fun `accepts first location with zero elapsedDuration`() {
        val r = RecentLocations(10.seconds, 1.0, 1.seconds)
        val location = Location(LatLon(0.0, 0.0), 1f, 0.seconds)
        r.add(location)

        assertEquals(location, r.getAll().toList().single())
    }

    @Test fun `accepts first location with negative elapsedDuration`() {
        // A cached fix can predate the adapter's monotonic time origin.
        val r = RecentLocations(10.seconds, 1.0, 1.seconds)
        val location = Location(LatLon(0.0, 0.0), 1f, (-1).seconds)
        r.add(location)

        assertEquals(location, r.getAll().toList().single())
    }

    @Test fun `getAll returns ordered by elapsedDuration descending`() {
        val r = RecentLocations(10.seconds, 1.0, 1.seconds)
        val l1 = Location(LatLon(0.0, 0.0), 1f, 1.seconds)
        val l2 = Location(LatLon(1.0, 0.0), 1f, 2.seconds)

        r.add(l1)
        r.add(l2)
        for ((first, second) in r.getAll().toList().asSequenceOfPairs()) {
            assertTrue(first.elapsedDuration > second.elapsedDuration)
        }
    }

    @Test fun `retains a fresh fix after an older converted fix`() {
        // Using a fix's age as its timestamp made RecentLocations discard newer fixes.
        val r = RecentLocations(60.seconds, 1.0, 1.seconds)
        val mark = TimeSource.Monotonic.markNow()
        val measurement = LocationMeasurement(
            position = Position(2.0, 1.0),
            measuredAt = Instant.fromEpochSeconds(100),
        )
        val older = LocationEvent.Update(measurement, mark - 10.seconds).toLocation()
        val newer = LocationEvent.Update(
            measurement.copy(position = Position(3.0, 1.0), measuredAt = Instant.fromEpochSeconds(110)),
            mark,
        ).toLocation()
        r.add(older)
        r.add(newer)

        assertEquals(listOf(newer, older), r.getAll().toList())
    }

    @Test fun `does not add older locations`() {
        val r = RecentLocations(10.seconds, 1.0, 1.seconds)
        val l1 = Location(LatLon(0.0, 0.0), 1f, 10.seconds)
        val l2 = Location(LatLon(1.0, 0.0), 1f, 1.seconds)
        r.add(l1)
        r.add(l2)
        assertEquals(
            l1,
            r.getAll().toList().single()
        )
    }

    @Test fun `getAll does not return locations too close to each other`() {
        val r = RecentLocations(10.seconds, 100.0, 1.seconds)
        val l1 = Location(LatLon(0.0, 0.0), 1f, 1.seconds)
        val l2 = Location(LatLon(0.0, 0.0).translate(80.0, 0.0), 1f, 2.seconds)
        val l3 = Location(LatLon(0.0, 0.0).translate(160.0, 0.0), 1f, 3.seconds)
        val l4 = Location(LatLon(0.0, 0.0).translate(240.0, 0.0), 1f, 4.seconds)

        r.add(l1)
        assertEquals(listOf(l1), r.getAll().toList())

        r.add(l2)
        assertEquals(listOf(l2), r.getAll().toList())

        r.add(l3)
        assertEquals(listOf(l3, l1), r.getAll().toList())

        r.add(l4)
        assertEquals(listOf(l4, l2), r.getAll().toList())
    }

    @Test fun `getAll does not return locations with too little time difference to each other`() {
        val r = RecentLocations(10.seconds, 1.0, 2.seconds)
        val l1 = Location(LatLon(0.0, 0.0), 1f, 1.seconds)
        val l2 = Location(LatLon(1.0, 0.0), 1f, 2.seconds)
        val l3 = Location(LatLon(2.0, 0.0), 1f, 3.seconds)
        val l4 = Location(LatLon(3.0, 0.0), 1f, 4.seconds)

        r.add(l1)
        assertEquals(listOf(l1), r.getAll().toList())

        r.add(l2)
        assertEquals(listOf(l2), r.getAll().toList())

        r.add(l3)
        assertEquals(listOf(l3, l1), r.getAll().toList())

        r.add(l4)
        assertEquals(listOf(l4, l2), r.getAll().toList())
    }

    @Test fun `removes oldest locations on add`() {
        val r = RecentLocations(10.seconds, 100.0, 1.seconds)
        val l1 = Location(LatLon(0.0, 0.0), 1f, 1.seconds)
        val l2 = Location(LatLon(1.0, 0.0), 1f, 5.seconds)
        val l3 = Location(LatLon(2.0, 0.0), 1f, 8.seconds)
        val l4 = Location(LatLon(3.0, 0.0), 1f, 17.seconds)

        r.add(l1)
        assertEquals(listOf(l1), r.getAll().toList())
        r.add(l2)
        assertEquals(listOf(l2, l1), r.getAll().toList())
        r.add(l3)
        assertEquals(listOf(l3, l2, l1), r.getAll().toList())
        r.add(l4)
        assertEquals(listOf(l4, l3), r.getAll().toList())
    }
}
