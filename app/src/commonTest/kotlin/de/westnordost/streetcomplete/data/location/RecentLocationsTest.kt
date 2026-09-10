package de.westnordost.streetcomplete.data.location

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.asSequenceOfPairs
import de.westnordost.streetcomplete.util.math.translate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class RecentLocationsTest {
    @Test fun `getAll returns nothing when empty`() {
        val r = RecentLocations(10.seconds, 1.0, 1.seconds)
        assertTrue(r.getAll().toList().isEmpty())
    }

    @Test fun `getAll returns one`() {
        val r = RecentLocations(10.seconds, 1.0, 1.seconds)
        val l1 = location(LatLon(0.0, 0.0), 1)
        r.add(l1)

        assertEquals(
            l1,
            r.getAll().toList().single()
        )
    }

    @Test fun `getAll returns newest measurements first`() {
        val r = RecentLocations(10.seconds, 1.0, 1.seconds)
        val l1 = location(LatLon(0.0, 0.0), 1)
        val l2 = location(LatLon(1.0, 0.0), 2)

        r.add(l1)
        r.add(l2)
        for ((first, second) in r.getAll().toList().asSequenceOfPairs()) {
            assertTrue(first.measuredAt > second.measuredAt)
        }
    }

    @Test fun `does not add older locations`() {
        val r = RecentLocations(10.seconds, 1.0, 1.seconds)
        val l1 = location(LatLon(0.0, 0.0), 10)
        val l2 = location(LatLon(1.0, 0.0), 1)
        r.add(l1)
        r.add(l2)
        assertEquals(
            l1,
            r.getAll().toList().single()
        )
    }

    @Test fun `getAll does not return locations too close to each other`() {
        val r = RecentLocations(10.seconds, 100.0, 1.seconds)
        val l1 = location(LatLon(0.0, 0.0), 1)
        val l2 = location(LatLon(0.0, 0.0).translate(80.0, 0.0), 2)
        val l3 = location(LatLon(0.0, 0.0).translate(160.0, 0.0), 3)
        val l4 = location(LatLon(0.0, 0.0).translate(240.0, 0.0), 4)

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
        val l1 = location(LatLon(0.0, 0.0), 1)
        val l2 = location(LatLon(1.0, 0.0), 2)
        val l3 = location(LatLon(2.0, 0.0), 3)
        val l4 = location(LatLon(3.0, 0.0), 4)

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
        val l1 = location(LatLon(0.0, 0.0), 1)
        val l2 = location(LatLon(1.0, 0.0), 5)
        val l3 = location(LatLon(2.0, 0.0), 8)
        val l4 = location(LatLon(3.0, 0.0), 17)

        r.add(l1)
        assertEquals(listOf(l1), r.getAll().toList())
        r.add(l2)
        assertEquals(listOf(l2, l1), r.getAll().toList())
        r.add(l3)
        assertEquals(listOf(l3, l2, l1), r.getAll().toList())
        r.add(l4)
        assertEquals(listOf(l4, l3), r.getAll().toList())
    }

    private fun location(position: LatLon, measuredAtSeconds: Long) =
        Location(position, 1f, Instant.fromEpochSeconds(measuredAtSeconds))
}
